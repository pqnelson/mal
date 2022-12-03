- [ ] Add more documentation
- [ ] Implement `Atom`
- [ ] Add an `Emit` class to emit Javascript for some Lisp code.
- [ ] The reader should convert tokens to expressions in a better
      manner, specifically without being forced to carry around tokens
      forever. We should stick the position information in the metadata.
- [X] Write up some core functions...like `with-meta`...
  - [X] `vector` and  `vector?`
  - [X] `hash-map` and `map?`
  - [X] `assoc` and `dissoc` (I included a destructive "in-place"
        version of both of these, `assoc!` and `dissoc!`, in case you
        want to shoot yourself in the foot)
  - [X] `get`
  - [X] `contains?`
  - [X] `keys` and `vals`
- [X] Treat `(def ^:meta x ...)` as `(def (with-meta :meta x) ...)`
- [X] Support HashMaps
- [X] Support `IObj` (metadata setter and getter)
  - [X] Seq
  - [X] Vector
  - [X] Symbol
  - [X] Fun
  - [X] Map
- [X] Unit test the evaluator thoroughly (or, at all)
  - [X] Test `keys` and `vals`
  - [X] Test functions more, much much more.
  - [X] `let`
  - [X] `if`
  - [X] `macroexpand`
  - [X] `quasiquote-expand`
  - [X] `try`
    - I think we can refactor out the `catch` logic in a separate
      function, because it's way too complicated for one single block.
