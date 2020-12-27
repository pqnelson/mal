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
    LispVal *result = NULL;
    
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
    LispVal *result = NULL;
    Scanner *scanner = scanner_new(src);
    // All the work done in read_scanner()
    result = read_scanner(scanner);
    scanner_free(scanner);
    return result;
}

static LispSymbol* symbol(Scanner *scanner) {
    assert (NULL != scanner);
    assert (!token_is_l_paren(scanner_peek(scanner)));
    assert (!token_is_r_paren(scanner_peek(scanner)));
    Token *token = scanner_next(scanner);
    char *name = alloc(token->length + 1);
    
    if (NULL == name) abort();

    memcpy(name, token->lexeme, token->length);
    name[token->length] = '\0';
    
    LispSymbol *symbol = symbol_new(name);

    token_free(token);
    return symbol;
}

static LispInt* integer(Scanner *scanner) {
    Token* token = scanner_next(scanner);
    if (NULL == token->lexeme) {
        eprintf("NULL lexeme encountered in integer: %s\n", token->lexeme);
        abort();
    }
    long long value = strtoll(token->lexeme, NULL, 10);
    token_free(token);
    return int_new(value);
}

static LispFloat* real(Scanner *scanner) {
    Token* token = scanner_next(scanner);
    if (NULL == token->lexeme) {
        eprintf("NULL lexeme encountered in float: %s\n", token->lexeme);
        abort();
    }
    double value = strtod(token->lexeme, NULL);
    token_free(token);
    return float_new(value);
}

static String* string(Scanner *scanner) {
    assert (NULL != scanner);
    assert (!token_is_l_paren(scanner_peek(scanner)));
    assert (!token_is_r_paren(scanner_peek(scanner)));
    Token *token = scanner_next(scanner);    
    String *string = string_new(token->lexeme, token->length);

    token_free(token);
    return string;
}

static LispVal* read_atom(Scanner *scanner) {
    assert (NULL != scanner);
    assert (!token_is_l_paren(scanner_peek(scanner)));
    assert (!token_is_r_paren(scanner_peek(scanner)));
    Token *token = scanner_peek(scanner);
    switch(token->type) {
    case TOKEN_ATOM:
        return (LispVal *)symbol(scanner);
    case TOKEN_INTEGER:
        return (LispVal *)integer(scanner);
    case TOKEN_FLOAT:
        return (LispVal *)real(scanner);
    case TOKEN_STRING:
        return (LispVal *)string(scanner);
    /* Other atoms read here */
    default:
        eprintf("read_atom() error: unknown token type %d, lexeme = '%.*s'\n",
                token->type, (int)token->length, token->lexeme);
        abort();
    }
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
    LispCons *result = NULL;
    LispCons *current = NULL;
    // preconditions
    assert_match(scanner, TOKEN_LPAREN);
    guard_against_runaways(scanner, start_line, result, start);
    
    // case: nil
    if (token_is_r_paren(scanner_peek(scanner))) {
        assert_match(scanner, TOKEN_RPAREN);
        return (LispCons *)&nil;
    }
    
    // default case
    current = result = make_cons(scanner);
    while (!token_is_r_paren(scanner_peek(scanner))) {
        guard_against_runaways(scanner, start_line, result, start);

        LispCons *next = make_cons(scanner);
        current->cdr = (LispVal *)next;
        current = next;
        
        assert (NULL == current->cdr);
    }
    //@ assert ')' == *scanner->position;
    assert_match(scanner, TOKEN_RPAREN);
    return result;
}
