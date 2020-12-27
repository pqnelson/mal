/**
 * Lisp scanner.
 * 
 * Lazily produce a sequence of @c{Token} objects on demand.
 * 
 * Future directions include using a flyweight pattern for unique
 * strings/lexemes.
 */
#ifndef SCANNER_H
#define SCANNER_H

typedef enum {
    TOKEN_LPAREN,
    TOKEN_RPAREN,
    TOKEN_ATOM,
    TOKEN_INTEGER,
    TOKEN_FLOAT,
    TOKEN_EOF
} TokenType;

typedef struct token {
    TokenType type;
    char *lexeme;
    size_t line;
    size_t length;
} Token;

Token* token_new(TokenType type, char *lexeme, size_t line, size_t length);
void token_free(Token *this);
void token_print(FILE *stream, Token *this);
bool token_is_l_paren(Token *this);
bool token_is_r_paren(Token *this);
bool token_is_eof(Token *this);

typedef struct Scanner {
    char *start, *position;
    size_t line;
    size_t offset;
    Token *current_token;
} Scanner;

Scanner* scanner_new(char *src);
void scanner_free(Scanner *scanner);
bool scanner_has_next(Scanner *scanner);
Token* scanner_peek(Scanner *scanner);
Token* scanner_next(Scanner *scanner);

#endif /* SCANNER_H */
