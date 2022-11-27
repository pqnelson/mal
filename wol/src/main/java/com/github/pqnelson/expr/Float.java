package com.github.pqnelson.expr;

import java.math.BigInteger;

import com.github.pqnelson.Token;
import static com.github.pqnelson.TokenType.NUMBER;

public class Float extends com.github.pqnelson.expr.Number {
    private final double value;
    public Float(double value) {
        this(new Token(NUMBER, Double.toString(value), value), value);
    }
    public Float(Token token, double value) {
        super(token);
        this.value = value;
    }
    @Override
    public Double value() { return Double.valueOf(this.value); }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
    /**
     * Addition with possible overflow.
     */
    public Float add(Int rhs) {
        return rhs.add(this);
    }
    public Float add(Float rhs) {
        return new Float(new Token(NUMBER), this.value() + rhs.value());
    }
    public Float add(BigInt rhs) {
        return new Float(new Token(NUMBER), this.value() + rhs.value().doubleValue());
    }
    /**
     * Subtraction
     */
    public Float subtract(Int rhs) {
        return new Float(new Token(NUMBER), this.value() - rhs.value());
    }
    public Float subtract(BigInt rhs) {
        return new Float(new Token(NUMBER), this.value() - rhs.value().doubleValue());
    }
    public Float subtract(Float rhs) {
        return new Float(new Token(NUMBER), this.value() - rhs.value());
    }
    /**
     * Multiplication
     */
    public Float multiply(Int rhs) {
        return rhs.multiply(this);
    }
    public Float multiply(BigInt rhs) {
        return new Float(new Token(NUMBER), this.value() * rhs.value().doubleValue());
    }
    public Float multiply(Float rhs) {
        return new Float(new Token(NUMBER), this.value() * rhs.value());
    }
    /**
     * Division
     */
    public Float divide(Int rhs) {
        return new Float(new Token(NUMBER), this.value()/rhs.value());
    }
    public Float divide(BigInt rhs) {
        return new Float(new Token(NUMBER), this.value()/(rhs.value().doubleValue()));
    }
    public Float divide(Float rhs) {
        return new Float(new Token(NUMBER), this.value() / rhs.value());
    }

    @Override
    public Number add(Number rhs) {
        if (rhs.isFloat()) return this.add((Float)rhs);
        if (rhs.isInt()) return this.add((Int)rhs);
        return this.add((BigInt)rhs);
    }
    @Override
    public Number subtract(Number rhs) {
        if (rhs.isFloat()) return this.subtract((Float)rhs);
        if (rhs.isInt()) return this.subtract((Int)rhs);
        return this.subtract((BigInt)rhs);
    }
    @Override
    public Number divide(Number rhs) {
        if (rhs.isFloat()) return this.divide((Float)rhs);
        if (rhs.isInt()) return this.divide((Int)rhs);
        return this.divide((BigInt)rhs);
    }
    @Override
    public Number multiply(Number rhs) {
        if (rhs.isFloat()) return this.multiply((Float)rhs);
        if (rhs.isInt()) return this.multiply((Int)rhs);
        return this.multiply((BigInt)rhs);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (null == obj) return false;
        if (obj.getClass() != this.getClass()) return false;
        Float rhs = (Float)obj;
        return Double.valueOf(this.value).equals(Double.valueOf(rhs.value()));
    }
    @Override
    public int hashCode() { return Double.hashCode(this.value()); }
}
