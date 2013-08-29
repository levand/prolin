(ns prolin.chebyshev
  (:require [prolin.protocols :as p]
            [prolin.polynomial :as poly]))

(defn dot-product
  "Returns the dot product of two vectors"
  [v1 v2]
  (reduce + (map * v1 v2)))

(defn- standardize
  "Puts a constraint in 'standard' form for easier manipulation by the orthagonal formula:

   a'x + c <= 0"

  [c]
  (if (= '>= (p/relation c))
    (p/constraint '<= (poly/multiply (p/polynomial c) -1))
    c))

(defn orthagonal
  "Return a constraint orthagonal to the provided constraint, with an
   additional term d inserted, which relates the distance from the
   original point to the new point.

   In other words, transforms:

   a'x + c <= 0

   to:

   a'y + ||a||r + c <= 0

"
  [constraint d]
  (let [c (standardize constraint)
        p (p/polynomial c)]
    (p/constraint (p/relation c)
                  (p/linear-polynomial
                   (p/constant p)
                   (let [vs (p/variables p)
                         cs (vals vs)]
                     (assoc vs d (Math/sqrt (dot-product cs cs))))))))

(defn chebyshev-center
  "Given a set of constraints, returns the set of constraints altered
  to refer to the coordinates and radius of the Chebyshev center of
  the feasible polyhedron indicated by the input set. Uses the provided
  r argument as the variable identifier for the radius."
  [constraints r]
  (map #(orthagonal % r) constraints))

