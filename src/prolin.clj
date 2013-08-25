(ns prolin
  (:require [prolin.protocols :as p]))

(defn subtract-polynomial
  "Given a LinearPolynomial, subtract a second polynomial."
  [a b]
  (let [c (- (p/constant a) (p/constant b))
        vs (merge-with (fnil - 0 0) (p/variables a) (p/variables b))]
    (reify p/LinearPolynomial
      (variables [_] vs)
      (constant [_] c))))

