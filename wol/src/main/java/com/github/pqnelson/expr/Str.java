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
    public String value() {
        return this.token.lexeme;
    }

    @Override
    public String toString() {
        return this.token.lexeme;
    }

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

    public Str substring(int start, int end) {
        return new Str(this.value().substring(start, end));
    }

    public Expr seq() {
        if (this.value().equals("")) return Literal.NIL;
        Seq letters = new Seq();
        for (int i = 0; i < this.value().length(); i++) {
            letters.conj(this.substring(i, i+1));
        }
        return letters;
    }

    @Override
    public String type() {
        return "Str";
    }
}