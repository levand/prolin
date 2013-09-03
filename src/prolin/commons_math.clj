(ns prolin.commons-math
  (:require [prolin.protocols :as p]
            [prolin.polynomial :as poly]
            [clojure.string :as str]
            [clojure.set :as set])
  (:import [org.apache.commons.math3.optim.linear
            SimplexSolver
            LinearObjectiveFunction
            LinearConstraint
            LinearConstraintSet
            Relationship]
           [org.apache.commons.math3.optim.nonlinear.scalar GoalType]
           [org.apache.commons.math3.optim OptimizationData]
           [org.apache.commons.math3.optim.linear
            UnboundedSolutionException
            NoFeasibleSolutionException]))

(def ^:dynamic *debug* false)

(defmulti debug class)

(defmethod debug prolin.protocols.LinearPolynomial
  [p]
  (str (p/variables p) " " (p/constant p)))

(defmethod debug prolin.protocols.Constraint
  [c]
  (str (debug (p/polynomial c)) " " (p/relation c) " 0"))

(defmethod debug :default
  [o]
  (str o))

(defmethod debug org.apache.commons.math3.linear.RealVector
  [v]
  (str (seq (.toArray v))))

(defmethod debug LinearObjectiveFunction
  [objective]
  (str (.getConstantTerm objective) ", " (debug (.getCoefficients objective))))

(defmethod debug LinearConstraint
  [c]
  (str (debug (.getCoefficients c)) " " (debug (.getRelationship c)) " " (.getValue c)))

(defmethod debug LinearConstraintSet
  [cs]
  (str (str/join "\n" (map debug (.getConstraints cs)))))

(defn- print-cm-debug-info
  [[objective constraints]]
  (println "======")
  (println "raw objective:" (debug objective))
  (doseq [constraint (.getConstraints constraints)]
    (println "raw constraint:" (debug constraint))))

(defn- print-debug-info
  [objective constraints]
  (println "======")
  (println "objective:" (debug objective))
  (doseq [constraint constraints]
    (println "constraint:" (debug constraint))))

(defn- build-objective
  "Build a LinearObjectiveFunction from a normalized
  p/LinearPolynomial, with coefficients ordered by the supplied
  ordered key sequence."
  [poly key-sequence]
  (LinearObjectiveFunction. (double-array (map (p/variables poly) key-sequence))
                            (double (p/constant poly))))

(def relationships {'= Relationship/EQ
                    '<= Relationship/LEQ
                    '>= Relationship/GEQ})

(defn- build-constraints
  "Build a LinearConstraintSet from the provided p/Constraints, with
  coefficients ordered by the supplied ordered key sequence."
  [constraints key-sequence]
  (LinearConstraintSet.
   (map (fn [constraint]
          (LinearConstraint. (double-array (map (p/variables (p/polynomial constraint))
                                                key-sequence))
                             (relationships (p/relation constraint))
                             (double (* -1 (p/constant (p/polynomial constraint))))))
        constraints)))

(def defaults
  "Default options for constructing a SimplexSolver."
  {:epsilon 1.0e-6
   :max-ulps 10
   :cutoff 1.0e-12})

(defn solver
  "Return  an  implementation  of  Solver using  the  2-stage  Simplex
  algorithm provided by Apache Commons Math.

  Optionally takes an options map containing the following keys:

  :epsilon - Amount of error to accept for algorithm convergence.
  :max-ulps - Amount of error to accept in floating point comparisons.
  :cutoff - Values smaller than the cutOff are treated as zero."
  ([] (solver {}))
  ([options]
     (reify p/Solver
       (optimize [_ objective constraints minimize?]
         (try
           (let [opts (merge defaults options)
                 solver (SimplexSolver. (:epsilon opts) (:max-ulps opts) (:cutoff opts))
                 zero (poly/zero (reduce set/union
                                         (keys (p/variables objective))
                                         (map (comp set keys p/variables p/polynomial)
                                              constraints)))
                 coefficient-ordering (keys (p/variables zero))
                 normalized-objective (poly/add zero objective)
                 normalized-constraints (map (fn [constraint]
                                               (p/constraint (p/relation constraint)
                                                             (poly/add zero (p/polynomial constraint))))
                                             constraints)
                 optimization-data [(build-objective normalized-objective coefficient-ordering)
                                    (build-constraints normalized-constraints coefficient-ordering)
                                    (if minimize? GoalType/MINIMIZE GoalType/MAXIMIZE)]
                 _ (when *debug* (print-debug-info normalized-objective normalized-constraints))
                 _ (when *debug* (print-cm-debug-info optimization-data))
                 solution (.optimize solver (into-array OptimizationData optimization-data))]
             (zipmap (keys (p/variables zero))
                     (.getPoint solution)))
           (catch UnboundedSolutionException e
             (throw (ex-info "Unbounded solution" {:reason :unbounded
                                                   :objective objective
                                                   :constraints constraints
                                                   :minimize? minimize?} e)))
           (catch NoFeasibleSolutionException e
             (throw (ex-info "No solution" {:reason :no-solution
                                            :objective objective
                                            :constraints constraints
                                            :minimize? minimize?} e))))))))


