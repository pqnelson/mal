package com.github.pqnelson.expr;

import java.math.BigInteger;

import com.github.pqnelson.Token;
import static com.github.pqnelson.TokenType.NUMBER;

public class Int extends com.github.pqnelson.expr.Number {
    private final long value;
    public Int(long value) {
        this(new Token(NUMBER, Long.toString(value), value), value);
    }
    public Int(Token token, long value) {
        super(token);
        this.value = value;
    }

    @Override
    public Long value() { return this.value; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (null == obj) return false;
        if (obj.getClass() != this.getClass()) return false;
        Int rhs = (Int)obj;
        return (this.value().equals(rhs.value()));
    }

    @Override
    public final int hashCode() {
        return Long.hashCode(this.value());
    }


    @Override
    public String toString() {
        return Long.toString(this.value, 10);
    }

    public String toString(int radix) {
        return Long.toString(this.value, radix);
    }

    public String toBinaryString() {
        return Long.toHexString(this.value);
    }

    public String toOctalString() {
        return Long.toOctalString(this.value);
    }

    public String toHexString() {
        return Long.toHexString(this.value);
    }

    /**
     * Addition with possible overflow.
     */
    public Number add(Int rhs) {
        return new Int(new Token(NUMBER), this.value() + rhs.value());
    }
    public BigInt add(BigInt rhs) {
        return new BigInt(new Token(NUMBER), BigInteger.valueOf(this.value()).add(rhs.value()));
    }
    public Float add(Float rhs) {
        return new Float(new Token(NUMBER), this.value() + rhs.value());
    }

    /**
     * Subtraction
     */
    public Int subtract(Int rhs) {
        return new Int(new Token(NUMBER), this.value() - rhs.value());
    }
    public BigInt subtract(BigInt rhs) {
        return new BigInt(new Token(NUMBER), BigInteger.valueOf(this.value()).subtract(rhs.value()));
    }
    public Float subtract(Float rhs) {
        return new Float(new Token(NUMBER), this.value() - rhs.value());
    }

    /**
     * Multiplication
     */
    public Int multiply(Int rhs) {
        return new Int(new Token(NUMBER), this.value() * rhs.value());
    }
    public BigInt multiply(BigInt rhs) {
        return new BigInt(new Token(NUMBER), BigInteger.valueOf(this.value()).multiply(rhs.value()));
    }
    public Float multiply(Float rhs) {
        return new Float(new Token(NUMBER), this.value() * rhs.value());
    }

    /**
     * Division
     */
    public Int divide(Int rhs) {
        return new Int(new Token(NUMBER), this.value()/rhs.value());
    }
    public BigInt divide(BigInt rhs) {
        return new BigInt(new Token(NUMBER), BigInteger.valueOf(this.value()).divide(rhs.value()));
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

    @Override public String type() {
        return "Int";
    }
}