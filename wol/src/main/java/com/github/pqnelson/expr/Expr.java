package com.github.pqnelson.expr;

/**
 * The abstract syntax tree for Lisp.
 *
 * This more or less corresponds to "types.js".
 *
 */
public abstract class Expr {
    public abstract <T> T accept(Visitor<T> visitor);
    public boolean isFunction() { return Fun.class.isInstance(this); }
    public boolean isInt() { return isLiteral() && ((Literal)this).isInt(); }
    public boolean isKeyword() { return Keyword.class.isInstance(this); }
    public boolean isList() { return Seq.class.isInstance(this); }
    public boolean isLiteral() { return Literal.class.isInstance(this); }
    public boolean isMap() { return Map.class.isInstance(this); }
    public boolean isNumber() { return com.github.pqnelson.expr.Number.class.isInstance(this); }
    public boolean isNil() { return isLiteral() && ((Literal)this).isNil(); }
    public boolean isString() { return isLiteral() && ((Literal)this).isString(); }
    public boolean isSymbol() { return Symbol.class.isInstance(this); }
    public boolean isVector() { return Vector.class.isInstance(this); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (null == obj) return false;
        return false;
    }
    public String type() {
        return "Expr";
    }
}