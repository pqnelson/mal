package com.github.pqnelson.js;

public class Null extends Expr implements PrimitiveValue {
    public static final Null instance = new Null();

    private Null() { }

    @Override
    public <T> T accept(final Visitor<T> visitor) {
        return visitor.visitNull(this);
    }

    @Override
    public boolean isFalsy() {
        return true;
    }
}