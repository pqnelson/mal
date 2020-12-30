#ifndef PRINTER_H
#define PRINTER_H

void print_value(FILE *stream, LispVal *value);
void print_list(FILE *stream, LispCons *list);
void print_symbol(FILE *stream, LispSymbol *symbol);
void print_int(FILE *stream, LispInt *integer);
void print_float(FILE *stream, LispFloat *real);
void print_string(FILE *stream, String *string);
void print_cfunc(FILE *stream, CFunction *fun);

#endif
