#include <assert.h>
#include <ctype.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include "debug.h"
#include "memory.h"
#include "scanner.h"

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

static Token* string_token(Scanner *scanner, size_t length, size_t line) {
    assert(NULL != scanner);
    assert(NULL != scanner->position);
    assert('"' == scanner->position[0]);
    assert('"' == scanner->position[length-1]);
    return token_new(TOKEN_STRING, scanner->position, line, length);
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

bool token_is_string(Token *this) {
    return (NULL != this) && (TOKEN_STRING == this->type);
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
    TRACE("trying to free scanner\n");
    if (NULL == scanner) return;
    
    // if (NULL != scanner->start) free(scanner->start);

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

static void comment(Scanner *scanner) {
    assert(NULL != scanner);
    assert(NULL != scanner->position);
    assert(';' == *scanner->position);
    while (scanner_has_next(scanner) && ('\n' != *scanner->position)) {
        scanner->position++;
    }
    //@ assert !scanner_has_next(scanner) || ('\n' == *scanner->position);
    if (scanner_has_next(scanner)) {
        assert('\n' == *scanner->position);
        scanner->position++;
        scanner->line++;
    }
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

static void skip_ws_comment(Scanner *scanner) {
    assert(NULL != scanner);
    assert(NULL != scanner->position);
    while(scanner_has_next(scanner) &&
          ((';' == *scanner->position) || isspace(*scanner->position))) {
        if(';' == *scanner->position) comment(scanner);
        
        skip_ws(scanner);
    }
}

static bool isreserved(char c) {
    return ('(' == c) || (')' == c) || (';' == c);
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

static bool isNumber(Scanner *scanner) {
    assert(NULL != scanner);
    assert(NULL != scanner->position);
    return isdigit(*scanner->position) ||
        ((('+' == *scanner->position) || ('-' == *scanner->position)) &&
         isdigit(scanner->position[1]));
}

// TODO: handle complex numbers? Or delegate it to reader macro?
static Token* number(Scanner *scanner) {
    assert(NULL != scanner);
    assert(NULL != scanner->position);
    assert(isNumber(scanner));
    bool has_seen_decimal_point = false;
    size_t length = 1;
    while (isdigit(scanner->position[length])) {
        length++;
    }
    if (('.' == scanner->position[length]) &&
        isdigit(scanner->position[length+1])) {
        has_seen_decimal_point = true;
        length++;
        while (isdigit(scanner->position[length])) {
            length++;
        }
    }
    assert(!isdigit(scanner->position[length]));
    TokenType type = (has_seen_decimal_point ? TOKEN_FLOAT : TOKEN_INTEGER);
    Token *result = token_new(type, scanner->position, scanner->line, length);
    debug("Scanner::number() position[length] = '%c'\n",
           scanner->position[length]);
    debug("Scanner::number() lexeme = '%.*s'\n",
           (int)length, scanner->position);
    scanner->current_token = result;
    return result;
}

static bool end_of_string(Scanner *scanner, int length) {
    if ('"' == scanner->position[length]) {
        return ('\\' != scanner->position[length-1]);
    }
    return false;
}

static Token* scan_string(Scanner *scanner) {
    assert(NULL != scanner);
    assert('"' == *scanner->position);
    size_t line = scanner->line;
    size_t length = 1;
    for(; !end_of_string(scanner, length); length++) {
        if ('\n' == scanner->position[length]) scanner->line++;
        if ('\0' == scanner->position[length]) {
            int delta = (length > 32 ? 32 : (int)length);
            eprintf("ERROR runaway string starting on line %lu: \"%.*s%s\"\n",
                    line,
                    delta,
                    scanner->position,
                    (delta == 32 ? "..." : ""));
            abort();
        }
    }
    length++;
    debug("string_token given:\t%.*s\n", (int)length, scanner->position);
    return string_token(scanner, length, line);
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
    skip_ws_comment(scanner);
    //@ assert !isspace(*scanner->position) || !scanner_has_next(scanner);
    if (!scanner_has_next(scanner)) {
        //@ assert '\0' == scanner->position[0]
        result = eof_token(scanner);
        scanner->current_token = result;
        return result;
    }
    
    //@ assert !isspace(scanner->position[0]);
    switch (scanner->position[0]) {
    case '(':
        TRACE("found left parentheses\n");
        scanner->current_token = left_paren(scanner);
        return scanner->current_token;
    case ')':
        TRACE("found right parentheses\n");
        scanner->current_token = right_paren(scanner);
        return scanner->current_token;
    case '"':
        scanner->current_token = scan_string(scanner);
        return scanner->current_token;
    default:
        break;
    }
    TRACE("lexeme start '%s'\n", scanner->position);
    if (isNumber(scanner)) {
        WARN("Found number?\n");
        return number(scanner);
    }
    length = length_of_token(scanner);
    result = token_new(TOKEN_ATOM, scanner->position, scanner->line, length);
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

