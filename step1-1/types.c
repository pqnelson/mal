#include <stdio.h>
#include "memory.h"
#include "types.h"

void val_free(LispVal *value) {
    if (NULL == value) return;
    switch (value->type) {
    case TYPE_CONS:
        cons_free((LispCons *)value);
        break;
    case TYPE_SYMBOL:
        symbol_free((LispSymbol *)value);
        break;
    case TYPE_INT:
        int_free((LispInt *)value);
        break;
    case TYPE_FLOAT:
        float_free((LispFloat *)value);
        break;
    default:
        fprintf(stderr,
                "Uhh...don't know how to handle type %d\n",
                (value->type));
    }
}

LispCons* cons(LispVal *car, LispVal *cdr) {
    LispCons *cell = alloc(sizeof(*cell));

    if (NULL == cell) abort();
    
    cell->type = TYPE_CONS;
    cell->car = car;
    cell->cdr = cdr;
    return cell;
}

void cons_free(LispCons *cons_cell) {
    if (NULL == cons_cell) return;
    if (&nil == cons_cell) return; // never free nil
    
    val_free(cons_cell->car);
    val_free(cons_cell->cdr);
    free(cons_cell);
    cons_cell = NULL;
}

LispSymbol* symbol_new(char *name) {
    LispSymbol *symbol = alloc(sizeof(*symbol));
    symbol->type = TYPE_SYMBOL;
    symbol->name = name;
    return symbol;
}

void symbol_free(LispSymbol *symbol) {
    if (NULL == symbol) return;
    free(symbol->name);
    free(symbol);
    symbol = NULL;
}

const LispCons nil = {.type = TYPE_CONS, .car = NULL, .cdr = NULL};

LispInt* int_new(long long value) {
    LispInt *integer = alloc(sizeof(*integer));
    
    if (NULL == integer) abort();
        
    integer->type = TYPE_INT;
    integer->value = value;

    return integer;
}

void int_free(LispInt *integer) {
    if (NULL == integer) return;
    free(integer);
}

LispFloat* float_new(double value) {
    LispFloat *real = alloc(sizeof(*real));
    if (NULL == real) abort();
    real->type = TYPE_FLOAT;
    real->value = value;
    return real;
}

void float_free(LispFloat *real) {
    printf("float_free called\n");
    if (NULL == real) return;
    free(real);
}
