#include <assert.h>
#include <stdio.h>
#include "types.h"
#include "printer.h"

static void print_nil(FILE *stream) {
    fprintf(stream, "nil");
}

void print_value(FILE *stream, LispVal *value) {
    if (NULL == value) {
        print_nil(stream);
        return;
    }
    assert (NULL != value);
    switch (value->type) {
    case TYPE_CONS:
        print_list(stream, (LispCons *)value);
        break;
    case TYPE_SYMBOL:
        print_symbol(stream, (LispSymbol *)value);
        break;
    default:
        fprintf(stderr, "ERROR: cannot determine type for %d\n",
                value->type);
    }
}

void print_list(FILE *stream, LispCons *list) {
    if (NULL == list || ((NULL == list->car) && (NULL == list->cdr))) {
        print_nil(stream);
    } else {
        LispCons *iter = list;
        fprintf(stream, "(");
        
        while (NULL != iter) {
            print_value(stream, iter->car);
            
            if (NULL == iter->cdr) {
                iter = NULL;
            } else if (TYPE_CONS == (iter->cdr)->type) {
                iter = (LispCons *)iter->cdr;
                // separate entries by a space
                if (NULL != iter->car) fprintf(stream, " ");
            } else {
                // print a space, the value, then break the loop
                fprintf(stream, " ");
                print_value(stream, iter->cdr);
                iter = NULL;
            }
        }
        fprintf(stream, ")");
    }
}

void print_symbol(FILE *stream, LispSymbol *symbol) {
    if (NULL == symbol || NULL == symbol->name) {
        print_nil(stream);
    } else {
        fprintf(stream, "%s", symbol->name);
    }
}
