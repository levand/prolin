# prolin

Prolin is a linear programming library for Clojure. It provides
idiomatic Clojure APIs to formulate and solve linear programming
problems, and a small set of utilties for some common transformations
of LP objective functions and constraints.

- http://en.wikipedia.org/wiki/Linear_programming

It uses the two-phase Simplex algorithm provided by Apache Commons Math
internally as its core LP solver.

- http://en.wikipedia.org/wiki/Simplex_algorithm
- http://commons.apache.org/proper/commons-math/userguide/optimization.html
- http://google-opensource.blogspot.com/2009/06/introducing-apache-commons-math.html

There are several value propositions to using Prolin over Commons Math directly:

- Idiomatic Clojure API
- Allows the use of arbitrary values (anything with equality
  semantics) to identify variables
- Modular, protocol-based design to allow for easy extension to
  alternative solver implementations or alternative representations of
  constraints and polynomials.

## Usage

The API is centered around a few key protocols.

#### Polynomials

An instance of `prolin.protocols.LinearPolynomial` represents a linear
polynomial. LinearPolynomials are a key building block of linear
programming.

```clojure
(defprotocol LinearPolynomial
  "Representation of a linear polynomial (a polynomial of degree
  one). A linear polynomial consists of:

   - Any number of variables, each with a numerical coefficient
   - A constant numerical term

   The keys representing variables can be any type that supports good
   equality semantics."

  (variables [this] "Return a map of variable identifiers to coefficients.")
  (constant [this] "Returns the constant term of the polynomial"))
```

A `prolin.protocols/linear-polynomial` function is provided to construct a
LinearPolynomial from a constant number and a variables map.

An implementation of `LinearPolynomial` for `java.lang.String` is
provided, allowing strings such as `"x + y - 4"` or `"3x + 4y - 2z"`
to be used anywhere you want a polynomial. Note that the parser is not
sophisticated and is provided mostly for experimentation and testing;
it will only work for basic equations or inequalities (not, for
example, equations with parenthesis, multiple constant terms, etc.)

The `prolin.polynomial` namespace contains utility functions for:

- Adding polynomials
- Subtracting polynomials
- Multiplying polynomials by a scalar
- Constructing a 'zero' polynomial with the given variables
- Instantiating a polynomial by plugging in numbers for each of its variables

#### Constraints

Linear constraints are linear equalities or inequalities that are used
to restrict the 'feasible region' of a linear programming problem.

Constraints are represented as the `prolin.protocols.Constraint`
protocol, to allow callers to define implementations that are the best
fit for a particular problem.

```clojure
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
```

A `prolin.protocols/constraint` constructor is also provided, to construct a constraint
directly from a polynomial and its relation to 0.

Additionally, `Constraint` is extended to `java.lang.String` to allow
Strings such as `"x = y"`, `"3x + y => 4"` to be used anywhere you
want a Constraint. Again, note that the parsing of such strings is
naive and intended only for experimentation and testing.


#### Solving

A solution algorithm is provided by an instance of the
`prolin.protocols.Solver` protocol.

Only one implementation of `Solver` is currently provided; one based
on the Apache Commons Math `SimplexSolver`. You can obtain an instance
of this solver by calling `prolin.commons-math/solver`. `solver`
optionally takes an options map containing the following keys:

```
:epsilon - Amount of error to accept for algorithm convergence. (double value)
:max-ulps - Amount of error to accept in floating point comparisons. (int value)
:cutoff - Values smaller than the cutOff are treated as zero." (double value)
```

If you wish to implement `Solver` for an alternative linear
programming solver or algorithm, see the protocol definition in
`prolin.protocols`.

Once you have an instance of `Solver`, you can invoke the
`prolin/optimize` function, which takes a solver, an objective
function (as a `LinearPolynomial`), a collection of `Constraints`, and
a boolean (true to minimize the objective, false to maximize it.)

The `prolin/maximize` and `prolin/minimize` functions have the same
signature, but eliminating the final boolean flag.

### Examples

```clojure

(require '[prolin :as p])
(require '[prolin.protocols :as pp])
(require '[prolin.commons-math :as cm])

;; Maximize x
(p/optimize (cm/solver) "x" #{"x <= 5", "x >= -2"} false)
;; => {"x" 5.0}

;; Same as above
(p/maximize (cm/solver) "x" #{"x <= 5", "x >= -2"})
;; => {"x" 5.0}

;; Now minimizing
(p/minimize (cm/solver) "x" #{"x <= 5", "x >= -2"})
;; => {"x" -2.0}

;; Using more than one variable
(p/maximize (cm/solver) "x" #{"2x = y", "y <= 5" })
;; => {"x" 5.0, "y" 2.5}

;; Same as above, but constructing objective & constraints directly,
;; instead of using the String implementations
(p/maximize (cm/solver)
            (pp/linear-polynomial 0 {:x 1})
            #{(pp/constraint '= (pp/linear-polynomial 0 {:x 2 :y -1}))
              (pp/constraint '<= (pp/linear-polynomial -5 {:y 1}))})
;; => {:x 5.0, :y 2.5}

;; Throws an ex-info with a :reason of :no-solution if it can't be solved
(p/maximize (cm/solver) "x" #{"x = 3", "x = 4" })
;; => Exception!

;; Throws an ex-info with a :reason of :unbounded if the solution
;; is unconstrainted
(p/maximize (cm/solver) "x" #{"x >= 1"})
;; => Exception!

```

## Development

To run the `clojure.test` tests, run `lein test` from the project
directory.

To run the `clojure.test.generative` tests, run `lein with-profile
test generative`.

## License

Copyright © 2013 Luke VanderHart

Distributed under the Eclipse Public License, the same as Clojure.
