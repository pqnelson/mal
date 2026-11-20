@ The SECD Machine is a stack machine with 4 ``registers'' (stacks):
\itemitem{(1)} \.{s} (stack) holds intermediate results (it's a list of expressions);
\itemitem{(2)} \.{e} (environment) holds values bound to variables;
\itemitem{(3)} \.{c} (control) holds the code being executed;
\itemitem{(4)} \.{d} (dump) stores the contents of other registers
temporarily when a function-call occurs (and restores them upon return).

There are other virtual machines, which could be viewed as variants of
the SECD Machine. For example, the CEK machine removes the dump \.{d}
from SECD and replaces it with continuations (and places parameters in
the environment \.{e} stack). The CESK machine uses a ``store'' [heap]
and pointers instead of copying objects into the environment \.{e}.
@^CEK Machine@>
@^CESK Machine@>

Peter Henderson's book {\sl Functional Programming: Application and Implementation} 
(Prentice Hall, 1980)
implements an SECD machine with about 15 opcodes in the book, and the
LispKit Lisp uses about 28 opcodes for their SECD virtual machine.
@^Henderson, Peter@>

@ The basic values in Lisp can be one of several types: characters,
fixed-width and arbitrary precision integers (``fixnum'' and
``bignum'', respectively), rational numbers, floating-point numbers (both single and double
precision), lists and \.{cons} cells, symbols, closures. Common Lisp also
includes arrays (including strings and vectors), hash tables,
readtables, packages, streams, structures.

Our virtual machine will need to determine some set of
primitives. What is a common set of primitive types for virtual machines?
{\it The Java\registeredtrademark{} Virtual Machine Specification: Java SE 8 Edition} (\S2.2) distinguishes
between primitive types
(\.{byte}, \.{short}, \.{int}, \.{long}, \.{char}, \.{float}, \.{double},
\.{boolean}, and \.{returnAddress} types) and reference types.

A first stab might be: integers, lists.

@ {\bf Remark: Note to self.}\enspace\ignorespaces%
The Symbolics I-Machine had \.{nil} stored at the address 1011000
and \.{t} stored at the address 1011005.

@ {\bf Lisp dialect: syntax.}\enspace\ignorespaces%
We will use a modified version of LispKit Lisp as our ``base
Lisp''. Following Chapter~4 of Henderson's book {\sl Functional Programming: Application and Implementation} 
(Prentice Hall, 1980), the syntax looks like:

$$\vbox{\offinterlineskip
\halign{ \hfil# &\strut\quad#\hfil\cr
 $x$ & a variable\cr
 {\tt (quote $s$)} & a constant\cr
%height2pt&\omit&\omit\cr
\noalign{\hrule}
%height2pt&\omit&\omit\cr
 {\tt (add $e_{1}$ $e_{2}$)} & \cr
 {\tt (sub $e_{1}$ $e_{2}$)} & \cr
 {\tt (mul $e_{1}$ $e_{2}$)} & arithmetic expressions \cr
 {\tt (div $e_{1}$ $e_{2}$)} & \cr
 {\tt (rem $e_{1}$ $e_{2}$)} & \cr
%height2pt&\omit&\omit\cr
\noalign{\hrule}
%height2pt&\omit&\omit\cr
 {\tt (eq $e_{1}$ $e_{2}$)} & equality\cr
 {\tt (leq $e_{1}$ $e_{2}$)} & partial ordering $e_{1}\leq e_{2}$\cr
%height2pt&\omit&\omit\cr
\noalign{\hrule}
%height2pt&\omit&\omit\cr
 {\tt (car $e$)} & get head of list\cr
 {\tt (cdr $e$)} & get tail of list\cr
 {\tt (cons $e_{1}$ $e_{2}$)} & form a list\cr
 {\tt (atom $e$)} & test if $e$ is atomic\cr
%height2pt&\omit&\omit\cr
\noalign{\hrule}
%height2pt&\omit&\omit\cr
 {\tt (if $e_{1}$ $e_{2}$ $e_{3}$)} & conditional forms\cr
 {\tt (lambda ($x_{1}$ $\cdots$ $x_{k}$) $e$)} & lambda expression\cr
 {\tt ($e$ $e_{1}$ $\cdots$ $e_{k}$)} & function call\cr
 {\tt (let (($x_{1}$ $e_{1}$) $\cdots$ ($x_{k}$ $e_{k}$)) $e$)} & simple block\cr
 {\tt (letrec (($x_{1}$ $e_{1}$) $\cdots$ ($x_{k}$ $e_{k}$)) $e$)} & recursive block\cr}}$$

@ {\bf Lisp semantics.}\enspace\ignorespaces%
The semantics for this Lisp dialect may be described using
an \\{eval}(\\{expr}, $n$, $v$) function, where $n$ is a ``name list''
(a linked list of lists of identifiers)
and $v$ is a ``value list'' (a linked list of lists of values). We
need a few helper functions first:

{
\&{function} \\{assoc}($x$, $n$, $v$) := \1\6
\&{if} \\{member}($x$, \\{car}($n$))\6
\&{then} \\{locate}($x$, \\{car}($n$), \\{car}($v$))\6
\&{else} \\{assoc}($x$, \\{cdr}($n$), \\{cdr}($v$))\2\7
\&{function} \\{locate}($x$, $\ell$, $m$) :=\1\6
\&{if} \\{eq}($x$, \\{car}($\ell$))\6
\&{then} \\{car}($m$)\6
\&{else} \\{locate}($x$, \\{cdr}($\ell$), \\{cdr}($m$))\2\7
\&{function} \\{member}($x$, $\ell$) :=\1\6
\&{if} \\{eq}($\ell$, \.{nil})\6
\&{then} \&{false}\6
\&{else} \&{if} \\{eq}($x$, \\{car}($\ell$))\6
\&{else} \\{member}($x$, \\{cdr}($\ell$))\2
\par}

@ Now, the basic rules look like (we freely rely on informal functions
in the metalanguage like \\{cons}, \\{head}, \\{tail}, etc.):

\bull
\\{eval}($e$, $n$, $v$) = \\{assoc}($e$, $n$, $v$) when $e$ is an atom.

\bull
\\{eval}({\tt (car $e$)}, $n$, $v$) = \\{head}(\\{eval}($e$, $n$, $v$))

\bull
\\{eval}({\tt (cdr $e$)}, $n$, $v$) = \\{tail}(\\{eval}($e$, $n$, $v$))

\bull
\\{eval}({\tt (atom $e$)}, $n$, $v$) = \\{is\_atom}(\\{eval}($e$, $n$, $v$))

\bull
\\{eval}({\tt (cons $e_{1}$ $e_{2}$)}, $n$, $v$) =
\\{cons}(\\{eval}($e_{1}$, $n$, $v$), \\{eval}($e_{2}$, $n$, $v$))

\bull
\\{eval}({\tt (eq \\{lhs} \\{rhs})}, $n$, $v$) =
\\{eq}(\\{eval}(\\{lhs}, $n$, $v$), \\{eval}(\\{rhs}, $n$, $v$))

\bull
\\{eval}({\tt (if $e$ $e_{1}$ $e_{2}$)}, $n$, $v$) =
\&{if}(\\{eval}($e$, $n$, $v$)) \&{then} \\{eval}($e_{1}$, $n$, $v$)
\&{else} \\{eval}($e_{2}$, $n$, $v$)

\bull
\\{eval}({\tt (lambda \\{args} $e$)}, $n$, $v$) = \&{let} \\{env} = \\{cons}($n$, $v$)
\&{and} \\{clos} = \\{cons}(\\{args}, $e$)
\&{in} \\{cons}(\\{clos}, \\{env}) \&{end};

It's important to note that this is how the LispKit semantically
represents a closure.

\bull
\\{eval}({\tt (let \\{decls} $e$)}, $n$, $v$) =
\\{eval}($e$, \\{cons}(\\{vars}, $n$), \\{cons}(\\{exprs}, $v$))

\&{where} \\{vars} = \\{get\_vars}(\\{decls})

\&{and}
\\{exprs} = \\{evlist}(\\{get\_exprs}(\\{decls}), $n$, $v$)

\&{and}
\\{get\_vars}(d) = \&{if} $d$ = \.{nil} \&{then} \.{nil}
\&{else} \\{cons}(\\{car}(\\{car}($d$)),\\{get\_vars}(\\{cdr}($d$)))

\&{and} \\{get\_exprs}($d$) = \&{if} $d$ = \.{nil} \&{then} \.{nil}
\&{else} \\{cons}(\\{cdr}(\\{car}($d$)),\\{get\_vars}(\\{cdr}($d$)))

\&{and} \\{evlist}($\ell$, $n$, $v$) = \&{if} $\ell$ = \.{nil} \&{then}
\.{nil} \&{else} \\{cons}(\\{eval}(\\{car}($\ell$), $n$, $v$), \\{evlist}(\\{cdr}($\ell$),$n$,$v$)

\bull
\\{eval}({\tt (letrec \\{decls} $e$)}, $n$, $v$) =
\\{eval}($e$, $n'$, \\{replaca}($v'$, \\{exprs}))

\&{where} $v'$ = \\{cons}(\&{pending}, $v$) \&{and} $n'$ = \\{cons}(\\{vars}, $n$)

\&{and}
\\{vars} = \\{get\_vars}(\\{decls})

\&{and}
\\{exprs} = \\{evlist}(\\{get\_exprs}(\\{decls}), $n'$, $v'$)

\&{and}
\\{get\_vars}(d) = \&{if} $d$ = \.{nil} \&{then} \.{nil}
\&{else} \\{cons}(\\{car}(\\{car}($d$)),\\{get\_vars}(\\{cdr}($d$)))

\&{and} \\{get\_exprs}($d$) = \&{if} $d$ = \.{nil} \&{then} \.{nil}
\&{else} \\{cons}(\\{cdr}(\\{car}($d$)),\\{get\_vars}(\\{cdr}($d$)))

\&{and} \\{evlist}($\ell$, $n$, $v$) = \&{if} $\ell$ = \.{nil} \&{then}
\.{nil} \&{else} \\{cons}(\\{eval}(\\{car}($\ell$), $n$, $v$), \\{evlist}(\\{cdr}($\ell$),$n$,$v$)

\&{and} \\{rplaca}($x$, $y$) = \&{setq}(\\{car}($x$), $y$) mutates
the \\{car}($x$) to be $y$.

\bull
\\{eval}({\tt ($e_{1}$ {.\spacefactor1000} $e_{2}$)}, $n$, $v$) =
\\{eval}(\\{cdr}(\\{clos}), $n'$, $v'$), (``applying a function'')

\&{where} $n'$ = \\{cons}(\\{car}(\\{clos}), \\{car}(\\{env})) \&{and}
$v'$ = \\{cons}($z$, \\{cdr}(\\{env}))

\&{and} (\\{clos}, \\{env}) = \\{eval}($e_{1}$, $n$, $v$)

\&{and} $z$ = \\{evlist}($e_{2}$, $n$, $v$)

\&{and} \\{evlist}($\ell$, $n$, $v$) = \&{if} $\ell$ = \.{nil} \&{then}
\.{nil} \&{else} \\{cons}(\\{eval}(\\{car}($\ell$), $n$, $v$), \\{evlist}(\\{cdr}($\ell$),$n$,$v$)


\bigskip
\noindent We can also define an \\{apply} function (as is classic in Lisp):
\bull
\\{apply}($f$, $x$) = \\{eval}(\\{cdr}(\\{clos}), $n$, $v$)

\&{where} $n$ = \\{cons}(\\{car}(\\{clos}), \\{car}(\\{env}))
\&{and} $v$ = \\{cons}($x$, \\{cdr}(\\{env}))

\&{and} (\\{clos}, \\{env}) = \\{eval}($f$, \.{nil}, \.{nil})

\bigskip
\noindent Finally, we can extend the definition of \\{eval} with more
primitives (like extending the LispKit Lisp with a \.{null} primitive
predicate, and then adding a rule in the definition of \\{eval}({\tt
(null $e$)}, $n$, $v$)); we can also give rules for arithmetic, but
that is tedious.

@ {\bf SECD Machine.}\enspace\ignorespaces%
We can now offer a formal specification of the SECD Machine by
describing the transition rules $(s,e,c,d)\to(s',e',c',d')$ and how it
changes the state of the four ``registers''. There are only three
types of values (numbers, symbols, lists).

\bull
$((a~b~\hbox{\tt .\spacefactor1000}~s), e, (\hbox{\tt ADD}, c), d)\to(((a+b)~\hbox{\tt .\spacefactor1000}~s), e, c, d)$

\bull
$((a~b~\hbox{\tt .\spacefactor1000}~s), e, (\hbox{\tt SUB}, c), d)\to(((a-b)~\hbox{\tt .\spacefactor1000}~s), e, c, d)$

\bull
$((a~b~\hbox{\tt .\spacefactor1000}~s), e, (\hbox{\tt MUL}, c), d)\to(((a\times b)~\hbox{\tt .\spacefactor1000}~s), e, c, d)$

\bull
$((a~b~\hbox{\tt .\spacefactor1000}~s), e, (\hbox{\tt DIV}, c), d)\to(((a\div b)~\hbox{\tt .\spacefactor1000}~s), e, c, d)$

\bull
$((a~b~\hbox{\tt .\spacefactor1000}~s), e, (\hbox{\tt REM}, c), d)\to(((a\mathbin{\bf rem} b)~\hbox{\tt .\spacefactor1000}~s), e, c, d)$

\bull
$((a~b~\hbox{\tt .\spacefactor1000}~s), e, (\hbox{\tt EQ}, c), d)\to(((a=b)~\hbox{\tt .\spacefactor1000}~s), e, c, d)$

\bull
$((a~b~\hbox{\tt .\spacefactor1000}~s), e, (\hbox{\tt LEQ}, c), d)\to(((a\leq b)~\hbox{\tt .\spacefactor1000}~s), e, c, d)$

\bull
$((a~b~\hbox{\tt .\spacefactor1000}~s), e, (\hbox{\tt CONS}, c), d)\to(((a~\hbox{\tt .\spacefactor1000}~b)~\hbox{\tt .\spacefactor1000}~s), e, c, d)$

\bull
$(((a~\hbox{\tt .\spacefactor1000}~b)~\hbox{\tt .\spacefactor1000}~s), e, (\hbox{\tt CAR}, c), d)\to((a~\hbox{\tt .\spacefactor1000}~s), e, c, d)$

\bull
$(((a~\hbox{\tt .\spacefactor1000}~b)~\hbox{\tt .\spacefactor1000}~s), e, (\hbox{\tt CDR}, c), d)\to((b~\hbox{\tt .\spacefactor1000}~s), e, c, d)$

\bull
$(s,e,({\tt LD}~i,c),d)\to((x~\hbox{\tt .\spacefactor1000}~s),e,c,d)$

\&{where} $x$ = \\{locate}($i$, $e$)

\&{and} \\{locate}(($b$, $n$), $e$) = \\{index}($n$, \\{index}($b$, $e$))

\&{and} \\{index}($n$, $s$) = \&{if} \\{eq}($n$, $0$) \&{then} \\{car}($s$) \&{else}
\\{index}($n-1$, \\{cdr}($s$))

\bull
$(s,e,({\tt LDC}~n,c),d)\to((n~\hbox{\tt .\spacefactor1000}~s),e,c,d)$
--- push a constant onto the S stack

\bull
$((x~\hbox{\tt .\spacefactor1000}~s),e,({\tt SEL}~c_{T}~c_{F}, c), d)\to(s,e,c_{x},(c~\hbox{\tt .\spacefactor1000}~d))$
--- where $x$ is either \.{T} or \.{F} [true or false]

\bull
$(s,e,({\tt JOIN},\dots),(c~\hbox{\tt .\spacefactor1000}~d))\to(s,e,c,d)$

\bull
$(s,e,({\tt LDF}~c',c),d)\to((c'~\hbox{\tt .\spacefactor1000}~e)~\hbox{\tt .\spacefactor1000}~s,e,c,d)$
--- Pushes a closure $(c',e)$ onto the top of the stack.

\bull
$(((c'~\hbox{\tt .\spacefactor1000}~e'), v, s),e,({\tt AP}, c),d)\to({\tt NIL},(v~\hbox{\tt .\spacefactor1000}~e'),c',((s,e,c)~\hbox{\tt .\spacefactor1000}~d))$

\noindent%
This describes function application where $((c',e'),v,s)$ the initial
S register holds the closure $(c',e')$ and then the arguments
$v$. Note: this is call-by-value semantics.

\bull $((x,\dots),e',({\tt RTN},\dots),((s,e,c)~\hbox{\tt .\spacefactor1000}~d))\to((x,s),e,c,d)$

\bull $(s,e,({\tt DUM},c),d)\to(s,(\Omega,e),c,d)$
where $\Omega$ was called \&{pending} in the definition of
the \\{eval} function for LispKit Lisp

\bull $(((c'~\hbox{\tt .\spacefactor1000}~e'),v,s),(\Omega,e),({\tt RAP},c),d)\to({\tt NIL}, \\{rplaca}(e',v),c',((s,e,c),d))$
where \\{rplaca}($e'$, $v$) replaces the \\{car} of $e'$ by $v$.

\bull $(s,e,({\tt STOP},c),d)\to\bot$ halts the machine.

@ {\bf Specification for compiler.}\enspace\ignorespaces%
We can give the specification for a compile function \\{comp}($e$, $n$, $c$)
where $e$ is a Lisp expression, $n$ is a namelist, and $c$ is the
accumulated compiled output so far. This summarizes Henderson's
{\sl Functional Programming: Application and Implementation} (1980), Ch~6.

\bull $\\{comp}(e,n,c)=({\tt LD},\\{location}(e,n),c)$ when $e$ is an atom,

\bull $\\{comp}(({\tt quote}~e),n,c) = ({\tt LDC}, e, c)$

\bull $\\{comp}(({\tt add}~e_{1}~e_{2}),n,c) = \\{comp}(e_{1},n,\\{comp}(e_{2},n,({\tt ADD}, c)))$

\bull $\\{comp}(({\tt sub}~e_{1}~e_{2}),n,c) = \\{comp}(e_{1},n,\\{comp}(e_{2},n,({\tt SUB}, c)))$

\bull $\\{comp}(({\tt mul}~e_{1}~e_{2}),n,c) = \\{comp}(e_{1},n,\\{comp}(e_{2},n,({\tt MUL}, c)))$

\bull $\\{comp}(({\tt div}~e_{1}~e_{2}),n,c) = \\{comp}(e_{1},n,\\{comp}(e_{2},n,({\tt DIV}, c)))$

\bull $\\{comp}(({\tt rem}~e_{1}~e_{2}),n,c) = \\{comp}(e_{1},n,\\{comp}(e_{2},n,({\tt REM}, c)))$

\bull $\\{comp}(({\tt eq}~e_{1}~e_{2}),n,c) = \\{comp}(e_{1},n,\\{comp}(e_{2},n,({\tt EQ}, c)))$

\bull $\\{comp}(({\tt leq}~e_{1}~e_{2}),n,c) = \\{comp}(e_{1},n,\\{comp}(e_{2},n,({\tt LEQ}, c)))$

\bull $\\{comp}(({\tt cons}~e_{1}~e_{2}),n,c) = \\{comp}(e_{1},n,\\{comp}(e_{2},n,({\tt CONS}, c)))$

\bull $\\{comp}(({\tt car}~e),n,c) = \\{comp}(e,n,({\tt CAR}, c))$

\bull $\\{comp}(({\tt cdr}~e),n,c) = \\{comp}(e,n,({\tt CDR}, c))$

\bull $\\{comp}(({\tt atom}~e),n,c) = \\{comp}(e,n,({\tt ATOM}, c))$

\bull $\\{comp}(({\tt if}~e~e_{1}~e_{2}),n,c) = \\{comp}(e,n,({\tt SEL},\\{thenpt},\\{elsept},c))$

\&{where} \\{thenpt} = \\{comp}($e_{1}$, $n$, ({\tt JOIN}))

\&{and} \\{elsept} = \\{comp}($e_{2}$, $n$, ({\tt JOIN}))

\bull $\\{comp}(({\tt lambda}~\\{args}~e),n,c) = ({\tt LDF}, \\{body}, c)$

\&{where} \\{body} = \\{comp}($e$, (\\{args}, $n$), ({\tt RTN}))

\bull $\\{comp}(({\tt let}~\\{decls}~\\{body}),n,c) = \\{complis}(\\{args},m,({\tt LDF}, \\{body}, {\tt AP}, c))$

\&{where} $m$ = \\{cons}(\\{vars}(\\{decls}), $n$)

\&{and} \\{args} = \\{exprs}(\\{decls})

\vbox{\+\indent\&{and} \\{complis}($e$, $n$, $c$) = \cleartabs & \&{if} \\{eq}($e$, \.{nil}) \&{then} \\{cons}(\.{LDC}, \\{cons}(\.{NIL}, $c$))\cr
\+& \&{else} \\{complis}(\\{cdr}($e$), $n$, \\{comp}(\\{car}($e$), $n$, \\{cons}(\.{CONS},$c$)))\cr}

\&{and} \\{vars}($d$) = \\{if} \\{eq}($d$, \.{nil}) \&{then} \.{nil}
\&{else} \\{cons}(\\{caar}($d$, \\{vars}(\\{cdr}($d$))))

\&{and} \\{exprs}($d$) = \\{if} \\{eq}($d$, \.{nil}) \&{then} \.{nil}
\&{else} \\{cons}(\\{cdar}($d$, \\{exprs}(\\{cdr}($d$))))

\&{and} \\{caar}($d$) = \\{car}(\\{car}($d$)) \&{and} \\{cdar}($d$)=\\{cdr}(\\{car}($d$))

\bull \\{comp}(({\tt letrec}~\\{decls}~\\{body}), $n$, $c$)
= \\{cons}(\.{DUM}, \\{complis}(\\{args}, $m$, $c'$))

\&{where} $c'$ = \\{cons}(\.{LDF}, \\{cons}($b$, \\{cons}(\.{RAP}, $c$)))

\&{and} $b$ = \\{comp}(\\{body}, $m$, (\.{RTN}))

\&{and} $m$ = \\{cons}(\\{vars}(\\{decls}), $n$)

\&{and} \\{args} = \\{exprs}(\\{decls})

\bull (Generic applications)
\\{comp}(($e_{1}$ $e_{2}$), $n$, $c$) =
\\{complis}($e_{2}$, $n$, \\{comp}($e_{1}$, $n$, \\{cons}(\.{AP}, $c$)))

\bull Then generically we have a compile function:

\\{compile}($e$) = \\{comp}($e$, \.{NIL}, (\.{AP}, \.{STOP}))

@ If we wanted to extend the SECD machine with a heap, then we would
probably need to modify the following opcodes:

\bull
{\tt ldf} allocates a new closure object on the heap containing the
function's code pointer and the current environment pointer E, the
resulting heap address is loaded onto the top of S.

\bull
{\tt cons}, {\tt car}, {\tt cdr} --- {\tt cons} pops the top two items
from S, allocates a new pair cell in the heap, initializes its
contents with the items popped from S, then pushes the address of the
pair cell onto the top of S. The {\tt car} and {\tt cdr} pops the top
of S, treats it as a heap address, looks up the contents, then returns
either the head or tail.

\bull
{\tt ap} pops a closure pointer and argument from S, looks up the
closure in the heap, extends its captured environment on the heap with
the new argument, and shifts control to the closure's code. (It also
stores the current state [i.e., the S, E, C registers] on the top of
the D stack; updates E as before; and resets ${\rm S}\gets\Lambda$ the S stack to be empty.)

\medskip
Really, it seems like the E and D registers reflects the call stack
(``activation records'', I guess they're called nowadays). We could
probably use a more optimized implementation than a linked list of
linked lists.
