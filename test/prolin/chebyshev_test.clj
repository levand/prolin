(ns prolin.chebyshev-test
  (:require [clojure.test :refer :all]
            [prolin :as prolin]
            [prolin.chebyshev :refer :all]
            [prolin.commons-math :as cm]
            [prolin.protocols :as p])
  (:import [org.apache.commons.math3.util Precision]))

(defn f=
  "Fuzzy floating point equals"
  [x y]
  (Precision/equals (double x) (double y) 1e-6))

(deftest center-of-line
  (is (f= 0 (get (prolin/maximize (cm/solver)
                                  "r" (chebyshev-center ["x <= 10"
                                                         "x >= -10"] "r"))
                 "x"))))

(deftest center-of-square
  (is (= {"x" 0.5 "y" 0.5 "r" 0.5}
         (prolin/maximize (cm/solver)
                          "r" (chebyshev-center ["x <= 1"
                                                 "x >= 0"
                                                 "y <= 1"
                                                 "y >= 0"] "r")))))

(deftest center-of-triangle
  (let [{x "x" y "y"}
        (prolin/maximize (cm/solver)
                         "r" (chebyshev-center ["y >= 0"
                                                "y - 4 <= x"
                                                "y <= 4 - x"] "r"))]
    (is (f= x 0))
    (is (f= y 1.656854249423))))
