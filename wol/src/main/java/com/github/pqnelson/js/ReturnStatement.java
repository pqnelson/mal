package com.github.pqnelson.js;

public class ReturnStatement extends Statement {
    private final JsExpr result;
    private final boolean inferred;

    public ReturnStatement() {
        this.result = Undefined.instance;
        this.inferred = true;
    }

    public ReturnStatement(JsExpr expr) {
        this.result = expr;
        this.inferred = false;
    }

    @Override
    public <T> T accept(final Visitor<T> visitor) {
        return visitor.visitReturn(this);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("return");
        if (!this.inferred) {
            buf.append(" ");
            buf.append(this.result.toString());
        }
        buf.append(";");
        return buf.toString();
    }
}
