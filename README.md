# Introduction

This is a collection of small C programs which serve as an
exploration of Lisp. There are many different types of lisps and
many avenues for exploration. For example, are we drawing
inspiration from Scheme, Common Lisp, or Clojure? This affects the
choice of primitive elements and desired features (e.g., only
Common Lisp would need Reader macros).

We also have considerations with respect to implementing a
_compiler_ emitting either assembly code, or machine code directly,
as opposed to an _interpreter_. I suppose one compromise may be
using a _virtual machine_ and having our investigation emit
bytecode for that virtual machine.

## The basic steps

For a minimalist approach inspired from Clojure, we outline the
steps in [making a Lisp](https://github.com/kanaka/mal).

- [X] Step 0: basic tokenizer for S-expressions.
- [X] Step 1: basic reader and printer for Lisp expressions.
  - [X] Step 1-0: basic reader with only "symbols" (atoms) and lists.
  - [X] Step 1-1: add support for integers, floats; comments ignored.
  - [X] Step 1-2: add support for strings.
  - [X] Step 1-3: support booleans (at least `t` as a special
        symbol). It suffices to have a global constant symbol `t`,
        but honest booleans may be better.
- [ ] Step 2: basic evaluator for Lisp expressions, just for
      arithmetic.
  - [X] Add basic refcount memory management.
  - [X] Basic environment data structure.
  - [ ] Evaluator.
- [ ] Step 3: add an environment (so we can support definitions in the
  future). This includes `(let ...)` and `(def! ...)` special forms
  (which updates the environment).
- [ ] Step 4: Basic special forms (`if`, `fn`, `do`).
- [ ] Step 5: Tail call optimization.
- [ ] Step 6: Add an `eval` function, `slurp` for reading a file into a
  string, etc.
- [ ] Step 7: Handle special forms like `quote`, `quasiquote`,
  `unquote`, `splice-unquote`. This requires supporting `cons` and
  `concat`. 
- [ ] Step 8: Macros.
- [ ] Step 9: Try-catch exception handling.
- [ ] Step A: Metadata, self-hosting, interoperability.

Scheme lovers could stop around step 7 with a few more modifications.

# Ground for Exploration

## First Explorations

**Vectors and Hashmaps.**
There are many ways to implement vectors (dynamic arrays) and
hashmaps, which should be provided.

**Reader Macros.**
If we were following Common Lisp's design philosophy, we would want
to implement reader macros and a proper reader. Basically, as the
scanner reads in each letter, it checks to see what "type" of
letter it is. Encountering a `(` indicates something special
(starting a list) as opposed to "just another letter". It triggers
a certain "reader macro" (function involving the reader). More
generally, we let the user define their own custom reader macros,
so they could write, say, `[:a 3 {:spam :eggs}]` for a vector with
three components, the third one being a hashmap.

Arguably, you _could_ implement a Scheme-like language with reader
macros, then change the reader macros to make the concrete syntax
more Clojure-like. (Or Common Lisp-like.)

**Packages.**
We never really implemented packages. There are subtle differences
between packages in Common Lisp and namespaces in Clojure (Clojure
namespaces are doubly indirect bindings, whereas packages in Common
Lisp are direct bindings). This requires reworking the environment
code a bit.

**Optimization: NaN Boxing.**
Another avenue worth exploring is NaN-boxing. Using IEEE-754
foating point, double precision is 64-bit. A NaN is when all bits
in the exponent are 1. Intel requires the 50th bit be nonzero, and
for the NaN to be "quiet" (as opposed to "signaling") we need the
51st bit to be 1. That's a total of 12 bits required, and 52 bits
free for our use. Typically we use 48 bits to encode a pointer, the
remaining 4 bits indicate what type the value _is_. 

## Grander Endeavors

**Object Model.**
Allow the user to define classes and subclasses.

**Multimethods and Pattern Matching.**
There are (at least) two ways to handle multimethods. One is the
way Common Lisp handles it, with `defgeneric` and `defmethod`. The
other is fancier, predicate dispatching (which is half-way between
Common Lisp multimethods and ML/Haskell/F# pattern matching).

**Type Theory Exploration.**
Using S-expressions has the advantage of abandoning syntax, giving
us greater freedom in investigating other dimensions. For example,
we could implement a [Haskell-like language (Liskell)](https://github.com/haskell-lisp/liskell).

**Compile to Assembly Code.**
Or, one could explore compilation further. Common Lisp has a rather
bloated compilation process. Is there some better way to compile to
assembly code?

