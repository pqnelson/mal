# Naming Conventions

Clojure has thought longer about
[munging](https://github.com/clojure/clojure/blob/e6fce5a42ba78fadcde00186c0b0c3cd00f45435/src/jvm/clojure/lang/Compiler.java#L2846-L2871)
special characters, perhaps I should adopt their naming convention. I
started doing this with atom methods.

# Docstrings, Printing Source

Clojure allows you to access the documentation for a function (or
variable) *and* the sourcecode, using `(doc foo)` and
`(source baz)` respectively. The [`source`](https://github.com/clojure/clojure/blob/3b6256e654bf250ddfd01cdaa4be9f39a74c2de6/src/clj/clojure/repl.clj#L172)
function seems doable, and if we stick the documentation in the
metadata...well that simplifies life considerably.

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

## Functions

Just looking through the sourcecode for the Compiler in Clojure, it
seems the primitive version of a function is `fn*`, which is implemented
as a [`public static class FnMethod extends ObjMethod`](https://github.com/clojure/clojure/blob/e6fce5a42ba78fadcde00186c0b0c3cd00f45435/src/jvm/clojure/lang/Compiler.java#L5289-L5768),
according to the `FnMethod::parse` method.

It's a mess, really, it seems the [`FnExpr`](https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/Compiler.java#L3904-L4150)
class, which uses `FnMethod` as an encoding of the different signatures
for a `fn*`; i.e., `(fn* ([arg1] method1) ([arg2] method2) ...)`
stores each `([arg1] method1)`, `([arg2] method2)`, ..., in their own
`FnMethod` object. The `FnExpr` is then compiled to a Java class.

### Multi-arity functions in Clojurescript

Running on the figwheel repl the commands:

```cljs
(require 'cljs.js)
(def st (cljs.js/empty-state))
(cljs.js/compile-str st "(defn foo ([a] (+ a 1)) ([a b] (* a b)))" println)
```

Produces the following javascript:

```js
(function cljs$user$foo(var_args) {
    var G__23 = arguments.length;
    switch (G__23) {
    case (1):
        return cljs.user.foo.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));
        break;
    case (2):
        return cljs.user.foo.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));
        break;
    default:
        throw (new Error(["Invalid arity: ",cljs.core.str.cljs$core$IFn$_invoke$arity$1(arguments.length)].join('')));
    }
});

(cljs.user.foo.cljs$core$IFn$_invoke$arity$1 = (function (a){
return (a + (1));
}));

(cljs.user.foo.cljs$core$IFn$_invoke$arity$2 = (function (a,b){
return (a * b);
}));

(cljs.user.foo.cljs$lang$maxFixedArity = (2));
```

### Thoughts

Presumably a cleaner way, for certain functions, would be

```clj
(defn foo
 ([] (foo const1 const2 const3))
 ([x1]   (foo x1 const2 const3))
 ([x1 x2]    (foo x1 x2 const3))
 ([x1 x2 x3]
   body))
```

compiled as

```js
function f(x1, x2, x3) {
  x1 = x1 || const1;
  x2 = x2 || const2;
  x3 = x3 || const3;
  /* insert compile(body) here */
}
```

### Destructuring

I'm just going to list some examples and their translations.

- Clojure: `(defn foo [{:keys [a b c]}] ...)`

  JS: `function foo ({a,b,c}) {...}`
- Clojure: `(defn foo [[a b & friends]] ...)`

  JS: `function foo ([a, b, ...friends]) {...}`
- Clojure: `(defn foo [{employer :company, name :name}] ...)`

  JS: `function foo ({company: employer, name: name}) {...}`

The caveat is there seems to be no parallel or counterpart to Clojure's
`:as` when destructuring. For example,

```clojure
(defn process-client [{name :name :as all} client]
  (println "Name" name "comes from the first argument:" all)
  (println "The second argument is `client` which is:" client))
```

I think we'd need to do something like

```js
function process_client ({name}, client) {
  var all = arguments[0];
  // ...and then translate the function body...
}
```

## Data Structures

We don't have any hash maps, vectors, or sets. These would probably be
nice to have.

Is there any reason to have a custom hash map? JSON objects allow only
strings for its keys (or, if `Symbol` is supported, those too). If you
want anything else, you'd need a custom hash map.

Actually, a good reason for having our own hash map class is because
some web browsers have memory leaks when trying to get the keys for an
object. This is mentioned in Michael Bolin's _Closure_ book, Internet
Explorer caused so many problems.

## Classes

Can we have a `defclass` macro (which generates a constructor, plus some
docstrings...or whatever) and `defmethod` to do some basic
prototyping. Then, e.g., overloading the `toString` method will change
how the instances of the class are printed.

# More Complete Testing

I wrote a poor man's XUnit testing framework, which works fine enough. I
should write more tests.

# Interoperability with Javascript

Interoperability with Javascript seems like a good idea.

## JSON Objects

About interoperability notation,
Clojurescript uses `js#{ ... }` notation for JSON objects.

## Javascript ES6 `Symbol`

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
since we have an evaluator. The main disadvantage is a complete lack of
optimizers.

# Vars

Clojure uses [`Var`](https://clojure.org/reference/vars)
to bind symbols to values.
The [implementation](https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/Var.java)
may be worth looking at.

Is this a good idea, or is it over-engineering for a Javascript Lisp?
The advantage in Clojure is that `Var` can be rebound within a thread,
without affecting the other threads. But Javascript is (not yet)
multithreaded, not in the same way as Java.

# Namespaces

I'm still thinking about namespaces. Clojure uses a namespace as a
mapping from `Var` to values. For the evaluator, we could treat a
namespace as "just another JSON object", and create it by something like
`window["my-new-namespace"][symbol_in_ns] = value`.

(The reason this works: [`window`](https://developer.mozilla.org/en-US/docs/Web/API/Window) accesses global bindings, see, e.g.,
[thread](https://stackoverflow.com/q/1920867) on stackoverflow.)
Care must be taken, apparently Node.js does not use `window` as a global
object but instead uses a `global` variable, see
[global objects in Javascript](https://developer.mozilla.org/en-US/docs/Glossary/Global_object).

Though if we compile to Javascript, then we may want to compile a
namespace to an object using the [module design pattern](https://github.com/getify/You-Dont-Know-JS/blob/2nd-ed/scope-closures/ch8.md).
