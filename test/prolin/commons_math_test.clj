(ns prolin.commons-math-test
  (:require [clojure.test :refer :all]
            [prolin :as prolin]
            [prolin.commons-math :as cm]
            [prolin.protocols :as p]))

(deftest one-variable
  (let [constraints #{"x <= 5", "x >= -2"}]
    (is (= {"x" 5.0} (prolin/maximize (cm/solver) "x" constraints)))
    (is (= {"x" -2.0} (prolin/minimize (cm/solver) "x" constraints)))
    (is (= {"x" -2.0} (prolin/maximize (cm/solver) "-x" constraints)))))


(deftest point-on-a-line
  (let [constraints #{"2x = y", "y <= 5"}]
    (is (= {"y" 5.0 "x" 2.5} (prolin/maximize (cm/solver) "y" constraints)))
    (is (= {"x" 2.5 "y" 5.0} (prolin/maximize (cm/solver) "x" constraints)))
    (is (thrown? clojure.lang.ExceptionInfo
                 (prolin/minimize (cm/solver) "x" constraints)))))

(deftest an-impossible-conundrum
  (let [constraints #{"x = -2", "x >= 0"}]
    (is (thrown? clojure.lang.ExceptionInfo
                 (prolin/maximize (cm/solver) "x" constraints)))))
