package com.github.pqnelson.expr;

import com.github.pqnelson.Token;

public abstract class Number extends Literal implements Comparable<Number> {
    public Number(Token token) {
        super(token);
    }
    public abstract com.github.pqnelson.expr.Number add(com.github.pqnelson.expr.Number rhs);
    public abstract com.github.pqnelson.expr.Number subtract(com.github.pqnelson.expr.Number rhs);
    public abstract com.github.pqnelson.expr.Number multiply(com.github.pqnelson.expr.Number rhs);
    public abstract com.github.pqnelson.expr.Number divide(com.github.pqnelson.expr.Number rhs);
    public boolean isBigInt() { return BigInt.class.isInstance(this); }
    public boolean isInt() { return Int.class.isInstance(this); }
    public boolean isFloat() { return Float.class.isInstance(this); }

    @Override
    public String type() {
        return "wol.Number";
    }
    public abstract int compareTo(Number o);
}