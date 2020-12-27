/**
 * Parser for Lisp.
 * 
 * Reads an input stream for S-expressions. 
 */
#ifndef READER_H
#define READER_H

LispVal* read_str(char *src);

#endif /* READER_H */
