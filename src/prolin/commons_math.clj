(ns prolin.commons-math
  (:require [prolin.protocols :as p])
  (:import [org.apache.commons.math3.optim.linear
            SimplexSolver
            LinearObjectiveFunction
            LinearConstraint
            LinearConstraintSet
            Relationship]
           [org.apache.commons.math3.optim.nonlinear.scalar GoalType]
           [org.apache.commons.math3.optim OptimizationData]))

(defn- c->da)

(defn constraint
  "Build a LinearConstraint from a p/Constraint"
  [constraint]
  (let [{a :constraints c :constant s :sign} (p/normalize constraint)
        constraintset (map )]
    
    )
  (LinearConstraint. ))
