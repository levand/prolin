(ns prolin.testutil
  (:require [prolin.protocols :as p]
            [clojure.data.generators :as gen])
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
    (p/linear-polynomial c varmap)))

(defn gen-var
  "Generate a random variable name"
  []
  (str (gen/uniform 1 5)))

