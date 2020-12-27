#include <assert.h>
#include <ctype.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include "scanner.h"
#include "memory.h"

/*@ assumes \valid(lexeme + (0 .. length));
  @ behavior no_alloc:
  @   assumes !is_allocable(sizeof(struct token));
  @   exits status: \exit_status != EXIT_SUCCESS;
  @   ensures never_terminates: \false;
  @ behavior default:
  @   assumes is_allocable(sizeof(struct token));
  @   allocates \result;
  @   assigns \result->lexeme \from lexeme;
  @   ensures \valid(\result);
  @ complete behaviors;
  @ disjoint behaviors;
  @*/
Token* token_new(TokenType type, char *lexeme, size_t line, size_t length) {
    assert (NULL != lexeme);
    assert ((length > 0) || (TOKEN_EOF == type));
    assert (line > 0);
    Token *token = alloc(sizeof(*token));
    
    if (NULL == token) {
        abort();
    }
    token->type   = type;
    token->lexeme = lexeme;
    token->line   = line;
    token->length = length;
    return token;
}

static Token* left_paren(Scanner *scanner) {
    assert(NULL != scanner);
    assert(NULL != scanner->position);
    assert('(' == *scanner->position);
    return token_new(TOKEN_LPAREN, scanner->position, scanner->line, 1);
}

static Token* right_paren(Scanner *scanner) {
    assert(NULL != scanner);
    assert(NULL != scanner->position);
    assert(')' == *scanner->position);
    return token_new(TOKEN_RPAREN, scanner->position, scanner->line, 1);
}

static Token* eof_token(Scanner *scanner) {
    assert(NULL != scanner);
    assert(NULL != scanner->position);
    assert('\0' == *scanner->position);
    return token_new(TOKEN_EOF, scanner->position, scanner->line, 0);
}

void token_free(Token *this) {
    if (NULL == this) return;
    /* Since the token doesn't malloc its own copy of the lexeme,
       we do not free the lexeme */
    free(this);
    this = NULL;
}

void token_print(FILE *stream, Token *this) {
    if (!this) return;
    fprintf(stream, "%.*s", (int)(this->length), this->lexeme);
}

bool token_is_l_paren(Token *this) {
    return (NULL != this) && (TOKEN_LPAREN == this->type);
}

bool token_is_r_paren(Token *this) {
    return (NULL != this) && (TOKEN_RPAREN == this->type);
}

bool token_is_eof(Token *this) {
    return (NULL != this) && (TOKEN_EOF == this->type);
}

/************************************************************************/

Scanner* scanner_new(char *src) {
    Scanner *scanner = alloc(sizeof(*scanner));
    if (NULL == scanner) {
        abort();
    }
    scanner->start = src;
    scanner->position = src;
    scanner->line = 1;
    scanner->offset = 1;
    scanner->current_token = NULL;
    return scanner;
}

void scanner_free(Scanner *scanner) {
    if (NULL == scanner) return;
    
    if (NULL != scanner->start) free(scanner->start);

    if (NULL != scanner->current_token) {
        token_free(scanner->current_token);
        scanner->current_token = NULL;
    }

    assert (NULL == scanner->current_token);
    free(scanner);
}

bool scanner_has_next(Scanner *scanner) {
    return '\0' != *scanner->position;
}

static void skip_ws(Scanner *scanner) {
    assert(NULL != scanner);
    assert(NULL != scanner->position);
    while (isspace(*scanner->position) && scanner_has_next(scanner)) {
        if ('\n' == *scanner->position) {
            scanner->line++;
            scanner->offset = 1;
        } else {
            scanner->offset++;
        }
        scanner->position++;
    }
    //@ assert !isspace(*scanner->position) || !scanner_has_next(scanner);
}

static bool isreserved(char c) {
    return ('(' == c) || (')' == c);
}

static size_t length_of_token(Scanner *scanner) {
    assert(NULL != scanner);
    size_t length;
    for(length = 1;
        ('\0' != scanner->position[length]) &&
            !isspace(scanner->position[length]) &&
            !isreserved(scanner->position[length]);
        length++);
    return length;
}

/*@ assumes \valid(scanner);
  @ behavior already_peeked:
  @   assumes \valid(scanner->current_token);
  @   assigns \nothing;
  @   ensures \result == scanner->current_token;
  @ behavior nothing_peeked:
  @   assumes \null == scanner->current_token;
  @   allocates \result;
  @   assigns \result \from indirect:\old(scanner->position);
  @   allocates \result;
  @   ensures \valid(\result);
  @ disjoint behaviors;
  @ complete behaviors;
  @*/
Token* scanner_peek(Scanner *scanner) {
    assert (NULL != scanner);
    if (NULL != scanner->current_token) return scanner->current_token;

    Token *result = NULL;
    size_t length = 0;
    skip_ws(scanner);
    //@ assert !isspace(*scanner->position) || !scanner_has_next(scanner);
    if (scanner_has_next(scanner)) {
        //@ assert !isspace(scanner->position[0]);
        switch (scanner->position[0]) {
        case '(':
            result = left_paren(scanner);
            break;
        case ')':
            result = right_paren(scanner);
            break;
        default:
            length = length_of_token(scanner);
            result = token_new(TOKEN_ATOM, scanner->position, scanner->line, length);
        }
    } else {
        //@ assert '\0' == scanner->position[0]
        result = eof_token(scanner);
    }
    //@ assert NULL != result;
    scanner->current_token = result;
    //@ assert result == scanner->current_token;
    return result;
}

/*@
  @ requires \valid(scanner);
  @ assigns scanner->current_token;
  @ ensures \old(scanner->position) + \result->length == scanner->position;
  @ ensures \null == scanner->current_token;
  @*/
Token* scanner_next(Scanner *scanner) {
    Token *result = scanner_peek(scanner);
    scanner->position += (result->length);
    scanner->current_token = NULL;
    return result;
}

