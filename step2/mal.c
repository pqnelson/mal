/**
 * Tests if we can read a string into an S-expression.
 */
#include <assert.h>
#include <ctype.h>
#include <stdbool.h>
#include <stdio.h>
#include <string.h>
#include "memory.h"
#include "scanner.h"
#include "types.h"
#include "reader.h"
#include "printer.h"
#include "env.h"
#include "debug.h"

void work_example(char *test_program) {
    size_t length = strlen(test_program)+1;
    char *src = alloc(length+1);
    snprintf(src, length+1, "%s", test_program);
    src[length] = '\0';

    LispVal *value = read_str(src);
    printf("src == NULL ? %s\n", (NULL == src) ? "true" : "false");
    printf("value:\n\t");
    print_value(stdout, value);
    printf("\n");
    printf("value is a string? %s\n",
           (TYPE_STRING == value->type ? "true" : "false"));
    val_free(value);
}

void good_example() {
    work_example("(this\nis (a (test)to) see)");
}

void bad_example() {
    work_example("(this should fail");
}

void example_with_nil() {
    work_example("(foo (bar () baz) spam)");
}

void comment_example() {
    work_example("(foo ;(bar () baz)\n spam eggs)");
}

void double_check_nil() {
    char test_program[] = "()";
    size_t length = strlen(test_program)+1;
    char *src = alloc(length+1);
    snprintf(src, length+1, "%s", test_program);
    src[length] = '\0';

    LispVal *value = read_str(src);

    printf("value == &nil ? %s\n",
           ((LispVal *)&nil == value) ? "true" : "false");
    
    printf("value:\n\t");
    print_value(stdout, value);
    printf("\n");
    val_free(value);
}

void int_example0() {
    work_example("1234");
}

void int_example() {
    work_example("(= 32 (+ x (sq y)))");
}

void float_example() {
  // ("=", ("area", (("*", ("3.141", (("sq", ("r", nil)), nil))), nil)))
    work_example("(= area (* 3.141 (sq r)))");
}

void pi_example() {
    work_example("3.14159");
}

void string_example() {
    work_example("\"This is a string?\"");
}

int parsing_tests() {
    good_example();
    example_with_nil();
    
    double_check_nil();
    comment_example();
  
    int_example0();
    int_example();
    pi_example();
    float_example();
  
    // string_example();
    // bad_example();

    return 0;
}


LispVal* apply(LispVal *fn, LispVal *args) {
    assert(NULL != fn);
    assert((TYPE_FUNCTION_C == fn->type) ||
           (TYPE_FUNCTION_LISP == fn->type));
    assert((NULL == args) || (TYPE_CONS == args->type));
    if (TYPE_FUNCTION_LISP == fn->type) {
        return (LispVal *)&nil;
    } else {
        CFunction *f = (CFunction *)fn;
        LispCons *a = (LispCons *)args;
        switch (f->arity) {
        case 0: return f->fn.f0();
        case 1: return f->fn.f1(nth(a, 0));
        case 2: return f->fn.f2(nth(a, 0), nth(a, 1));
        case 3: return f->fn.f3(nth(a, 0), nth(a, 1), nth(a, 2));
        case 4: return f->fn.f4(nth(a, 0), nth(a, 1), nth(a, 2), nth(a, 3));
        case 5: return f->fn.f5(nth(a, 0), nth(a, 1), nth(a, 2), nth(a, 3), nth(a, 4));
        case 6: return f->fn.f6(nth(a, 0), nth(a, 1), nth(a, 2), nth(a, 3), nth(a, 4), nth(a, 5));
        case 7: return f->fn.f7(nth(a, 0), nth(a, 1), nth(a, 2), nth(a, 3), nth(a, 4), nth(a, 5), nth(a, 6));
        case 8: return f->fn.f8(nth(a, 0), nth(a, 1), nth(a, 2), nth(a, 3), nth(a, 4), nth(a, 5), nth(a, 6), nth(a, 7));
        case 9: return f->fn.f9(nth(a, 0), nth(a, 1), nth(a, 2), nth(a, 3), nth(a, 4), nth(a, 5), nth(a, 6), nth(a, 7), nth(a, 8));
        default:
            eprintf("Unsupported arity %d\n", f->arity);
            abort();
        }
    }
}

LispVal* EVAL(LispVal *ast, Env *env);

