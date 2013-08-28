(ns prolin.commons-math-test
  (:require [clojure.test :refer :all]
            [prolin.testutil :as util]
            [prolin :as prolin]
            [prolin.commons-math :as cm]
            [prolin.protocols :as p]))

(deftest one-variable
  (let [constraints #{(p/constraint '<= (p/linear-polynomial -5 {:x 1}))
                      (p/constraint '>= (p/linear-polynomial 2 {:x 1}))}
        solver (cm/solver {})]
    (is (= {:x 5.0} (prolin/maximize solver (p/linear-polynomial 0 {:x 1}) constraints)))
    (is (= {:x -2.0} (prolin/minimize solver (p/linear-polynomial 0 {:x 1}) constraints)))
    (is (= {:x -2.0} (prolin/maximize solver (p/linear-polynomial 0 {:x -10}) constraints)))))


(deftest point-on-a-line
  (let [constraints #{(p/constraint '= (p/linear-polynomial 0 {:x 2 :y -1}))
                      (p/constraint '<= (p/linear-polynomial -5 {:y 1}))}
        solver (cm/solver {})]
    (is (= {:y 5.0 :x 2.5} (prolin/maximize solver (p/linear-polynomial 0 {:y 1}) constraints)))
    (is (= {:x 2.5 :y 5.0} (prolin/maximize solver (p/linear-polynomial 0 {:x 1}) constraints)))
    (is (thrown? clojure.lang.ExceptionInfo
                 (prolin/minimize solver (p/linear-polynomial 0 {:x 1}) constraints)))))

(deftest an-impossible-conundrum
  (let [constraints #{(p/constraint '= (p/linear-polynomial 2 {:x 1}))
                      (p/constraint '>= (p/linear-polynomial 0 {:x 1}))}
        solver (cm/solver {})]
    (is (thrown? clojure.lang.ExceptionInfo
                 (prolin/maximize solver (p/linear-polynomial 0 {:x 1}) constraints)))))

