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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (null == obj) return false;
        if (obj.getClass() != this.getClass()) return false;
        Str rhs = (Str)obj;
        return this.value().equals(rhs.value());
    }

    @Override
    public int hashCode() {
        return this.value().hashCode();
    }
}