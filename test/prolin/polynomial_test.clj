(ns prolin.polynomial-test
  (:require [prolin.polynomial :as poly]
            [prolin.protocols :as p]
            [prolin.testutil :as util]
            [clojure.set :as set]
            [clojure.data.generators :as gen]
            [clojure.test.generative :refer [defspec]]))

(defspec polynomial-instantiation
  (fn [constant variables coefficients values]
    (let [sum (reduce + constant (map * coefficients values))
          poly (p/linear-polynomial constant (zipmap variables coefficients))
          values (zipmap variables values)
          poly-sum (poly/instantiate poly values)]
      {:sum sum :poly-sum poly-sum}))
  [^{:tag (util/gen-number)} constant
   ^{:tag (util/gen-set gen/anything 10)} variables
   ^{:tag (gen/vec util/gen-number 10)} coefficients
   ^{:tag (gen/vec util/gen-number 10)} values]
  (assert (util/f= (:sum %) (:poly-sum %))))

(defspec polynomial-addition
  poly/add
  [^{:tag (util/gen-linear-polynomial
           util/gen-var
           util/gen-number)} a
   ^{:tag (util/gen-linear-polynomial
           util/gen-var
           util/gen-number)} b]
  (let [vars (set/union (keys (p/variables a))
                        (keys (p/variables b)))
        values (zipmap vars (repeatedly util/gen-number))
        val (poly/instantiate % values)
        check (+ (poly/instantiate a values) (poly/instantiate b values))]
    (util/debug-ex "The check should have been equal to the calculated value"
                   [val val
                    check check]
                   (assert (util/f= val check)))))

(defspec polynomial-multiplication
  poly/multiply
  [^{:tag (util/gen-linear-polynomial
           util/gen-var
           util/gen-number)} poly
   ^{:tag util/gen-number} n]
  (let [values (zipmap (keys (p/variables poly))
                       (repeatedly util/gen-number))
        val (poly/instantiate % values)
        check (* n (poly/instantiate poly values))]
    (util/debug-ex "The check should have been equal to the calculated value"
                   [val val
                    check check]
                   (assert (util/f= val check)))))

(defspec polynomial-subtraction
  poly/subtract
  [^{:tag (util/gen-linear-polynomial
           util/gen-var
           util/gen-number)} a
   ^{:tag (util/gen-linear-polynomial
           util/gen-var
           util/gen-number)} b]
  (let [vars (set/union (keys (p/variables a))
                        (keys (p/variables b)))
        values (zipmap vars (repeatedly util/gen-number))
        val (poly/instantiate % values)
        diff (- (poly/instantiate a values) (poly/instantiate b values))]
    (util/debug-ex "The differences should have been equal"
                   [val val
                    diff diff
                    a a
                    b b
                    values values]
                   (assert (util/f= val diff)))))
