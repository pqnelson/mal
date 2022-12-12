package com.github.pqnelson.js;

public class ConditionalExpr extends JsExpr {
    private JsExpr test, trueBranch, falseBranch;

    public ConditionalExpr(JsExpr test, JsExpr trueBranch, JsExpr falseBranch) {
        this.test = test;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }
    @Override
    public <T> T accept(final ExprVisitor<T> visitor) {
        return visitor.visitConditionalExpr(this);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("((");
        buf.append(test.toString());
        buf.append(") ? (");
        buf.append(trueBranch.toString());
        buf.append(") : (");
        buf.append(falseBranch.toString());
        buf.append("))");
        return buf.toString();
    }
}
