package com.github.pqnelson.expr;

import com.github.pqnelson.Token;
import com.github.pqnelson.TokenType;

public class Symbol extends Expr {
    final Token identifier;

    public Symbol(String name) {
        this.identifier = new Token(TokenType.IDENTIFIER, name);
    }

    public Symbol(Token identifier) {
        this.identifier = identifier;
    }

    public final String name() {
        return this.identifier.lexeme;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitSymbol(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (null == obj) return false;
        if (obj.getClass() != this.getClass()) return false;
        Symbol rhs = (Symbol)obj;
        // Since Symbols are used for any identifier (like "do" and "fn*"),
        // check the TokenTypes are the same
        return (this.identifier.type == rhs.identifier.type) &&
            (this.name().equals(rhs.name()));
    }
}