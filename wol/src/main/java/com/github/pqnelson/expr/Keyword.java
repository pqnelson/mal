package com.github.pqnelson.expr;

public class Keyword extends Expr {
    private final String identifier;

    public Keyword(final String name) {
        this.identifier = name;
    }
    @Override
    public Keyword clone() {
        return this;
    }

    public final String name() {
        return this.identifier;
    }

    private static final int MAGIC = 0x9e3779b9;

    public final int hashCode() {
        return this.name().hashCode() + MAGIC;
    }

    @Override
    public String toString() {
        return ":" + this.name();
    }

    @Override
    public <T> T accept(final Visitor<T> visitor) {
        return visitor.visitKeyword(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        Keyword rhs = (Keyword) obj;
        return (this.name().equals(rhs.name()));
    }

    @Override public String type() {
        return "Keyword";
    }

    public Symbol symbol() {
        return new Symbol(this.name());
    }
}
