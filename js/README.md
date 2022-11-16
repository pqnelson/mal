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
