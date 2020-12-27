#ifndef TOKENIZER_H
#define SCANNER_H

#ifndef STDIO_H
#define STDIO_H
#include <stdio.h>
#endif /* STDIO_H */

typedef enum {
    TOKEN_LPAREN,
    TOKEN_RPAREN,
    TOKEN_ATOM,
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

typedef struct Scanner {
    char *start, *position;
    size_t line;
    size_t offset;
    Token *current_token;
} Scanner;

Scanner* scanner_new(char *src);
void scanner_free(Scanner *scanner);
bool scanner_has_next(Scanner *scanner);
Token* scanner_next(Scanner *scanner);

#endif /* SCANNER_H */
