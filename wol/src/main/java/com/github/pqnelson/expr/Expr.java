package com.github.pqnelson.expr;

/**
 * The abstract syntax tree for Lisp.
 *
 * This more or less corresponds to "types.js".
 *
 * @TODO HashMap
 * @TODO Set
 * @TODO Atom(?)
 */
public abstract class Expr {
    public abstract <T> T accept(Visitor<T> visitor);
    public boolean isFunction() { return Fun.class.isInstance(this); }
    public boolean isList() { return Seq.class.isInstance(this); }
    public boolean isSymbol() { return Symbol.class.isInstance(this); }
    public boolean isVector() { return Vector.class.isInstance(this); }
    public boolean isLiteral() { return Literal.class.isInstance(this); }
}