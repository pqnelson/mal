package com.github.pqnelson.expr;

import com.github.pqnelson.Token;
import com.github.pqnelson.TokenType;

public class Keyword extends Expr {
    final Token identifier;

    public Keyword(String name) {
        this(new Token(TokenType.KEYWORD, name));
    }
    public Keyword(Token identifier) {
        this.identifier = identifier;
    }

    public final String name() {
        return this.identifier.lexeme;
    }

    public final int hashCode() {
        return this.name().hashCode() + 0x9e3779b9;
    }

    @Override
    public String toString() {
        return ":"+this.name();
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitKeyword(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (null == obj) return false;
        if (obj.getClass() != this.getClass()) return false;
        Keyword rhs = (Keyword)obj;
        return (this.name().equals(rhs.name()));
    }

    @Override public String type() {
        return "Keyword";
    }

    public Symbol symbol() {
        return new Symbol(this.name());
    }
}