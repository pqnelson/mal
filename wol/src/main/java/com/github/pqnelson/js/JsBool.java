package com.github.pqnelson.js;

class JsBool extends JsExpr implements PrimitiveValue {
    private final boolean value;
    public static final JsBool TRUE = new JsBool(true);
    public static final JsBool FALSE = new JsBool(false);

    public JsBool(boolean val) {
        this.value = val;
    }

    @Override
    public <T> T accept(final ExprVisitor<T> visitor) {
        return visitor.visitBool(this);
    }

    @Override
    public boolean isFalsy() {
        return (false == this.value);
    }
}