LispVal* eval_ast(LispVal *ast, Env *env) {
    if (!ast) return NULL;
    if (TYPE_SYMBOL == ast->type) {
        return env_get(env, (LispSymbol *)ast);
    } else if (TYPE_CONS == ast->type) {
        LispCons *iter = (LispCons *)ast;
        LispVal *head = EVAL(iter->car, env);
        LispCons *result = cons(head, NULL);
        LispCons *current = result;
        while (NULL != iter) {
            if (NULL == iter->cdr) {
                current->cdr = (LispVal *)&nil;
                iter = NULL;
            } else if (TYPE_CONS == iter->cdr->type) {
                iter = (LispCons *)iter->cdr;
                head = EVAL(iter->car, env);
                current->cdr = (LispVal *)cons(head, NULL);
                current = (LispCons *)current->cdr;
            } else {
                head = EVAL(iter->cdr, env);
                current->cdr = head;
                iter = NULL;
            }
        }
        return (LispVal *)result;
    } else {
        return ast;
    }
}

LispVal* EVAL(LispVal *ast, Env *env) {
    if (NULL == ast) return NULL;

    if (TYPE_CONS != ast->type) {
        return eval_ast(ast, env);
    }

    // apply
    assert(TYPE_CONS == ast->type);
    LispVal *elts = eval_ast(ast, env);
    if (NULL == elts) return NULL;
    assert(TYPE_CONS == elts->type);
    LispVal *fn = ((LispCons *)elts)->car;
    LispVal *args = ((LispCons *)elts)->cdr;
    return apply(fn, args);
}

Env* initial_env() {
    Env *global = env_new();
    LispSymbol *apply_symbol = symbol_new("apply*");
    CFunction *lisp_apply = native_fun_new(2);
    lisp_apply->fn.f2 = apply;
    LispSymbol *plus_symbol = symbol_new("+");
    CFunction *lisp_plus = native_fun_new(2);
    lisp_plus->fn.f2 = plus;
    LispSymbol *minus_symbol = symbol_new("-");
    CFunction *lisp_minus = native_fun_new(2);
    lisp_minus->fn.f2 = &minus;
    LispSymbol *times_symbol = symbol_new("*");
    CFunction *lisp_times = native_fun_new(2);
    lisp_times->fn.f2 = &times;
    LispSymbol *slash_symbol = symbol_new("/");
    CFunction *lisp_div = native_fun_new(2);
    lisp_div->fn.f2 = &divide;
    env_set(global, apply_symbol, (LispVal *)lisp_apply);
    env_set(global, plus_symbol, (LispVal *)lisp_plus);
    env_set(global, minus_symbol, (LispVal *)lisp_minus);
    env_set(global, times_symbol, (LispVal *)lisp_times);
    env_set(global, slash_symbol, (LispVal *)lisp_div);
    return global;
}

LispVal* READ() {
    char *line = malloc(128);
    printf("\nLISP> ");
    if (!fgets(line, sizeof(line), stdin)) {
        printf("\n");
        return NULL;
    }
    /* for (int i = strlen(line); isspace(line[i]); i--) { */
    /*     line[i] = '\0'; */
    /* } */
    return read_str(line);
}

LispVal* rep(Env *env) {
    LispVal *ast = READ(), *expr;
    if (NULL == ast) return ast;

    expr = EVAL(ast, env);
    if (ast != expr) {
        val_free(ast);
    }
    
    print_value(stdout, expr);
    printf("\n");
    val_free(expr);
    return (LispVal *)&t;
}

int repl() {
    Env *env = initial_env();
    for(;;) {
        LispVal *expr = rep(env);
        if (NULL == expr) break;
    }

    return 0;
}

int main() {
    repl();
    return 0;
}

int eval_test1() {
    char src[] = "(+ 1 2)";
    LispVal *ast = read_str(src);
    printf("EVALUATING ast = ");
    print_value(stdout, ast);
    printf("\n\n");
    printf("(nth ast 0) = ");
    print_value(stdout, nth((LispCons *)ast, 0));
    printf("\n\n");
    printf("(nth ast 1) = ");
    print_value(stdout, nth((LispCons *)ast, 1));
    printf("\n\n");
    printf("(nth ast 2) = ");
    print_value(stdout, nth((LispCons *)ast, 2));
    printf("\n\n");
    
    Env *env = initial_env();
    LispVal *expr = EVAL(ast, env);
    print_value(stdout, expr);
    val_free(expr);
    val_free(ast);
    env_free(env);
    return 0;
}
