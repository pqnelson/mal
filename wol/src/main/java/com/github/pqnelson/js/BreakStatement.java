package com.github.pqnelson.js;

public class BreakStatement extends Statement {
    private final Name location;

    public BreakStatement() {
        this.location = null;
    }

    public BreakStatement(Name name) {
        this.location = name;
    }

    @Override
    public <T> T accept(final Visitor<T> visitor) {
        return visitor.visitBreak(this);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("break");
        if (null != this.location) {
            buf.append(" ");
            buf.append(this.location.toString());
        }
        buf.append(";");
        return buf.toString();
    }
}