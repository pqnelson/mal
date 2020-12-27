#include <assert.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "debug.h"
#include "memory.h"
#include "types.h"
#include "scanner.h"

static LispVal* read_atom(Scanner *scanner);
static LispCons* read_list(Scanner *scanner);

static LispVal* read_scanner(Scanner *scanner) {
    assert (NULL != scanner);
    LispVal* result = NULL;
    if (NULL == scanner_peek(scanner)) {
        result = (LispVal *)symbol_new("nil");
    } else if (token_is_l_paren(scanner_peek(scanner))) {
        result = (LispVal *)read_list(scanner);
    } else {
        result = read_atom(scanner);
    }
    return result;
}

LispVal* read_str(char *src) {
    if (NULL == src) return NULL;
    LispVal* result = NULL;
    Scanner *scanner = scanner_new(src);
    result = read_scanner(scanner);
    scanner_free(scanner);
    return result;
}

static LispSymbol* read_symbol(Scanner *scanner) {
    assert (NULL != scanner);
    assert (!token_is_l_paren(scanner_peek(scanner)));
    assert (!token_is_r_paren(scanner_peek(scanner)));
    Token* token = scanner_next(scanner);
    char *name = alloc(token->length + 1);
    
    if (NULL == name) abort();

    memcpy(name, token->lexeme, token->length);
    name[token->length] = '\0';
    
    LispSymbol *symbol = symbol_new(name);

    token_free(token);
    return symbol;
}

static LispVal* read_atom(Scanner *scanner) {
    assert (NULL != scanner);
    assert (!token_is_l_paren(scanner_peek(scanner)));
    assert (!token_is_r_paren(scanner_peek(scanner)));
    /* Other atoms read here */
    LispSymbol *result = read_symbol(scanner);
    return (LispVal *)result;
}

static int min(int a, int b) {
    if (a < b) return a;
    else       return b;
}

#define ERROR_MESSAGE_CONTEXT_SIZE 32
/*
 * In case we encounter a runaway list, dump the core, and print to
 * the error stream what has happened and where.
 * 
 * This is actually much too harsh, for a REPL we would want to
 * just rollback the expressions read.
 */
static void guard_against_runaways(Scanner *scanner,
                                   int start_line,
                                   LispCons *result,
                                   char *start) {
    if (token_is_eof(scanner_peek(scanner))) {
        // A list didn't close, so we have to dump the core and terminate
        eprintf("Error: runaway argument starting at line %d, starting at:\n",
                start_line);
        int delta = scanner->position - start;
        int length = min(ERROR_MESSAGE_CONTEXT_SIZE, delta);
        eprintf("\t'%.*s%s'\n", length, start, (length < delta ? "..." : ""));
        eprintf("Freeing memory, bailing out.\n");
        cons_free(result);
        scanner_free(scanner);
        // if (is_in_repl()) return;
        abort();
    }
}

void assert_match(Scanner *scanner, TokenType type) {
    Token *token = scanner_next(scanner);
    assert(token->type == type);
    token_free(token);
}

static LispCons* make_cons(Scanner *scanner) {
    LispVal *car = read_scanner(scanner);
    LispCons *next = cons(car, NULL);
    return next;
}

static LispCons* read_list(Scanner *scanner) {
    assert (NULL != scanner);
    char *start = scanner->position;
    int start_line = scanner_peek(scanner)->line;
    LispCons* result = NULL;
    LispCons* current = NULL;
    // preconditions
    assert_match(scanner, TOKEN_LPAREN);
    guard_against_runaways(scanner, start_line, result, start);
    
    // first iteration
    if (!token_is_r_paren(scanner_peek(scanner))) {
        current = result = make_cons(scanner);
    } else {
        // case: nil
        result = cons(NULL, NULL);
    }
    
    while (!token_is_r_paren(scanner_peek(scanner))) {
        guard_against_runaways(scanner, start_line, result, start);

        current->cdr = (LispVal *)make_cons(scanner);
        current = (LispCons *)current->cdr;
        
        assert (NULL == current->cdr);
    }
    //@ assert ')' == *scanner->position;
    assert_match(scanner, TOKEN_RPAREN);
    return result;
}
