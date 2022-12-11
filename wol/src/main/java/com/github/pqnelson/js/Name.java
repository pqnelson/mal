package com.github.pqnelson.js;

/**
 * A Javascript Identifier.
 *
 * <p>A Javascript identifier consists of letters, digits, underscore {@code _},
 * and dollar sign {@code $}. Althought the ECMA standard allows Unicode
 * letters, we restrict attention to the Latin alphabet {@code a-zA-Z}.</p>
 */
class Name extends Expr {
    public final String value;

    public Name(String identifier) {
        this.value = identifier;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (null == obj) return false;
        if (this.getClass() != obj.getClass()) return false;
        Name rhs = (Name) obj;
        return this.value.equals(rhs.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public <T> T accept(final Visitor<T> visitor) {
        return visitor.visitName(this);
    }
}