package com.github.pqnelson.expr;

import java.math.BigInteger;

import com.github.pqnelson.Token;
import static com.github.pqnelson.TokenType.NUMBER;

public class Int extends com.github.pqnelson.expr.Number {
    private final long value;

    public Int(final long value) {
        this(new Token(NUMBER, Long.toString(value), value), value);
    }

    public Int(final Token token, final long value) {
        super(token);
        this.value = value;
    }

    @Override
    public Long value() {
        return this.value;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
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

    public String toString(final int radix) {
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
    public Number add(final Int rhs) {
        return new Int(new Token(NUMBER),
                       this.value() + rhs.value());
    }
    public BigInt add(final BigInt rhs) {
        return new BigInt(new Token(NUMBER),
                          BigInteger.valueOf(this.value()).add(rhs.value()));
    }
    public Float add(final Float rhs) {
        return new Float(new Token(NUMBER),
                         this.value() + rhs.value());
    }

    /**
     * Subtraction
     */
    public Int subtract(final Int rhs) {
        return new Int(new Token(NUMBER),
                       this.value() - rhs.value());
    }
    public BigInt subtract(final BigInt rhs) {
        return new BigInt(new Token(NUMBER),
                          BigInteger.valueOf(this.value())
                                    .subtract(rhs.value()));
    }
    public Float subtract(final Float rhs) {
        return new Float(new Token(NUMBER),
                         this.value() - rhs.value());
    }

    /**
     * Multiplication
     */
    public Int multiply(final Int rhs) {
        return new Int(new Token(NUMBER),
                       this.value() * rhs.value());
    }

    public BigInt multiply(final BigInt rhs) {
        return new BigInt(new Token(NUMBER),
                          BigInteger.valueOf(this.value())
                                    .multiply(rhs.value()));
    }

    public Float multiply(final Float rhs) {
        return new Float(new Token(NUMBER),
                         this.value() * rhs.value());
    }

    /**
     * Division
     */
    public Int divide(final Int rhs) {
        return new Int(new Token(NUMBER),
                       this.value() / rhs.value());
    }

    public BigInt divide(final BigInt rhs) {
        return new BigInt(new Token(NUMBER),
                          BigInteger.valueOf(this.value()).divide(rhs.value()));
    }

    public Float divide(final Float rhs) {
        return new Float(new Token(NUMBER),
                         this.value() / rhs.value());
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
        return this.divide((BigInt) rhs);
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
        return "Int";
    }

    @Override
    public int compareTo(final Number o) {
        Number diff = this.subtract(o);
        if (diff.isFloat()) {
            return Double.valueOf(Math.signum(((Float) diff).value()))
                .intValue();
        } else if (diff.isBigInt()) {
            return ((BigInt) diff).value().intValue();
        }
        return ((Int) diff).value().intValue();
    }
}