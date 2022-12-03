package com.github.pqnelson.expr;

import java.math.BigInteger;

import com.github.pqnelson.Token;
import static com.github.pqnelson.TokenType.NUMBER;

public class BigInt extends com.github.pqnelson.expr.Number {
    private final BigInteger value;

    public BigInt(final Token token, final BigInteger value) {
        super(token);
        this.value = value;
    }

    @Override
    public BigInteger value() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

    /**
     * Addition with possible overflow.
     */
    public BigInt add(final Int rhs) {
        return rhs.add(this);
    }

    public BigInt add(final BigInt rhs) {
        return new BigInt(new Token(NUMBER),
                          this.value().add(rhs.value()));
    }

    public Float add(final Float rhs) {
        return new Float(new Token(NUMBER),
                         this.value().doubleValue() + rhs.value());
    }

    /**
     * Subtraction
     */
    public BigInt subtract(final Int rhs) {
        return new BigInt(new Token(NUMBER),
                          this.value().subtract(BigInteger.valueOf(rhs.value())));
    }

    public BigInt subtract(final BigInt rhs) {
        return new BigInt(new Token(NUMBER),
                          this.value().subtract(rhs.value()));
    }

    public Float subtract(final Float rhs) {
        return new Float(new Token(NUMBER),
                         this.value().doubleValue() - rhs.value());
    }
    /**
     * Multiplication
     */
    public BigInt multiply(final Int rhs) {
        return rhs.multiply(this);
    }

    public BigInt multiply(final BigInt rhs) {
        return new BigInt(new Token(NUMBER),
                          this.value().multiply(rhs.value()));
    }

    public Float multiply(final Float rhs) {
        return new Float(new Token(NUMBER),
                         this.value().doubleValue() * rhs.value());
    }
    /**
     * Division
     */
    public BigInt divide(final Int rhs) {
        return new BigInt(new Token(NUMBER),
                          this.value().divide(BigInteger.valueOf(rhs.value())));
    }

    public BigInt divide(final BigInt rhs) {
        return new BigInt(new Token(NUMBER),
                          this.value().divide(rhs.value()));
    }

    public Float divide(final Float rhs) {
        return new Float(new Token(NUMBER),
                         this.value().doubleValue() / rhs.value());
    }

    @Override
    public Number add(final Number rhs) {
        if (rhs.isFloat()) {
            return this.add((Float) rhs);
        } else if (rhs.isInt()) {
            return this.add((Int) rhs);
        }
        return this.add((BigInt) rhs);
    }

    @Override
    public Number subtract(final Number rhs) {
        if (rhs.isFloat()) {
            return this.subtract((Float) rhs);
        } else if (rhs.isInt()) {
            return this.subtract((Int) rhs);
        }
        return this.subtract((BigInt) rhs);
    }

    @Override
    public Number divide(final Number rhs) {
        if (rhs.isFloat()) {
            return this.divide((Float) rhs);
        } else if (rhs.isInt()) {
            return this.divide((Int) rhs);
        }
        return this.divide((BigInt)rhs);
    }

    @Override
    public Number multiply(final Number rhs) {
        if (rhs.isFloat()) {
            return this.multiply((Float) rhs);
        } else if (rhs.isInt()) {
            return this.multiply((Int) rhs);
        }
        return this.multiply((BigInt) rhs);
    }

    @Override
    public String type() {
        return "BigInt";
    }

    @Override
    public int compareTo(final Number o) {
        final Number diff = this.subtract(o);
        if (diff.isFloat()) {
            return Double.valueOf(Math.signum(((Float) diff).value()))
                .intValue();
        } else if (diff.isBigInt()) {
            return ((BigInt) diff).value().intValue();
        }
        return ((Int) diff).value().intValue();
    }
}