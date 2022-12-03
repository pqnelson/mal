package com.github.pqnelson.expr;

import com.github.pqnelson.Token;
import static com.github.pqnelson.TokenType.NUMBER;

public class Float extends com.github.pqnelson.expr.Number {
    private final double value;

    public Float(final double value) {
        this(new Token(NUMBER, Double.toString(value), value), value);
    }

    public Float(final Token token, final double value) {
        super(token);
        this.value = value;
    }

    @Override
    public Double value() {
        return Double.valueOf(this.value);
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
    /**
     * Addition with possible overflow.
     */
    public Float add(final Int rhs) {
        return rhs.add(this);
    }
    public Float add(final Float rhs) {
        return new Float(new Token(NUMBER), this.value() + rhs.value());
    }
    public Float add(final BigInt rhs) {
        return new Float(new Token(NUMBER),
                         this.value() + rhs.value().doubleValue());
    }
    /**
     * Subtraction
     */
    public Float subtract(final Int rhs) {
        return new Float(new Token(NUMBER), this.value() - rhs.value());
    }
    public Float subtract(final BigInt rhs) {
        return new Float(new Token(NUMBER),
                         this.value() - rhs.value().doubleValue());
    }
    public Float subtract(final Float rhs) {
        return new Float(new Token(NUMBER), this.value() - rhs.value());
    }
    /**
     * Multiplication
     */
    public Float multiply(final Int rhs) {
        return rhs.multiply(this);
    }
    public Float multiply(final BigInt rhs) {
        return new Float(new Token(NUMBER),
                         this.value() * rhs.value().doubleValue());
    }
    public Float multiply(final Float rhs) {
        return new Float(new Token(NUMBER), this.value() * rhs.value());
    }
    /**
     * Division
     */
    public Float divide(final Int rhs) {
        return new Float(new Token(NUMBER),
                         this.value() / rhs.value());
    }
    public Float divide(final BigInt rhs) {
        return new Float(new Token(NUMBER),
                         this.value() / (rhs.value().doubleValue()));
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
        final Float rhs = (Float) obj;
        return Double.valueOf(this.value).equals(Double.valueOf(rhs.value()));
    }
    @Override
    public int hashCode() {
        return Double.hashCode(this.value());
    }

    @Override public String type() {
        return "Float";
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
