package com.github.pqnelson.expr;

import java.math.BigInteger;

import com.github.pqnelson.Token;
import static com.github.pqnelson.TokenType.NUMBER;

public class BigInt extends com.github.pqnelson.expr.Number {
    private final BigInteger value;
    public BigInt(Token token, BigInteger value) {
        super(token);
        this.value = value;
    }
    @Override
    public BigInteger value() { return this.value; }

    @Override
    public String toString() { return this.value.toString(); }
    /**
     * Addition with possible overflow.
     */
    public BigInt add(Int rhs) {
        return rhs.add(this);
    }
    public BigInt add(BigInt rhs) {
        return new BigInt(new Token(NUMBER), this.value().add(rhs.value()));
    }
    public Float add(Float rhs) {
        return new Float(new Token(NUMBER), this.value().doubleValue() + rhs.value());
    }
    /**
     * Subtraction
     */
    public BigInt subtract(Int rhs) {
        return new BigInt(new Token(NUMBER), this.value().subtract(BigInteger.valueOf(rhs.value())));
    }
    public BigInt subtract(BigInt rhs) {
        return new BigInt(new Token(NUMBER), this.value().subtract(rhs.value()));
    }
    public Float subtract(Float rhs) {
        return new Float(new Token(NUMBER), this.value().doubleValue() - rhs.value());
    }
    /**
     * Multiplication
     */
    public BigInt multiply(Int rhs) {
        return rhs.multiply(this);
    }
    public BigInt multiply(BigInt rhs) {
        return new BigInt(new Token(NUMBER), this.value().multiply(rhs.value()));
    }
    public Float multiply(Float rhs) {
        return new Float(new Token(NUMBER), this.value().doubleValue() * rhs.value());
    }
    /**
     * Division
     */
    public BigInt divide(Int rhs) {
        return new BigInt(new Token(NUMBER), this.value().divide(BigInteger.valueOf(rhs.value())));
    }
    public BigInt divide(BigInt rhs) {
        return new BigInt(new Token(NUMBER), this.value().divide(rhs.value()));
    }
    public Float divide(Float rhs) {
        return new Float(new Token(NUMBER), this.value().doubleValue() / rhs.value());
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
}