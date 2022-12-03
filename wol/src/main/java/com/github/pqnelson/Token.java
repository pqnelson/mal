package com.github.pqnelson;

/**
 * A token is a lexeme with some useful metadata.
 */
public final class Token {
    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final int line;

    public Token(final TokenType tokenType) {
        this(tokenType, null, null, 0);
    }
    public Token(final TokenType tokenType,
                 final String tokenLexeme) {
        this(tokenType, tokenLexeme, null, 0);
    }
    public Token(final TokenType tokenType,
                 final String tokenLexeme,
                 final Object tokenLiteral) {
        this(tokenType, tokenLexeme, tokenLiteral, 0);
    }
    public Token(final TokenType tokenType,
                 final String tokenLexeme,
                 final Object tokenLiteral,
                 final int tokenLine) {
        this.type    = tokenType;
        this.lexeme  = tokenLexeme;
        this.literal = tokenLiteral;
        this.line    = tokenLine;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}