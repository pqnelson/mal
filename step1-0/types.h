/**
 * All possible expressions.
 * 
 * We encode a Lisp expression in terms of S-expressions, a
 * linked-list of atoms. For now, we have only "symbols" as atoms
 * (any sequence of characters which is not whitespace or
 * parentheses).
 * 
 * There is no memory management, so every expression created must
 * be freed by hand.
 */
#ifndef TYPES_H
#define TYPES_H

typedef enum {
    TYPE_CONS = 0,
    TYPE_SYMBOL = 1
} LispType;

typedef struct LispVal {
    LispType type;
} LispVal;

void val_free(LispVal *value);

typedef struct LispCons {
    LispType type;
    LispVal *car;
    LispVal *cdr;
} LispCons;

LispCons* cons(LispVal *car, LispVal *cdr);
void cons_free(LispCons *cons_cell);

typedef struct LispSymbol {
    LispType type;
    char *name;
} LispSymbol;

LispSymbol* symbol_new(char *name);
void symbol_free(LispSymbol *symbol);

#endif /* TYPES_H */
