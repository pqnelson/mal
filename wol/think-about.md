- [ ] Think about various arities for each function, e.g.,
      `(fn* ([x] ...) ([x y] ...))`
- [ ] Add a `defn`, `defn-` special forms for convenience
- [ ] Research compiling macros further.
  - Emacs does macro expansion at compile time [14.3](https://www.gnu.org/software/emacs/manual/html_node/elisp/Compiling-Macros.html)

# Functions

In Javascript 1.2, what happens with a function declaration of the form:

```js
function foo(...args) {
  // do stuff
}
```

The function body is stored, uninterpreted, associated with the property
`foo` in the current context (either globally (if this is a top-level
declaration) or as a property of whatever environment has this occur).
This has the danger that it could be overwritten _pre-emptively_ by
accident. For example,

```js
var f = 0;
function f() {
  return 44;
}
console.log(f); /* prints "0" since the var f = 0 overwrites the
function associated with the property name "f" */
```

What's going on here? Well, the function has been "hoisted", which is
the technical term for the following process: Javascript pretends the
function declarations occur at the start of the file. That is, hoisting
makes Javascript think the following occurs:

```js
function f() {
  return 44;
}
var f = 0;
console.log(f);
```

This is problematic. On the other hand, it's handy when you want to use
a function **before** it's declared.

Since Clojure is a single-pass compiler, you need to declare functions
if you want to use them before defining them. This would suggest we
should use something like:

```js
const f = function(...args) {
  // snip
}
```


