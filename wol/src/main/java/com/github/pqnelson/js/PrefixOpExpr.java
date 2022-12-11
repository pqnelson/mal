package com.github.pqnelson.js;

/**
 * The base class for Javascript expressions.
 *
 * <p>We use the following restricted grammar:</p>
 * <dl>
 * <dt><i>PrefixOperator</i></dt>
 * <dd>{@code '+'}</dd>
 * <dd>{@code '-'}</dd>
 * <dd>{@code '!'}</dd>
 * <dd>{@code 'typeof'}</dd>
 * </dl>
 */
public class PrefixOpExpr extends Expr {
    public enum PrefixOp {
        PLUS("+", 0),
        NEGATE("-", 1),
        NOT("!", 2),
        TYPEOF("typeof ", 3);
        private final String stringValue;
        private final Integer value;
        private PrefixOp(String stringValue, int precedence) {
            this.stringValue = stringValue;
            this.value = precedence;
        }
        @Override
        public String toString() {
            return this.stringValue;
        }
    };
    private PrefixOp op;
    private Expr expr;

    public PrefixOpExpr(PrefixOp operator, Expr operand) {
        this.op = operator;
        this.expr = operand;
    }

    @Override
    public <T> T accept(final Visitor<T> visitor) {
        return visitor.visitPrefixOpExpr(this);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(this.op.toString());
        buf.append(this.expr.toString());
        return buf.toString();
    }
}