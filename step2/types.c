#include <assert.h>
#include <math.h>
#include <stdio.h>
#include <string.h>
#include "debug.h"
#include "memory.h"
#include "types.h"
#include "printer.h"

void val_free(LispVal **value) {
    if (NULL == value || NULL == *value) return;
    if ((LispVal *)&nil == *value) return;
    if ((LispVal *)&t == *value) return;

    assert(((*value)->refcount) >= 0);
    (*value)->refcount--;
    if (((*value)->refcount) > 0) return;
    switch ((*value)->type) {
    case TYPE_CONS:
        cons_free((LispCons **)value);
        break;
    case TYPE_SYMBOL:
        symbol_free((LispSymbol **)value);
        break;
    case TYPE_INT:
        int_free((LispInt **)value);
        break;
    case TYPE_FLOAT:
        float_free((LispFloat **)value);
        break;
    case TYPE_STRING:
        string_free((String **)value);
        break;
    case TYPE_FUNCTION_C:
        native_fun_free((CFunction **)value);
        break;
    default:
        eprintf("Uhh...don't know how to handle type %d\n",
                ((*value)->type));
    }
}

void ref_inc(LispVal *value) {
    if (NULL == value) return;
    if ((LispVal *)&nil == value) return;
    if ((LispVal *)&t == value) return;
    assert((value->refcount) >= 0);
    value->refcount++;
}

void ref_dec(LispVal **value) {
    if (NULL == value || NULL == *value) return;
    if ((LispVal *)&nil == *value) return;
    if ((LispVal *)&t == *value) return;
    assert(((*value)->refcount) > 0);
    val_free(value);
}

LispCons* cons(LispVal *car, LispVal *cdr) {
    LispCons *cell = alloc(sizeof(*cell));

    if (NULL == cell) abort();
    
    cell->type = TYPE_CONS;
    cell->refcount = 1;
    cell->car = car;
    cell->cdr = cdr;
    ref_inc(car);
    ref_inc(cdr);
    debug("cons(car<%p>, cdr<%p>) at %p\n",
          (void*)car, (void*)cdr, (void*)cell);
    return cell;
}

void cons_free(LispCons **cons_cell) {
    if (NULL == cons_cell || NULL == *cons_cell) return;
    if (&nil == *cons_cell) return; // never free nil

    if((*cons_cell)->car) ref_dec(&((*cons_cell)->car));
    if((*cons_cell)->cdr) ref_dec(&((*cons_cell)->cdr));
    free(*cons_cell);
    *cons_cell = NULL;
}

LispVal* nth(LispCons *cell, int n) {
    if (NULL == cell) return NULL;
    if (n < 0) return NULL;
    LispCons *iter = cell;
    while ((n > 0) && (NULL != iter)) {
        iter = (LispCons *)(iter->cdr);
        n--;
    }
    if (NULL != iter)
        return iter->car;
    else
        return NULL;
}

const LispSymbol t = {.type = TYPE_SYMBOL, .name = "t"};

LispSymbol* symbol_new(char *name) {
    LispSymbol *symbol = alloc(sizeof(*symbol));
    symbol->type = TYPE_SYMBOL;
    symbol->refcount = 1;
    symbol->name = name;
    symbol->hash = 0;
    debug("symbol(%s)<%p>\n", name, (void*)symbol);
    return symbol;
}

void symbol_free(LispSymbol **symbol) {
    if (NULL == symbol || NULL == *symbol) return;
    if (&t == *symbol) return; // do NOT free "t"
    debug("Trying to free symbol<%p>[name='%s', refcount=%ld]\n",
          (void*)*symbol, (*symbol)->name, (*symbol)->refcount);
    if ((*symbol)->name) free((*symbol)->name);
    free(*symbol);
    *symbol = NULL;
}

/**
 * Cached hash computation for symbols.
 * 
 * There is no shortage of hash functions to pick from. So I expect I may
 * end up with a zoo of hashes, eventually.
 * 
 * Clojure's hashCode for a symbol uses Java's @c{String::hashCode()} method.
 * We mimic its methodology, with Java 8's implementation.
 * 
 * @see{https://github.com/openjdk/jdk/blobl/jdk8-b01/jdk/src/share/classes/java/lang/String.java#L1494}
 */
hash_t symbol_hashCode(LispSymbol *symbol) {
    if ((symbol->hash != 0)
        || ((LispSymbol*)&nil == symbol)) return symbol->hash;
    hash_t h = 0;

    for(int i=0; symbol->name[i] != '\0'; i++) {
        h = 31*h + (hash_t)symbol->name[i];
    }
    symbol->hash = h;
    return h;
}

