package com.github.pqnelson.js;

public class ConditionalExpr extends Expr {
    private Expr test, trueBranch, falseBranch;

    public ConditionalExpr(Expr test, Expr trueBranch, Expr falseBranch) {
        this.test = test;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }
    @Override
    public <T> T accept(final Visitor<T> visitor) {
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