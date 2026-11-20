
@* Read a line from an input stream.
The |readline_from_stream| routine obtains a string for a parser.
It could be from the |stdin| stream for REPLs, or it could be from a file/pipe.
This is basically how GNU's readline and BSD's libedit (and other
similar libraries) works under the hood. But I did not want to get
bogged down on figuring out what library to use, which library is
bloated, and GNU readline developers do not believe valgrind is a
legitimate tool (Valgrind raises a bunch of errors when working with
readline). If we get into a situation where Terminal emulators are not
synchronizing correctly with the program (and we are {\it forced} to
use a library), then I suppose \.{linenoise} would be the minimal library
to defer to.

There are three main ``moments'' or ``actions'' in the
loop: ensure there's space in the buffer to add a character, read a
character from the stream, then update the buffer by adding the
character to it (or exit the function when |'\n'| or |EOF| has been
encountered). It's not terribly complicated conceptually. But some of
the acts require a bit of care in the details.

The |readline_from_stream| will allocate an array of characters, and
return it to the caller. The caller will be responsible for
|free()|-ing it. If an error occurs, then the |err| parameter will be
set to a nonzero value. If the function is successful, then |err|
should be zero.

% https://pubs.opengroup.org/onlinepubs/9799919799/functions/fgetc.html
% \S7.9.7.1 of the C89 standard
% \S7.19.7.1 of the C99 standard

% size_t C89 standard \S6.3.3.4, \S7.1.1

@<Read a line from an input stream@>=
char* readline_from_stream(FILE *fp, int *err) {
  size_t len = 0; /* amount of |line| currently used */
  size_t cap = 16; /* current capacity of the |line| */
  char *line = NULL; /* return value */
  if (err != NULL) *err = 0; /* reset the |err| flag */
  @<Exit early when |fp| is null@>@;
  line = malloc(cap * sizeof(char)); /* initialize buffer */
  @<Check for allocation failure in |readline|...@>@;
  while(true) {
    @<Ensure capacity of |line| buffer is big enough...@> @; @|
    assert(len < cap);
    int c = fgetc(fp);
    @<Insert character into |line|...@> @;
  }
  return line; /* We shouldn't get here, but still... */
}

@ @<Exit early when |fp| is null@>=
  if (fp == NULL) {
      if (err != NULL) *err = 1;
      return NULL;
  }
  assert(fp != NULL);

@ The |line| buffer has already been initialized to be 16 characters long.
When we have used up all characters, we double the |line|'s capacity using
|realloc()|. If there's no memory left (i.e., |line| is null), then we
should free the old contents of the |line| and propagate the error
onwards. Otherwise, |realloc()| succeeded, so we should just update
the capacity parameter.

Note that |newcap <= cap| happens if |cap*2| overflows (which should
happen in the situation where |cap == SIZE_MAX|---recall the \POSIX/
standard requires |SIZE_MAX >= 65535 == 2@t$^{16}$@> - 1| according to its specification
for \.{stdint.h}; see also the \CEE/99 Standard \S7.18.3). So any line with more than |SIZE_MAX| bytes may
possibly fail.

If this succeeds, then |len < cap|. If this fails, then the |line|
buffer is freed, |err| is set to a nonzero value, and |NULL| is returned.
When |realloc| fails, the |errno| global variable (which \POSIX/
provides) is automatically set to |ENOMEM|. Importantly, if |readline|
fails or encounters exceptions (e.g., out of memory), then it will be
due to this chunk of code.

% https://pubs.opengroup.org/onlinepubs/9799919799/functions/realloc.html
% https://pubs.opengroup.org/onlinepubs/9799919799/basedefs/errno.h.html
% \S7.10.3.4 of the C89 standard for |realloc()|

@<Ensure capacity of |line| buffer is big enough, and exit if it needs to grow but cannot@>=
if (len + 1 >= cap) {
  size_t newcap = cap * 2;
  char *oldval = line;
  if (newcap <= cap) @<Whoops! |line| buffer capacity overflowed...@>@;
  line = realloc(line, newcap * sizeof(char));
  @<Check for allocation failure in |readline|...@>@;
  cap = newcap;
}

@ Using \POSIX/'s \.{errno.h}, specifically the \.{ENOMEM} error
value. Update the error flag to this value, and return |NULL| as the
array value.
% https://pubs.opengroup.org/onlinepubs/9699919799/basedefs/errno.h.html

@<Whoops! |line| buffer capacity overflowed, cleaning up, returning |NULL|@>=
{
  free(line);
  if (err != NULL) *err = 1;
  errno = ENOMEM;
  return NULL;
}

@ @<Check for allocation failure in |readline|, exit if failed@>=
  if (line == NULL) { /* |realloc()| or |malloc()| failed to allocate new space */
    if (oldval != NULL) free(oldval);
    if (err != NULL) *err = 1;
    return NULL;
  }
  assert(line != NULL);

@ Care must be taken: most modern operating systems use |'\n'| to end
a line (Windows uses |"\r\n"|), but this is by no means universal. For
some examples:

\bull
|'\r'| ends the line for Commodore 64, Commodore 128, Acorn BBC, ZX
Spectrum, TRS-80, Apple II, Oberon, classic Mac OS, HP Series 80, MIT
Lisp Machine, and OS-9

\bull
|"\n\r"| ends the line for Acorn BBC and RISC~OS

\bull
|"\r\n"| ends the line for Windows, MS-DOS compatibles, Atari TOS, DEC
TOPS-10, RT-11, CP/M, MP/M, OS/2, Symbian~OS, Palm~OS, Amstrad~CPC,
and most other early non-\UNIX/ and non-IBM operating systems 

\bull
|'\x1E'| ends the line for QNX pre-\POSIX/ implementation (pre-4 version).
This character is |RS| [record separator] in ASCII.

\bigskip
For Windows (and Acorn BBC and RISC~OS), I think the trailing |"\r\n"| (resp.,
|"\n\r"|) suffix should be omitted from the \\{line} buffer.

@<Insert character into |line|, but exit if |c| is |EOF| or |'\n'|@>=
if (c == EOF || c == '\n')
  @<Exit |readline_from_stream| since we've found an |EOF| or newline@>@;
else { @+
  line[len] = c; @+
  len++; @+
} /* accumulate character */

@ This is the ``successful'' exit of |readline_from_stream| because we
have ended the line. It seems like we've come a long way just for
success, but isn't that life?

@<Exit |readline_from_stream| since we've found an |EOF| or newline@>=
{
  if (c == EOF && len == 0) {
    free(line);
    return NULL;
  } else {
    line[len] = '\0';
    return line;
  }
}
