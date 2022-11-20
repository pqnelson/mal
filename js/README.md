I was curious whether we could make a lightweight Clojurescript that's
more lispy in nature than Clojurescript.

At the same time, could we have something that's tightly coupled to
native Javascript? Rather than have a `class String`, could I just _use_
strings? Specifically, could we _compile_ to Lisp to Javascript while
preserving some degree of introspection?

Right now, I am following [MAL](https://github.com/kanaka/mal/) and am
working on [step 9: try](https://github.com/kanaka/mal/blob/master/process/guide.md#step-9-try)
[i.e., exception handling].

I have the evaluator defined in the `step1.js`, `step2.js`,
etc. files; I have implemented macros in `step8.js`.

I've tried to write JSDoc for most of the code, and I have written an
XUnit testing framework sufficient for basic testing.

# Tl;dr

Just open up mal.html and type in your code, then hit "compile" or "run"
(or whatever I named the button). The output will be printed below (if
any).

# Notes on the Design

S-expressions, when evaluated, will go through the various special
forms, then the default case assumes it is a function. There are two
types of functions: interpreted functions constructed using `fn*`, and
native functions.

We can "hack" this part of the evaluator to have a new type behave like
a function by adding an `apply(self, args)` method. Here the `args`
parameter behaves like `arguments` object in Javascript.

The only alternative I could think of would be something like Sean Lee's
[callable](https://github.com/sleexyz/callable) hack.
