package com.github.pqnelson.expr;

import java.math.BigInteger;

import com.github.pqnelson.Token;
import static com.github.pqnelson.TokenType.NUMBER;

public class BigInt extends Literal {
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
}