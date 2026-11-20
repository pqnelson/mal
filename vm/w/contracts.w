
@* Contracts for C Standard Library.
The Frame-\CEE/ compiler is an effort to use the
ANSI~\CEE/~Specification Language to formalize the behaviour of C
programs. It's worth consulting to try to express the contracts for
functions appearing in the \CEE/ Standard Library.
For example, the \pdfURL{annotations for {\tt stdlib.h}}%
{https://github.com/Frama-C/Frama-C-snapshot/blob/master/share/libc/stdlib.h}
are worth consulting.

Frame-C strives to conform to the \POSIX/.1-2008 standard, whenever
possible (e.g., there's no hope with Micosoft Visual \CEE/), according
to their \.{\_\_fc\_machdep\_linux\_shared.h} file's definition of
\.{\_\_FC\_POSIX\_VERSION} macro.

@ {\bf Ambiguities of C89 Standard.}\enspace\ignorespaces%
So, the \CEE/89 standard has some flaws and ambiguities. For example,
it specifies (\S7.1.6) that |size_t| ``is the unsigned integral type
of the result of the |sizeof| operator.'' But it does not specify what
its maximum value is (i.e., |SIZE_MAX| is not defined in either the \CEE/89
standard nor in the \CEE/95 standard as the maximum value of a |size_t| object).
The charitable reading of the \CEE/89 and \CEE/95 standards is that
|size_t| has |UINT_MAX>=65535==2@t$^{16}$@>-1| as its limit.

@ {\bf Definition.}\enspace\ignorespaces%
Let $p$ be a pointer.

\itemitem{(1)}
We say that $p$ is \define{Allocatable} iff the
pointer $p$ refers to the base address of an unallocated memory block.

\itemitem{(2)}
We say that $p$ is \define{Freeable} if and only if the pointer $p$
refers to the base address of an allocated memory block that can be
safely released using the \CEE/ function \\{free}.

\itemitem{(3)}
Let $n$ be an integer.
We say that $(p,n)$ is \define{Fresh} [or, that $p$ is ``fresh'' if
$n$ is clear from context] if and only if $p$ refers to the base
address of an allocated memory block, and that the length (in bytes)
of the block allocated dynamically is $n$. Further, that the memory
block allocated is disjoint from previously allocated memory blocks
which have not yet been freed (i.e., we don't overwrite an allocated
memory block still in use).

\medskip
See also \S2.7 of the \ACSL/ reference manual for more about a
formalization of \CEE/'s pointers and physical addressing.

@ The \CEE/89 standard (\S7.10.3.3) gives the following specification
for |malloc(size_t size)|: \\{malloc} returns either a null pointer or
to the allocated space, and the return value is indeterminate (with no
guarantees about the contents of the region being initialized to zero
or anything else).

The \CEE/89 standard (\S7.10.3) and the \CEE/90 standard (\S7.20.3)
specifies upon success, \\{malloc} 
returns a pointer to the start of the block of memory allocated. If
space cannot be allocated, then a null pointer is returned. If
the \\{size} requested is zero, then this is undefined behaviour.

\bull Bahavior when there is allocation: Assuming we can allocate \\{size} bytes,
\\{malloc} will assign \\{size} bytes on the heap, and the \.{result}
will point to the start of the allocated memory.

\bull Behavior when there is no allocation: Assuming \\{size} bytes is
not allocatable, then the \.{result} is the \.{NULL} pointer.

\medskip
These two behaviors completely describe \\{malloc} and they are disjoint.

@ The \CEE/89 standard (\S7.10.3.4) gives the following specification
for \PB{\\{realloc}(\&{void} ${}{*}\\{ptr},{}$\&{size\_t} \\{size})}:


\bull
The size of the object pointed to by the \\{ptr} will be changed to
the size specified by the \\{size}.

\bull
The contents of the object shall be unchanged up to the lesser of the
new and old sizes.

\bull
If the new size is larger than the old size, the value of the newly
allocated portion of the object is indeterminate.

\bull
If \\{ptr} is a null pointer, then \\{realloc} behaves
like \\{malloc}.

\bull
If \\{ptr} does not match a pointer earlier returned
by \\{calloc}, \\{malloc}, or \\{realloc}, or if the space has been
deallocated by a call to \\{free} or \\{realloc}, then the behaviour
is undefined.

\bull
If the space cannot be allocated, the object pointed to by \\{ptr} is
unchanged.

\bull
If \\{size} is zero and \\{ptr} is not a null pointer, the object it
points to is free. (This behaviour has changed in later \CEE/
standards to be undefined behaviour, permitting the \CEE/ compiler to
do this or not.)

\medskip
The contract for \PB{\\{realloc}(\&{void} ${}{*}\\{ptr},{}$\&{size\_t} \\{size})}:
assuming \\{ptr} is either null or freeable.

\itemitem{(1)} Behavior when there is allocation: assuming we can
allocate \\{size} bytes, then the computer will allocate the space,
and the \.{result} returned is the address of the start of the region
allocated. In this situation, it is ensured that the region allocated
is ``fresh''.

\itemitem{(2)} Behavior when there is deallocation: assuming
the \\{ptr} is not null, and assuming we can allocate \\{size} bytes,
then \\{realloc} will free \\{ptr}. If \\{size} is nonzero,
then (i) \\{realloc} allocates \\{size} bytes, (ii) copies over the
contents of the first \\{size} bytes of the region \\{ptr} points to,
and (iii) returns the address of the start of the region allocated
When allocation occurs, the region allocated is ``fresh''.

\itemitem{(3)} Behavior when there is failure: assuming the computer
cannot allocate \\{size} bytes, then nothing is allocated or freed,
and the result returned is the \.{NULL} pointer.

\medskip

Importantly, this means that \\{realloc} returns either (1) a null
pointer, or (2) a pointer to the possibly moved allocated space.

One ambiguity with the \CEE/89 specification, which the \CEE/99
standard fixed, is that \\{realloc} deallocates the old object when it
can allocate the space.

{\bf Caution}: if \\{ptr} is null or if \\{size} is zero, then
non-ANSI versions of \\{realloc} causes undefined behaviour.
A.~Dolenc, A.~Lemmke, D.~Keppel, and G.V.~Reilly's
``Notes on Portable \CEE/'' (Nov 1990, 8th revision, \S8.2.5)
recommends avoiding \\{realloc} when either \\{ptr} is null
or \\{size} is zero.

@ The \CEE/89 standard (\S7.9.7.1) specifies the
|int fgetc(FILE *stream)| function gets the next character (if
present) as an \&{unsigned char} converted to an \&{int} from the
input stream pointed to by \\{stream} and advances the associated file
position indicator for the stream (if defined).

Assume the \\{stream} is valid

\bull Behavior when error:
If an error is encountered (e.g., the \\{stream} is invalid),
then \\{fgetc} sets the \\{errno} and returns |EOF|.

\bull Behavior when stream has been exhausted:
If the \\{stream} is valid and the \\{stream} has its end-of-file indicator set,
then \\{fgetc} returns |EOF|.

\bull Behavior when we can read more:
Assuming that \\{stream} is valid, \\{fgetc} mutates the \\{stream}
(updating the file position indicator, possibly setting its
end-of-file indicator) and returns the next character from the \\{stream}.
