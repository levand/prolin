(ns prolin.polynomial
  (:require [prolin.protocols :as p]))

(defn multiply
  "Multiply a LinearPolynomial by a constant"
  [polynomial n]
  (let [c (* n (p/constant polynomial))
        old-vs (p/variables polynomial)
        vs (zipmap (keys old-vs) (map #(* n %) (vals old-vs)))]
    (reify p/LinearPolynomial
      (variables [_] vs)
      (constant [_] c))))

(defn add
  "Add a LinearPolynomial to another"
  [a b]
  (let [c (+ (p/constant a) (p/constant b))
        vs (merge-with (fnil + 0 0) (p/variables a) (p/variables b))]
    (reify p/LinearPolynomial
      (variables [_] vs)
      (constant [_] c))))

(defn subtract
  "Subtract a polynomial from another"
  [a b]
  (add a (multiply b -1)))

(defn zero
  "Construct a LinearPolynomial that contains the set of provided
   variables, each with a coefficient of zero."
  [variables]
  (let [vs (into {} (map (fn [v] [v 0]) variables))]
    (reify p/LinearPolynomial
      (variables [_] vs)
      (constant [_] 0))))

(defn instantiate
  "Given a LinearPolynomial and a map of variables to values, return a
  single number. All variables in the polynomial must be present in
  the value map."
  [polynomial values]
  (reduce + (p/constant polynomial)
          (map (fn [[v c]]
                 (* c (values v 0)))
               (p/variables polynomial))))