bool symbol_equals(LispSymbol *this, LispSymbol *other) {
    if (NULL == this) return (NULL == other);
    else if (NULL == other) return false;
    else if (this == other) return true; // the same objects are equal
    else return (0 == strcmp(this->name, other->name));
}

const LispCons nil = {.type = TYPE_CONS, .car = NULL, .cdr = NULL};

LispInt* int_new(long long value) {
    LispInt *integer = alloc(sizeof(*integer));
    
    if (NULL == integer) abort();
        
    integer->type = TYPE_INT;
    integer->refcount = 1;
    integer->value = value;

    debug("int<%p>\n", (void*)integer);
    return integer;
}

void int_free(LispInt **integer) {
    if (NULL == integer || NULL == *integer) return;
    debug("Freeing int<%p>(%lld)\n", (void*)*integer, (*integer)->value);
    free(*integer);
    *integer = NULL;
}

LispVal* int_plus(LispInt *this, LispVal *other) {
    if ((NULL != this) && (NULL != other)) {
        if (TYPE_INT == other->type) {
            long long sum = (this->value) + ((LispInt *)other)->value;
            return (LispVal *)int_new(sum);
        } else if (TYPE_FLOAT == other->type) {
            double sum = (this->value) + ((LispFloat *)other)->value;
            return (LispVal *)float_new(sum);
        } else {
            // ERROR
        }
    }
    eprintf("int_plus() HUH cannot understand LHS: ");
    print_value(stderr, other);
    eprintf("\n");
    // ERROR
    return (LispVal *)float_NaN();
}

LispVal* int_minus(LispInt *this, LispVal *other) {
    if ((NULL != this) && (NULL != other)) {
        if (TYPE_INT == other->type) {
            long long diff = (this->value) - ((LispInt *)other)->value;
            return (LispVal *)int_new(diff);
        } else if (TYPE_FLOAT == other->type) {
            double diff = (this->value) - ((LispFloat *)other)->value;
            return (LispVal *)float_new(diff);
        } else {
            // ERROR
        }
    }
    // ERROR
    return (LispVal *)float_NaN();
}

LispVal* int_times(LispInt *this, LispVal *other) {
    if ((NULL != this) && (NULL != other)) {
        if (TYPE_INT == other->type) {
            long long product = (this->value) * ((LispInt *)other)->value;
            return (LispVal *)int_new(product);
        } else if (TYPE_FLOAT == other->type) {
            double product = (this->value) * ((LispFloat *)other)->value;
            return (LispVal *)float_new(product);
        } else {
        }
    }
    return (LispVal *)float_NaN();
}

// TODO: handle divide-by-zero
LispVal* int_div(LispInt *this, LispVal *other) {
    if ((NULL != this) && (NULL != other)) {
        if (TYPE_INT == other->type) {
            long long quotient = (this->value) / ((LispInt *)other)->value;
            return (LispVal *)int_new(quotient);
        } else if (TYPE_FLOAT == other->type) {
            double quotient = (this->value) / ((LispFloat *)other)->value;
            return (LispVal *)float_new(quotient);
        } else {
        }
    }
    return (LispVal *)float_NaN();
}

LispFloat* float_new(double value) {
    LispFloat *real = alloc(sizeof(*real));
    if (NULL == real) abort();
    real->type = TYPE_FLOAT;
    real->refcount = 1;
    real->value = value;
    TRACE("float<%p>\n", (void*)real);
    return real;
}

void float_free(LispFloat **real) {
    printf("float_free called\n");
    if (NULL == real || NULL == *real) return;
    free(*real);
    *real = NULL;
}

LispFloat* float_NaN() {
    return float_new(nan(""));
}

LispVal* float_plus(LispFloat *this, LispVal *other) {
    if ((NULL != this) && (NULL != other)) {
        if (TYPE_INT == other->type) {
            double sum = (this->value) + ((LispInt *)other)->value;
            return (LispVal *)float_new(sum);
        } else if (TYPE_FLOAT == other->type) {
            double sum = (this->value) + ((LispFloat *)other)->value;
            return (LispVal *)float_new(sum);
        } else {
            return (LispVal *)float_NaN();
        }
    }
    return (LispVal *)float_NaN();
}

