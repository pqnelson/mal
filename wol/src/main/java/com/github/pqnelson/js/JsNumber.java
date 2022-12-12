package com.github.pqnelson.js;

class JsNumber extends JsExpr implements PrimitiveValue {
    private final double value;
    public static final JsNumber NaN = new JsNumber(Double.NaN);

    public JsNumber(double number) {
        this.value = number;
    }

    @Override
    public <T> T accept(final ExprVisitor<T> visitor) {
        return visitor.visitNumber(this);
    }

    @Override
    public boolean isFalsy() {
        return Double.isNaN(this.value)
            || (0.0 == this.value) || (-0.0 == this.value);
    }
}
