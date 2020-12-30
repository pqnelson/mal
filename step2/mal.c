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
    val_free(&value);
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
    val_free(&value);
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
        LispVal *result = NULL;
        CFunction *f = (CFunction *)fn;
        LispCons *a = (LispCons *)args;
        switch (f->arity) {
        case 0: result = f->fn.f0(); break;
        case 1: result = f->fn.f1(nth(a, 0)); break;
        case 2: result = f->fn.f2(nth(a, 0), nth(a, 1)); break;
        case 3: result = f->fn.f3(nth(a, 0), nth(a, 1), nth(a, 2)); break;
        case 4: result = f->fn.f4(nth(a, 0), nth(a, 1), nth(a, 2), nth(a, 3)); break;
        case 5: result = f->fn.f5(nth(a, 0), nth(a, 1), nth(a, 2), nth(a, 3), nth(a, 4)); break;
        case 6: result = f->fn.f6(nth(a, 0), nth(a, 1), nth(a, 2), nth(a, 3), nth(a, 4), nth(a, 5)); break;
        case 7: result = f->fn.f7(nth(a, 0), nth(a, 1), nth(a, 2), nth(a, 3), nth(a, 4), nth(a, 5), nth(a, 6)); break;
        case 8: result = f->fn.f8(nth(a, 0), nth(a, 1), nth(a, 2), nth(a, 3), nth(a, 4), nth(a, 5), nth(a, 6), nth(a, 7)); break;
        case 9: result = f->fn.f9(nth(a, 0), nth(a, 1), nth(a, 2), nth(a, 3), nth(a, 4), nth(a, 5), nth(a, 6), nth(a, 7), nth(a, 8)); break;
        default:
            eprintf("Unsupported arity %d\n", f->arity);
            abort();
        }
        ref_dec(&fn);
        ref_dec(&args);
        return result;
    }
}

LispVal* EVAL(LispVal *ast, Env *env);

LispCons* evlist(LispVal *ast, Env *env) {
    LispCons *iter = (LispCons *)ast;
    LispVal *head = EVAL(iter->car, env);
    LispCons *result = cons(head, NULL);
    LispCons *current = result;
    while (NULL != iter) {
        // iter ~ (evaluated . next)
        if (NULL == iter->cdr) {
            current->cdr = (LispVal *)&nil;
            iter = NULL;
        } else if (TYPE_CONS == iter->cdr->type) {
            // iter ~ (atom . cons)
            iter = (LispCons *)iter->cdr;
            head = EVAL(iter->car, env);
            current->cdr = (LispVal *)cons(head, NULL);
            current = (LispCons *)current->cdr;
        } else {
            // iter ~ (atom . atom)
            head = EVAL(iter->cdr, env);
            current->cdr = head;
            iter = NULL;
        }
    }
    return result;
}

LispVal* eval_ast(LispVal *ast, Env *env) {
    if (!ast) return NULL;
    if (TYPE_SYMBOL == ast->type) {
        LispVal *binding = env_get(env, (LispSymbol *)ast);
        ref_dec(&ast);
        return binding;
    } else if (TYPE_CONS == ast->type) {
        LispCons *result = evlist(ast, env);
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
    ref_inc(fn); ref_inc(args);
    LispVal *result = apply(fn, args);
    ref_dec(&elts);
    ref_dec(&ast);
    return result;
}

Env* initial_env() {
    Env *global = env_new();
    char *key = alloc(7);
    memcpy(key, "apply*", 7);
    LispSymbol *apply_symbol = symbol_new(key);
    CFunction *lisp_apply = native_fun_new(2);
    lisp_apply->fn.f2 = apply;
    env_set(global, apply_symbol, (LispVal *)lisp_apply);

    key = alloc(2);
    key[0] = '+'; key[1] ='\0';
    LispSymbol *plus_symbol = symbol_new(key);
    CFunction *lisp_plus = native_fun_new(2);
    lisp_plus->fn.f2 = plus;
    env_set(global, plus_symbol, (LispVal *)lisp_plus);

    key = alloc(2);
    key[0] = '-'; key[1] ='\0';
    LispSymbol *minus_symbol = symbol_new(key);
    CFunction *lisp_minus = native_fun_new(2);
    lisp_minus->fn.f2 = minus;
    env_set(global, minus_symbol, (LispVal *)lisp_minus);

    key = alloc(2);
    key[0] = '*'; key[1] ='\0';
    LispSymbol *times_symbol = symbol_new(key);
    CFunction *lisp_times = native_fun_new(2);
    lisp_times->fn.f2 = times;
    env_set(global, times_symbol, (LispVal *)lisp_times);

    key = alloc(2);
    key[0] = '/'; key[1] ='\0';
    LispSymbol *slash_symbol = symbol_new(key);
    CFunction *lisp_div = native_fun_new(2);
    lisp_div->fn.f2 = divide;
    env_set(global, slash_symbol, (LispVal *)lisp_div);
    return global;
}

LispVal* READ() {
    // char *line = calloc(128,1);
    char line[128];
    printf("\nLISP> ");
    if (!fgets(line, sizeof(line), stdin)) {
        printf("\n");
        return NULL;
    }
    for (int i = strlen(line); '\0' == line[i] || isspace(line[i]); i--) {
        line[i] = '\0';
    }
    if ('\0' == line[0]) {
        // printf("Exiting gracefully?\n");
        return NULL;
    }
    LispVal *ast = read_str(line);
    return ast;
}

LispVal* rep(Env *env) {
    LispVal *ast  = READ();
    LispVal *expr = NULL;
    if (NULL == ast) return ast;

    expr = EVAL(ast, env);
    if ((ast != expr) && (ast->refcount > 0)) {
        ref_dec(&ast);
    }
    
    print_value(stdout, expr);
    printf("\n");
    return (LispVal *)expr;
}

int repl() {
    Env *env = initial_env();
    for(;;) {
        LispVal *expr = rep(env);
        if (NULL == expr) break;
        if (expr->refcount > 0) ref_dec(&expr);
    }
    env_free(env);
    return 0;
}

int eval_test1() {
    char src[] = "(+ 1 2)";
    LispVal *ast = read_str(src);
    
    Env *env = initial_env();
    LispVal *expr = EVAL(ast, env);

    printf("LISP> %s\n", src);
    print_value(stdout, expr); printf("\n");

    ref_dec(&expr);
    if (NULL != ast && ast->refcount > 0) ref_dec(&ast);
    env_free(env);
    return 0;
}

int eval_test2() {
    char src[] = "(+ 1 (* 2 3))";
    LispVal *ast = read_str(src);
    Env *env = initial_env();
    LispVal *expr = EVAL(ast, env);

    printf("LISP> %s\n", src);
    print_value(stdout, expr); printf("\n");

    ref_dec(&expr);
    if (NULL != ast && ast->refcount > 0) ref_dec(&ast);
    env_free(env);
    return 0;
}

void symbol_refdec_test() {
    char *name = malloc(32);
    snprintf(name, 16, "%s", "foobar");
    LispSymbol *symbol = symbol_new(name);
    ref_dec((LispVal **)&symbol);
    printf("Symbol == null ? %s\n",
           (NULL == symbol ? "true" : "false"));
}

int main() {
    // symbol_refdec_test();
    eval_test1();
    eval_test2();
    // repl();
    return 0;
}
