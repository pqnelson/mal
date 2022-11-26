package com.github.pqnelson.expr;

import com.github.pqnelson.Token;

public class Keyword extends Expr {
    final Token identifier;

    public Keyword(Token identifier) {
        this.identifier = identifier;
    }

    public final String name() {
        return this.identifier.lexeme;
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
}