This is an attempt to implement a fragment of Common Lisp compiler and
repl which targets a virtual machine. The target is based on Landin's
SECD machine, but heavily influenced by Symbolics's Lisp Machine.

This is a collection of literate programs written in Knuth's CWEB. The programs:

- secd.w implements an SECD machine based on Henderson's book
- shecd.w implements an SECD machines with a heap, so there's
  mutability and so on
  
"Common code" is isolated to the `./w/` subdirectory, which contains
"vignettes" that are re-used in each of the 'top level' literate programs.