LispVal* float_minus(LispFloat *this, LispVal *other) {
    if ((NULL != this) && (NULL != other)) {
        if (TYPE_INT == other->type) {
            double sum = (this->value) - ((LispInt *)other)->value;
            return (LispVal *)float_new(sum);
        } else if (TYPE_FLOAT == other->type) {
            double sum = (this->value) - ((LispFloat *)other)->value;
            return (LispVal *)float_new(sum);
        } else {
            return (LispVal *)float_NaN();
        }
    }
    return (LispVal *)float_NaN();
}

LispVal* float_times(LispFloat *this, LispVal *other) {
    if ((NULL != this) && (NULL != other)) {
        if (TYPE_INT == other->type) {
            double sum = (this->value) * ((LispInt *)other)->value;
            return (LispVal *)float_new(sum);
        } else if (TYPE_FLOAT == other->type) {
            double sum = (this->value) * ((LispFloat *)other)->value;
            return (LispVal *)float_new(sum);
        } else {
        }
    }
    return (LispVal *)float_NaN();
}

LispVal* float_div(LispFloat *this, LispVal *other) {
    if ((NULL != this) && (NULL != other)) {
        if (TYPE_INT == other->type) {
            double sum = (this->value) / ((LispInt *)other)->value;
            return (LispVal *)float_new(sum);
        } else if (TYPE_FLOAT == other->type) {
            double sum = (this->value) / ((LispFloat *)other)->value;
            return (LispVal *)float_new(sum);
        } else {
        }
    }
    return (LispVal *)float_NaN();
}


String* string_new(char *buffer, size_t length) {
    String *string = alloc(sizeof(*string));
    if (NULL == string) abort();

    string->chars = alloc(1 + length);
    if (NULL == string->chars) abort();
    
    memcpy(string->chars, buffer, length);
    string->chars[length] = '\0';

    string->refcount = 1;
    string->length = length;
    string->type = TYPE_STRING;
    TRACE("string<%p>\n", (void*)string);
    return string;
}

void string_free(String **string) {
    if (NULL == string || NULL == *string) return;

    if (NULL != (*string)->chars) free((*string)->chars);

    free(*string);
    *string = NULL;
}

LispVal* plus(LispVal *this, LispVal *other) {
    LispVal *result = NULL;
    if (NULL == this) result = (LispVal *)float_NaN();
    if (TYPE_INT == this->type)
        result = int_plus((LispInt *)this, other);
    if (TYPE_FLOAT == this->type)
        result = float_plus((LispFloat *)this, other);

    if (NULL != result) {
        ref_dec(&this);
        ref_dec(&other);
    } else {
        result = (LispVal *)float_NaN();
    }
    return result;
}

LispVal* minus(LispVal *this, LispVal *other) {
    LispVal *result = NULL;
    if (NULL == this) result = (LispVal *)float_NaN();
    if (TYPE_INT == this->type)
        result = int_minus((LispInt *)this, other);
    if (TYPE_FLOAT == this->type)
        result = float_minus((LispFloat *)this, other);

    if (NULL != result) {
        ref_dec(&this);
        ref_dec(&other);
    } else {
        result = (LispVal *)float_NaN();
    }
    return result;
}

LispVal* times(LispVal *this, LispVal *other) {
    LispVal *result = NULL;
    if (NULL == this) result = (LispVal *)float_NaN();
    if (TYPE_INT == this->type)
        result = int_times((LispInt *)this, other);
    if (TYPE_FLOAT == this->type)
        result = float_times((LispFloat *)this, other);

    if (NULL != result) {
        ref_dec(&this);
        ref_dec(&other);
    } else {
        result = (LispVal *)float_NaN();
    }
    return result;
}

LispVal* divide(LispVal *this, LispVal *other) {
    LispVal *result = NULL;
    if (NULL == this) result = (LispVal *)float_NaN();
    if (TYPE_INT == this->type)
        result = int_div((LispInt *)this, other);
    if (TYPE_FLOAT == this->type)
        result = float_div((LispFloat *)this, other);

    if (NULL != result) {
        ref_dec(&this);
        ref_dec(&other);
    } else {
        result = (LispVal *)float_NaN();
    }
    return result;
}


CFunction* native_fun_new(int arity) {
    CFunction *fun = alloc(sizeof(*fun));
    if (NULL == fun) abort();
    fun->type = TYPE_FUNCTION_C;
    fun->arity = arity;
    fun->refcount = 1;
    debug("fun<%p>\n", (void*)fun);
    return fun;
}
void native_fun_free(CFunction **fun) {
    if (NULL == fun || NULL == *fun) return;
    free(*fun);
    *fun = NULL;
}
