(ns prolin.polynomial-test
  (:require [prolin.polynomial :as poly]
            [prolin.protocols :as p]
            [clojure.set :as set]
            [clojure.data.generators :as gen]
            [clojure.test.generative :refer [defspec]])
  (:import [org.apache.commons.math3.util Precision]))


(defmacro debug-ex
  "Executes body, re-throwings any exception as an ex-info with the
  given message, and the bindings evaluated and added to the ex-data map."
  [msg bindings & body]
  `(try
     (do ~@body)
     (catch Throwable t#
       (throw (ex-info ~msg
                       (hash-map ~@(mapcat (fn [[v e]] [(list 'quote v) e])
                                           (partition 2 bindings)))
                       t#)))))

(defn f=
  "Fuzzy floating point equals"
  [x y]
  (Precision/equals (double x) (double y) 1e-6))

(defn gen-number
  "Generate a random double in a realistic range for coefficients"
  []
  (* (gen/double) (gen/uniform -1000 1000)))

(defn gen-set
  "Generates a random set with a fixed size populated with values from
  f. The built in gen/set is incapable of doing this."
  [f size]
  (loop [s #{}]
    (let [v (f)]
      (if (< (count s) size)
        (recur (conj s v))
        s))))

(defn gen-linear-polynomial
  "Generate a random polynomial with variables provided by varf and values provided by valf"
  [varf valf]
  (let [c (valf)
        varset (gen/set varf 2)
        varmap (into {} (map (fn [var] [var (valf)]) varset))]
    (assoc varmap :prolin.protocols/constant c)))

(defn gen-var
  "Generate a random variable"
  []
  (str (gen/uniform 1 5)))

(defspec polynomial-instantiation
  (fn [constant variables coefficients values]
    (let [sum (reduce + constant (map * coefficients values))
          poly (assoc (zipmap variables coefficients) :prolin.protocols/constant constant)
          values (zipmap variables values)
          poly-sum (poly/instantiate poly values)]
      {:sum sum :poly-sum poly-sum}))
  [^{:tag (prolin.polynomial-test/gen-number)} constant
   ^{:tag (prolin.polynomial-test/gen-set gen/anything 10)} variables
   ^{:tag (gen/vec prolin.polynomial-test/gen-number 10)} coefficients
   ^{:tag (gen/vec prolin.polynomial-test/gen-number 10)} values]
  (assert (f= (:sum %) (:poly-sum %))))

(defspec polynomial-addition
  poly/add
  [^{:tag (prolin.polynomial-test/gen-linear-polynomial
           prolin.polynomial-test/gen-var
           prolin.polynomial-test/gen-number)} a
   ^{:tag (prolin.polynomial-test/gen-linear-polynomial
           prolin.polynomial-test/gen-var
           prolin.polynomial-test/gen-number)} b]
  (let [vars (set/union (keys (p/variables a))
                        (keys (p/variables b)))
        values (zipmap vars (repeatedly prolin.polynomial-test/gen-number))
        val (poly/instantiate % values)
        check (+ (poly/instantiate a values) (poly/instantiate b values))]
    (debug-ex "The check should have been equal to the calculated value"
              [val val
               check check]
              (assert (f= val check)))))

(defspec polynomial-multiplication
  poly/multiply
  [^{:tag (prolin.polynomial-test/gen-linear-polynomial
           prolin.polynomial-test/gen-var
           prolin.polynomial-test/gen-number)} poly
   ^{:tag prolin.polynomial-test/gen-number} n]
  (let [values (zipmap (keys (p/variables poly))
                       (repeatedly prolin.polynomial-test/gen-number))
        val (poly/instantiate % values)
        check (* n (poly/instantiate poly values))]
    (debug-ex "The check should have been equal to the calculated value"
              [val val
               check check]
              (assert (f= val check)))))

(defspec polynomial-subtraction
  poly/subtract
  [^{:tag (prolin.polynomial-test/gen-linear-polynomial
           prolin.polynomial-test/gen-var
           prolin.polynomial-test/gen-number)} a
   ^{:tag (prolin.polynomial-test/gen-linear-polynomial
           prolin.polynomial-test/gen-var
           prolin.polynomial-test/gen-number)} b]
  (let [vars (set/union (keys (p/variables a))
                        (keys (p/variables b)))
        values (zipmap vars (repeatedly prolin.polynomial-test/gen-number))
        val (poly/instantiate % values)
        diff (- (poly/instantiate a values) (poly/instantiate b values))]
    (debug-ex "The differences should have been equal"
              [val val
               diff diff
               a a
               b b
               values values]
              (assert (f= val diff)))))
