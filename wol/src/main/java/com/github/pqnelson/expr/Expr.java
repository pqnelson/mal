package com.github.pqnelson.expr;

/**
 * The abstract syntax tree for Lisp.
 *
 * This more or less corresponds to "types.js".
 *
 */
public abstract class Expr {
    public abstract <T> T accept(final Visitor<T> visitor);

    public final boolean isBigInt() {
        return BigInt.class.isInstance(this);
    }
    public final boolean isFloat() {
        return Float.class.isInstance(this);
    }
    public final boolean isFunction() {
        return Fun.class.isInstance(this);
    }
    public final boolean isInt() {
        return Int.class.isInstance(this);
    }
    public final boolean isKeyword() {
        return Keyword.class.isInstance(this);
    }
    public final boolean isList() {
        return Seq.class.isInstance(this);
    }
    public boolean isFalsy() {
        return this.isLiteral() && ((Literal) this).isFalsy();
    }
    public final boolean isLiteral() {
        return Literal.class.isInstance(this);
    }
    public final boolean isMap() {
        return Map.class.isInstance(this);
    }
    public final boolean isNumber() {
        return com.github.pqnelson.expr.Number.class.isInstance(this);
    }
    public boolean isNil() {
        return isLiteral() && ((Literal) this).isNil();
    }
    public final boolean isString() {
        return Str.class.isInstance(this);
    }
    public final boolean isSymbol() {
        return Symbol.class.isInstance(this);
    }
    public final boolean isVector() {
        return Vector.class.isInstance(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj) {
            return false;
        }
        return false;
    }
    public String type() {
        return "Expr";
    }
}