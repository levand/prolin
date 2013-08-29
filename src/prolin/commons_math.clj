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

(defn- build-objective
  "Build a LinearObjectiveFunction from a normalized p/LinearPolynomial"
  [poly]
  (LinearObjectiveFunction. (double-array (vals (p/variables poly)))
                            (double (p/constant poly))))

(def relationships {'= Relationship/EQ
                    '<= Relationship/LEQ
                    '>= Relationship/GEQ})

(defn- build-constraints
  "Build a LinearConstraintSet from the provided p/Constraints"
  [constraints]
  (LinearConstraintSet.
   (map (fn [constraint]
          (LinearConstraint. (double-array (vals (p/variables (p/polynomial constraint))))
                             (relationships (p/relation constraint))
                             (double (* -1 (p/constant (p/polynomial constraint))))))
        constraints)))

(def defaults
  "Default options for constructing a SimplexSolver."
  {:epsilon 1.0e-6
   :max-ulps 10
   :cutoff 1.0e-12})

(defmulti debug class)

(defmethod debug :default
  [o]
  (str o))

(defmethod debug org.apache.commons.math3.linear.RealVector
  [v]
  (str (seq (.toArray v))))

(defmethod debug LinearObjectiveFunction
  [objective]
  (str "objective: " (.getConstantTerm objective) ", " (debug (.getCoefficients objective))))

(defmethod debug LinearConstraint
  [c]
  (str "constraint: " (debug (.getCoefficients c)) " " (debug (.getRelationship c)) " " (.getValue c)))

(defmethod debug LinearConstraintSet
  [cs]
  (str (str/join "\n" (map debug (.getConstraints cs)))))


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
                 normalized-objective (poly/add zero objective)
                 normalized-constraints (map (fn [constraint]
                                               (p/constraint (p/relation constraint)
                                                             (poly/add zero (p/polynomial constraint))))
                                             constraints)
                 optimization-data [(build-objective normalized-objective)
                                    (build-constraints normalized-constraints)
                                    (if minimize? GoalType/MINIMIZE GoalType/MAXIMIZE)]
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


