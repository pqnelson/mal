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
