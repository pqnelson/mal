package com.github.pqnelson.js;

public class WhileStatement extends Statement {
    private JsExpr test;
    private Statement body;

    private WhileStatement() { }

    public WhileStatement(JsExpr condition, Statement statement) {
        this.test = condition;
        this.body = statement;
    }

    @Override
    public <T> T accept(final Visitor<T> visitor) {
        return visitor.visitWhile(this);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("while (");
        buf.append(this.test.toString());
        buf.append(") ");
        buf.append(this.body.toString());
        return buf.toString();
    }
}
