# C Programming

## Preprocessor stuff

- Determining the OS and architecture by preprocessor macros
  - https://sourceforge.net/p/predef/wiki/Home/
  - https://docs.freebsd.org/en/books/porters-handbook/porting-dads/#porting-versions
  - https://stackoverflow.com/q/142508

## Portability

- VAX C requires bit fields within a `struct` be either of type `int`
  or `unsigned`
- VAX C and HP-UX treats `char` as `signed` (i.e., `signed int`)
- When shifting `signed int` right, the vacated bits might be filled
  with zeroes or with copies of the signbit; `unsigned int` fills them
  with zeroes.