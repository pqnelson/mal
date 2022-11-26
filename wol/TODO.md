- [ ] Unit test the evaluator thoroughly (or, at all)
- [ ] Add more documentation
- [ ] Add an `Emit` class to emit Javascript for some Lisp code.
- [ ] Treat `(def ^:meta x ...)` as `(def (with-meta :meta x) ...)`
- [ ] Write up some core functions...like `with-meta`...
- [ ] The reader should convert tokens to expressions in a better
      manner, specifically without being forced to carry around tokens
      forever. We should stick the position information in the metadata.