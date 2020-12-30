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

#include <stdbool.h>

typedef enum {
    TYPE_CONS = 0,         // 0b0000
    TYPE_SYMBOL = 1,       // 0b0001
    TYPE_INT = 2,          // 0b0010
    TYPE_FLOAT = 3,        // 0b0011
    TYPE_STRING = 4,       // 0b0100
    TYPE_FUNCTION_C = 5,   // 0b0101
    TYPE_FUNCTION_LISP = 6 // 0b0110
} LispType;

typedef struct LispVal {
    LispType type;
    long refcount;
} LispVal;

void val_free(LispVal **value);
void ref_inc(LispVal *value);
void ref_dec(LispVal **value);

typedef struct LispCons {
    LispType type;
    long refcount;
    LispVal *car;
    LispVal *cdr;
} LispCons;

LispCons* cons(LispVal *car, LispVal *cdr);
void cons_free(LispCons **cons_cell);
LispVal* nth(LispCons *cell, int n);

typedef long hash_t;

typedef struct LispSymbol {
    LispType type;
    long refcount;
    char *name;
    hash_t hash;
} LispSymbol;

extern const LispSymbol t;

LispSymbol* symbol_new(char *name);
void symbol_free(LispSymbol **symbol);
hash_t symbol_hashCode(LispSymbol *symbol);
bool symbol_equals(LispSymbol *this, LispSymbol *other);

extern const LispCons nil;

// Newly added
typedef struct LispInt {
    LispType type;
    long refcount;
    long long value;
} LispInt;

LispInt* int_new(long long value);
void int_free(LispInt **integer);

typedef struct LispFloat {
    LispType type;
    long refcount;
    double value;
} LispFloat;

LispFloat* float_new(double value);
void float_free(LispFloat **real);
LispFloat* float_NaN();

typedef struct String {
    LispType type;
    long refcount;
    char *chars;
    size_t length;
} String;

String* string_new(char *buffer, size_t length);
void string_free(String **string);

LispVal* plus(LispVal *this, LispVal *other);
LispVal* minus(LispVal *this, LispVal *other);
LispVal* times(LispVal *this, LispVal *other);
LispVal* divide(LispVal *this, LispVal *other);

/*
The emacs macros to generate the union:
(defun foo-args (i)
  (cond
    ((<= i 0) "")
    ((= i 1) "void*")
    (t (concat "void*, "
               (foo-args (1- i))))))
(defun foo (i)
  (insert (concat "void* (*f"
                  (number-to-string i)
                  ")("
                  (foo-args i)
                  ");\n")))

(defun foo-range (i max)
  (when (< i max)
    (foo i)
    (foo-range (1+ i) max)))
*/
typedef struct CFunction {
    LispType type;
    long refcount;
    union {
        LispVal* (*f0)();
        LispVal* (*f1)(LispVal*);
        LispVal* (*f2)(LispVal*, LispVal*);
        LispVal* (*f3)(LispVal*, LispVal*, LispVal*);
        LispVal* (*f4)(LispVal*, LispVal*, LispVal*, LispVal*);
        LispVal* (*f5)(LispVal*, LispVal*, LispVal*, LispVal*, LispVal*);
        LispVal* (*f6)(LispVal*, LispVal*, LispVal*, LispVal*, LispVal*, LispVal*);
        LispVal* (*f7)(LispVal*, LispVal*, LispVal*, LispVal*, LispVal*, LispVal*, LispVal*);
        LispVal* (*f8)(LispVal*, LispVal*, LispVal*, LispVal*, LispVal*, LispVal*, LispVal*, LispVal*);
        LispVal* (*f9)(LispVal*, LispVal*, LispVal*, LispVal*, LispVal*, LispVal*, LispVal*, LispVal*, LispVal*);
    } fn;
    int arity;
} CFunction;

CFunction* native_fun_new(int arity);
void native_fun_free(CFunction **fun);

#endif /* TYPES_H */
