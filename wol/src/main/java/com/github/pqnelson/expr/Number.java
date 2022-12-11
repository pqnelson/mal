package com.github.pqnelson.expr;

public abstract class Number extends Literal implements Comparable<Number> {
    Number(final Object value) {
        super(value);
    }
    public abstract com.github.pqnelson.expr.Number add(com.github.pqnelson.expr.Number rhs);
    public abstract com.github.pqnelson.expr.Number subtract(com.github.pqnelson.expr.Number rhs);
    public abstract com.github.pqnelson.expr.Number multiply(com.github.pqnelson.expr.Number rhs);
    public abstract com.github.pqnelson.expr.Number divide(com.github.pqnelson.expr.Number rhs);

    @Override
    public String type() {
        return "wol.Number";
    }
    public abstract int compareTo(Number o);
}
