(ns prolin
  (:require [prolin.protocols :as p]
            [prolin.polynomial :as poly]
            [clojure.string :as s]))

(defn optimize
  "Find the maximum or minimum value of an objective LinearPolynomial, subject to
  the given set of Constraints, using the specified solver implementation."
  [solver objective constraints minimize?]
  (p/optimize solver objective constraints minimize?))

(defn minimize
  "Find the minimum value of an objective LinearPolynomial, subject to
  the given set of Constraints, using the specified solver implementation."
  [solver objective constraints]
  (p/optimize solver objective constraints true))

(defn maximize
  "Find the maximum value of an objective LinearPolynomial, subject to
  the given set of Constraints, using the specified solver implementation."
  [solver objective constraints]
  (p/optimize solver objective constraints false))


;; Extend LinearPolynomial and Constraint to java.lang.String
;; Uses very rudimetary regex-based parsing

(defn- parse-variable
  "Given a string term, return the variable (as a string). Returns nil
  if not present."
  [term]
  (re-find #"[a-zA-Z]" term))

(defn- parse-coefficient
  "Given a string term, return the coefficient (as a double). Returns
  1 if the term doesn't have an explicit coefficient."
  [term]
  (let [stripped (s/replace term #"\s" "")
        c (re-find #"[+\-]?[0-9]*\.?[0-9]+" stripped)]
    (cond
     c (Double/parseDouble c)
     (re-find #"-" term) -1.0
     :else 1.0)))

(defn- parse-term
  "Parse a term and return a tuple of [variable coefficient]"
  [term]
  [(parse-variable term) (parse-coefficient term)])

(defn- parse-terms
  "Given a string, return individual terms (as strings)"
  [s]
  (re-seq #"[+\-]?\s*[0-9a-zA-z.]+" s))

(extend-type String
  p/LinearPolynomial
  (constant [s]
    (let [terms (map parse-term (parse-terms s))
          constant-terms (filter (comp nil? first) terms)]
      (or (second (first constant-terms)) 0)))
  (variables [s]
    (into {} (filter (comp identity first)
                     (map parse-term (parse-terms s)))))
  p/Constraint
  (relation [s] (symbol (re-find #"<=|>=|=" s)))
  (polynomial [s]
    (let [[lhs rhs] (s/split s #"<=|>=|=")]
      (poly/subtract lhs rhs))))
