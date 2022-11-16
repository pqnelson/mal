# Naming Conventions

Clojure has thought longer about
[munging](https://github.com/clojure/clojure/blob/e6fce5a42ba78fadcde00186c0b0c3cd00f45435/src/jvm/clojure/lang/Compiler.java#L2846-L2871)
special characters, perhaps I should adopt their naming convention. I
started doing this with atom methods.

# Standard Library

There is no "prelude library", no "core.lisp". We should probably
implement it. Some useful functions off the top of my head:

- [ ] `def`
  - [ ] docstrings
  - [ ] metadata
  - [ ] private
- [ ] `fn`
  - destructuring...can we piggieback off of Javascript's [destructuring](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Destructuring_assignment)?
- [ ] `defn` and `defn-`
- [ ] `apply`
- [ ] `map`
  - Although Javascript's [`map`](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/map)
    is probably optimal for a single collection, it doesn't work for the
    case with multiple collections, like `(map list (range) my-coll)`
- [ ] `mapcat`
- [ ] `reduce`
  - Javascript's `reduceRight` is probably optimized
- [ ] `filter`
  - Again, Javascript's [`filter`](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array/filter)
    is probably optimized.
- [ ] `partition`
- [ ] `partition-all`
- [ ] `partition-by`
- [ ] `zipmap`
- [ ] String related functions (index-of, last-index-of, substring,
      etc.)
- [ ] `spit`


## Data Structures

We don't have any hash maps, vectors, or sets. These would probably be
nice to have.

Is there any reason to have a custom hash map? I don't know.
Clojurescript uses `js#{ ... }` notation for JSON objects.

## Classes

Can we have a `defclass` macro (which generates a constructor, plus some
docstrings...or whatever) and `defmethod` to do some basic
prototyping. Then, e.g., overloading the `toString` method will change
how the instances of the class are printed.

# Javascript Symbols

ES6 introduced symbols as a way to avoid name collisions. They seem to
resemble Common Lisp keywords (like `#:foo`), as opposed to being used
like Lisp symbols.

- Jason Orendorff, [ES6 In Depth: Symbols](https://hacks.mozilla.org/2015/06/es6-in-depth-symbols/)
  blogpost dated June 11, 2015.

# Compiler Structure

So far, we have written an evaluator.
There is no infrastructure for emitting Javascript code, nor any
optimizer.
We could probably implement this in the object language (i.e., in Lisp)
since we have an evaluator.
