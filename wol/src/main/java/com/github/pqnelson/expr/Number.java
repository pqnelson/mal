package com.github.pqnelson.expr;

import com.github.pqnelson.Token;

public abstract class Number extends Literal implements Comparable<Number> {
    public Number(final Token token) {
        super(token);
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