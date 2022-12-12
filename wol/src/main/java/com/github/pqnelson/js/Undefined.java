package com.github.pqnelson.js;

public class Undefined extends JsExpr implements PrimitiveValue {
    public static final Undefined instance = new Undefined();

    private Undefined() { }

    @Override
    public <T> T accept(final ExprVisitor<T> visitor) {
        return visitor.visitUndefined(this);
    }

    @Override
    public boolean isFalsy() {
        return true;
    }
}
