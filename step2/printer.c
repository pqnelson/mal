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
    case TYPE_INT:
        print_int(stream, (LispInt *)value);
        break;
    case TYPE_FLOAT:
        print_float(stream, (LispFloat *)value);
        break;
    case TYPE_STRING:
        print_string(stream, (String *)value);
        break;
    case TYPE_FUNCTION_C:
        print_cfunc(stream, (CFunction *)value);
        break;
    default:
        fprintf(stderr, "ERROR: cannot determine type for %d\n",
                value->type);
    }
}

void print_int(FILE *stream, LispInt *integer) {
    if (NULL == integer) print_nil(stream);
    else                 fprintf(stream, "%lld", integer->value);
}

void print_float(FILE *stream, LispFloat *real) {
    if (NULL == real) print_nil(stream);
    else              fprintf(stream, "%.16f", real->value);
}

void print_string(FILE *stream, String *string) {
    if (NULL == string) print_nil(stream);
    else                fprintf(stream, "%s", string->chars);
}


void print_list(FILE *stream, LispCons *list) {
    if (NULL == list || (false && (NULL == list->car) && (NULL == list->cdr))) {
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


void print_cfunc(FILE *stream, CFunction *fun) {
    if (NULL == fun) {
        print_nil(stream);
    } else {
        fprintf(stream, "#native_function<%p, arity=%d, refcount=%ld>",
                (void*)fun,
                fun->arity,
                fun->refcount);
    }
}
