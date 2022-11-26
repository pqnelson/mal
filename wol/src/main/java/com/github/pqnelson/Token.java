package com.github.pqnelson;

/**
 * A token is a lexeme with some useful metadata.
 */
class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    Token(TokenType type) {
        this(type, null, null, 0);
    }
    Token(TokenType type, String lexeme) {
        this(type, lexeme, null, 0);
    }
    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}