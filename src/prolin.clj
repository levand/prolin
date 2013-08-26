(ns prolin
  (:require [prolin.protocols :as p]))

(defn minimize
  "Find the minimum value of an objective LinearPolynomial, subject to
  the given set of Constraints, using the specified solver implementation."
  [solver objective constraints]
  (p/optimize objective constraints true))

(defn maximize
  "Find the maximum value of an objective LinearPolynomial, subject to
  the given set of Constraints, using the specified solver implementation."
  [solver objective constraints]
  (p/optimize objective constraints false))
