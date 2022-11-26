package com.github.pqnelson.expr;

import com.github.pqnelson.Token;
import com.github.pqnelson.TokenType;

public class Str extends Literal {
    public Str(Token token) {
        super(token);
    }
    public Str(String s) {
        super(new Token(TokenType.STRING, s));
    }

    @Override
    public String value() { return this.token.lexeme; }

    @Override
    public String toString() { return this.value(); }
}