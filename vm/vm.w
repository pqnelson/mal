

\def\topofcontents{
  \leftline{\sc\today\ at \hours}\bigskip\bigskip
  \centerline{\titlefont\title}}
  
\def\title{Lisp Virtual Machine}

\font\sixrm=cmr6
\def\registeredtrademark{\ooalign {\hfil \raise .07ex\hbox {\sixrm R}\hfil \crcr \Orb }}

\def\define#1{{\bf``#1''}}

\def\bull{\smallskip\textindent{$\bullet$}}
% organizations
\def\GNU/{{\mc GNU\spacefactor1000}} % ltugboat.cls
\def\IEEE/{{\mc IEEE\spacefactor1000}} % ltugboat.cls
\def\POSIX/{{\mc POSIX\spacefactor1000}} % s-tugboat.mkiv

% computer programs
\def\BSD/{{\mc BSD\spacefactor1000}} % ltugboat.cls
\def\SOLARIS/{{\rm\textsc{Solaris}\spacefactor1000}}
\def\UNIX/{{\mc U\kern-.05emNIX\spacefactor1000}}
\def\ULTRIX/{{\mc U\kern-.05emLTRIX\spacefactor1000}}
\def\macOS/{{\rm mac{\mc OS\spacefactor1000}}} % classic mac
\def\MacOSX/{{\rm Mac\,{\mc OS\,X\spacefactor1000}}} % OS X

% tools
\def\ACSL/{{\mc ACSL\spacefactor1000}}

% patch the typesetting macros for prettyprinting C code
\def\E{=} % equivalence
\let\K=\leftarrow % assignment
@s line normal @q unreserve a C++ keyword @>
@s not_eq normal @q unreserve a C++ keyword @>
@s bool normal @q unreserve a C++ keyword @>

@** Introduction.
The Symbolics Lisp Machine has garnered a mystique over the years.
But after the first AI Winter, Symbolics lost its revenue streams, and
their Lisp Machines have become artifacts. This has only added to
their lustre. Peter Kogge's {\it The Architecture of Symbolic Computers}
(McGraw--Hill, 1991) reviews some aspects of Lisp Machines, explaining
it is little more than an SECD Machine with some optimizations.

Kogge's description guides our endeavour. We will implement a virtual
machine based on the SECD Machine, and add optimizations based on the
Symbolics Lisp Machines.

Since we will be implementing a \POSIX/-compliant program, we define
the |_POSIX_C_SOURCE| macro (see \POSIX/ vol~2 ``System Interfaces'',
Ch~2, \S2.2.1.2). We will use the 2008~\POSIX/ standard, since that's
what Frama-C uses.

@q https://pubs.opengroup.org/onlinepubs/9699919799.2008edition/functions/V2_chap02.html#tag_16_02 @>
@q % https://pubs.opengroup.org/onlinepubs/9799919799/functions/V2_chap02.html#tag_16_02 @>

@c
#define _POSIX_C_SOURCE 200809L /* for 2008; 200112L for 2001; for 2024: |202405L| */
@<Preprocessor macros@>@;

@<Read...@>

@i w/lispkit-spec.w

@ We need the basic \CEE/ libraries.

@<Preprocessor macros@>=
#include <stdlib.h>
#include <stdio.h>
#include <assert.h>

@ Following Knuth (see, e.g., his \.{mmix-sim} program), we are
concerned about portability, and so we want to support even \CEE/89
compilers. We will be following Henry Rabinowitz and Chaim Schaap's
{\it Portable C} (Prentice Hall, 1990)

This requires the following hack to make function
declarations work even on \CEE/89 compilers.

@<Preprocessor macros@>=
#if defined(__STDC__) && __STDC__
#define ARGS(list) list
#else
#define ARGS(list) ()
#endif

@ If the user has a \CEE/89 compiler, then we need to provide
the \&{bool} type, \\{false} is zero, \\{true} is one. 

% https://pubs.opengroup.org/onlinepubs/9799919799/basedefs/stdbool.h.html

@<Preprocessor macros@>=
#if defined(__STDC__) && __STDC__
#include <stdbool.h>
#else
typedef enum {@!false,@!true}@+@!bool;
#endif

@i w/contracts.w

@i w/readline-from-file.w

@* Index.