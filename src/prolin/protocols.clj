(ns prolin.protocols)

(defprotocol LinearPolynomial
  "Representation of a linear polynomial (a polynomial of degree
  one). A linear polynomial consists of:

   - Any number of variables, each with a numerical coefficient
   - A constant numerical term

   The keys representing variables can be any type that supports good equality semantics."

  (variables [this] "Return a map of variables to their coefficients.")
  (constant [this] "Returns the constant term of the polynomial"))

(extend-protocol LinearPolynomial
  clojure.lang.IPersistentMap
  (constant [this] (::constant this))
  (coefficients [this] (dissoc this ::constant)))

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
    'subtract-polynomial' for a function to help transform (in)equalities
    to this format."))


