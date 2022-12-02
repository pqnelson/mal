package com.github.pqnelson.expr;

import com.github.pqnelson.Token;
import com.github.pqnelson.TokenType;

public class Symbol extends Expr implements IObj {
    final Token identifier;
    private Map meta = null;
    public static final Symbol DO = new Symbol(new Token(TokenType.DO, "do"));
    public static final Symbol FN_STAR = new Symbol(new Token(TokenType.FN_STAR, "fn*"));
    public static final Symbol IF = new Symbol(new Token(TokenType.IF, "if"));
    public static final Symbol QUASIQUOTE = new Symbol(new Token(TokenType.BACKTICK, "quasiquote"));
    public static final Symbol QUOTE = new Symbol(new Token(TokenType.QUOTE, "quote"));
    public static final Symbol SPLICE = new Symbol(new Token(TokenType.SPLICE, "splice"));
    public static final Symbol UNQUOTE = new Symbol(new Token(TokenType.UNQUOTE, "unquote"));
    public Symbol(String name) {
        this.identifier = new Token(TokenType.IDENTIFIER, name);
    }

    public Symbol(Token identifier) {
        this.identifier = identifier;
    }

    public Symbol(Symbol s) {
        this.identifier = s.identifier;
    }

    public boolean isSpecialForm() {
        return TokenType.IDENTIFIER != this.identifier.type;
    }

    public boolean isDef() {
        return TokenType.DEF == this.identifier.type;
    }

    @Override
    public Map meta() {
        return this.meta;
    }

    @Override
    public IObj withMeta(Map meta) {
        if (this.meta.equals(meta)) return this;

        Symbol result = new Symbol(this);
        result.meta = meta;
        return result;
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

    public final int hashCode() {
        return this.name().hashCode();
    }

    @Override
    public String type() {
        return "Symbol";
    }

    @Override
    public String toString() { return this.name(); }
}