package com.github.pqnelson.js;

public class Null extends JsExpr implements PrimitiveValue {
    public static final Null instance = new Null();

    private Null() { }

    @Override
    public <T> T accept(final ExprVisitor<T> visitor) {
        return visitor.visitNull(this);
    }

    @Override
    public boolean isFalsy() {
        return true;
    }
}
