(ns prolin.commons-math
  (:require [prolin.protocols :as p]
            [clojure.set :as set])
  (:import [org.apache.commons.math3.optim.linear
            SimplexSolver
            LinearObjectiveFunction
            LinearConstraint
            LinearConstraintSet
            Relationship]
           [org.apache.commons.math3.optim.nonlinear.scalar GoalType]
           [org.apache.commons.math3.optim OptimizationData]))

(def defaults
  "Default options for constructing a SimplexSolver."
  {:epsilon 1.0e-6
   :max-ulps 10
   :cutoff 1.0e-12})


#_(defn solver
  "Return  an  implementation  of  Solver using  the  2-stage  Simplex
  algorithm provided by Apache Commons Math.

  You may pass a map containing following options:

  :epsilon - Amount of error to accept for algorithm convergence.
  :max-ulps - Amount of error to accept in floating point comparisons.
  :cutoff - Values smaller than the cutOff are treated as zero."
  [options]
  (reify p/Solver
    (optimize [objective constraints minimize?]
      (let [opts (merge defaults options)
            solver (SimplexSolver. (:epsilon opts) (:max-ulps opts) (:cutoff opts))



            solution (.optimize solver (into-array OptimizationData [commons-objective
                                                                     (LinearConstraintSet. commons-constraints)
                                                                     (if minimize? GoalType/MINIMIZE GoalType/MAXIMIZE)]))]
        
))))

