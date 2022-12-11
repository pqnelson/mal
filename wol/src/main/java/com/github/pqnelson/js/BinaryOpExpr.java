package com.github.pqnelson.js;

public class BinaryOpExpr extends Expr {
    public enum BinaryOp {
        MULTIPLY("*", 0),
        DIVIDE("/", 1),
        REMAINDER("%", 2),
        PLUS("+", 3),
        NEGATE("-", 4),
        GEQ(">=", 5),
        LEQ("<=", 6),
        GT(">", 7),
        LT("<", 8),
        EQUAL("===", 9),
        NOT_EQUAL("!==", 10),
        OR("||", 11),
        AND("&&", 12);
        private final String stringValue;
        private final Integer value;
        private BinaryOp(String stringValue, int precedence) {
            this.stringValue = stringValue;
            this.value = precedence;
        }
        @Override
        public String toString() {
            return this.stringValue;
        }
    };
    private Expr lhs, rhs;
    private BinaryOp op;

    public BinaryOpExpr(BinaryOp operator, Expr lhs, Expr rhs) {
        this.op = operator;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public <T> T accept(final Visitor<T> visitor) {
        return visitor.visitBinaryOpExpr(this);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(this.lhs.toString());
        buf.append(" ");
        buf.append(this.op.toString());
        buf.append(" ");
        buf.append(this.rhs.toString());
        return buf.toString();
    }
}