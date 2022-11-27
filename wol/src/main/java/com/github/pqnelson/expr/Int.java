package com.github.pqnelson.expr;

import java.math.BigInteger;

import com.github.pqnelson.Token;
import static com.github.pqnelson.TokenType.NUMBER;

public class Int extends Literal {
    private final long value;
    public Int(Token token, long value) {
        super(token);
        this.value = value;
    }

    @Override
    public Long value() { return Long.valueOf(this.value); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (null == obj) return false;
        if (obj.getClass() != this.getClass()) return false;
        Int rhs = (Int)obj;
        return (this.value() == rhs.value());
    }

    @Override
    public final int hashCode() {
        return Long.hashCode(this.value());
    }


    @Override
    public String toString() {
        return String.valueOf(this.value);
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
    public Int add(Int rhs) {
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
}