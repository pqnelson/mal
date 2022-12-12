package com.github.pqnelson.js;

public class IfStatement extends Statement {
    private Statement trueBranch, falseBranch;
    private JsExpr test;

    public IfStatement(JsExpr test, Statement trueBranch) {
        this(test, trueBranch, null);
    }

    public IfStatement(JsExpr test,
                       Statement trueBranch,
                       Statement falseBranch) {
        this.test = test;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    private void chopSemicolon(StringBuffer buf) {
        if (';' == buf.charAt(buf.length() - 1)) {
            buf.deleteCharAt(buf.length() - 1);
        }
    }

    @Override
    public <T> T accept(final Visitor<T> visitor) {
        return visitor.visitIf(this);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("if (");
        buf.append(test.toString());
        buf.append(") ");
        buf.append(trueBranch.toString());
        if (null != falseBranch) {
            chopSemicolon(buf);
            buf.append(" else ");
            buf.append(falseBranch.toString());
        }
        return buf.toString();
    }
}
