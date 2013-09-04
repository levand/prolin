(ns prolin.protocols)

(defprotocol LinearPolynomial
  "Representation of a linear polynomial (a polynomial of degree
  one). A linear polynomial consists of:

   - Any number of variables, each with a numerical coefficient
   - A constant numerical term

   The keys representing variables can be any type that supports good equality semantics."

  (variables [this] "Return a map of variable identifiers to coefficients.")
  (constant [this] "Returns the constant term of the polynomial"))

(extend-protocol LinearPolynomial
  clojure.lang.APersistentMap
  (variables [this] (:v this))
  (constant [this] (:c this)))

(defn linear-polynomial
  "Construct a linear polynomial, given a constant and variables map"
  [constant variables]
  {:c constant
   :v variables})

(defprotocol Constraint
  "Representation of a linear constraint as an (in)equality"
  (relation [this] "Return one of '>=, '<=, or '=")
  (polynomial [this]
    "Returns a LinearPolynomial representing the variables,
    coefficients and constant term of the (in)equality, when it is put
    in one of the forms:

    a[0]x[0] + ... + a[n]x[n] + c = 0
    a[0]x[0] + ... + a[n]x[n] + c <= 0
    a[0]x[0] + ... + a[n]x[n] + c >= 0

    Any linear (in)equality can be algebraically manipulated to this
    form without any loss of generality, and it is this form that is
    used to represent all linear constraints internally. See
    'prolin.polynomial/subtract' for a function to help transform
    arbitrary (in)equalities to this format."))

(extend-protocol Constraint
  clojure.lang.APersistentMap
  (relation [this] (:r this))
  (polynomial [this] (:p this)))

(defn constraint
  "Construct a constraint, given a relation and a polynomial."
  [relation polynomial]
  {:r relation :p polynomial})

(defprotocol Solver
  "An implementation of a linear programming solver"
  (optimize [this objective constraints minimize?]
    "Maximize or minimize the given objective polynomial, subject to
     the provided set of LinearConstraints. Pass true as the third
     argument to minimize instead of maximize. Return a variables
     mapping representing the assignemtn of each variable present in
     the objective and constraints.

     If there is no solution matching the constraints, throws an
     ex-info with a :reason key of :no-solution.

     If the solution is unbounded by the provided constraints, throws
     an ex-info with a :reason key of :unbounded."))

