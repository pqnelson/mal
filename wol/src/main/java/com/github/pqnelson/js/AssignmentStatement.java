package com.github.pqnelson.js;

public class AssignmentStatement extends Statement {
    private RefinementExpr lval;
    private JsExpr rhs;

    public AssignmentStatement(Name variable, JsExpr rhs) {
        this(new RefinementExpr(variable), rhs);
    }
    
    public AssignmentStatement(RefinementExpr variable, JsExpr rhs) {
        this.lval = variable;
        this.rhs = rhs;
    }

    @Override
    public <T> T accept(final Visitor<T> visitor) {
        return visitor.visitAssignment(this);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(lval.toString());
        buf.append(" = ");
        buf.append(rhs.toString());
        buf.append(";");
        return buf.toString();
    }
}
