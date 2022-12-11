package com.github.pqnelson.expr;

public class Literal extends Expr {
    public static final Literal NIL = new Literal();
    public static final Literal F = new Literal(false);
    public static final Literal T = new Literal(true);
    public static final Literal ZERO = new Int(0L);
    public static final Literal ONE = new Int(1L);
    private final Object value;

    private Literal() {
        this.value = null;
    }
    protected Literal(final Object val) {
        this.value = val;
    }

    public static final Literal Char(char c) {
        return new Char(c);
    }

    public boolean isNil() {
        return (NIL == this);
    }

    public boolean isTrue() {
        return (T == this);
    }

    public static boolean exprIsTrue(final Expr e) {
        return e.isLiteral() && ((Literal) e).isTrue();
    }

    public boolean isFalse() {
        return F == this;
    }

    public static boolean exprIsFalse(final Expr e) {
        return e.isLiteral() && ((Literal) e).isFalse();
    }

    public boolean isFalsy() {
        return isFalse() || isNil();
    }

    public static boolean isFalsy(final Expr e) {
        return e.isLiteral() && ((Literal) e).isFalsy();
    }

    public Object value() {
        return this.value;
    }

    @Override
    public Literal clone() {
        return this;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitLiteral(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj) return false;
        if (obj.getClass() != this.getClass()) return false;
        Literal rhs = (Literal)obj;
        if (null == this.value()) return null == rhs.value();
        return (this.value().equals(rhs.value()));
    }
    
    @Override
    public int hashCode() {
        if (null == this.value()) return 0;
        return this.value().hashCode();
    }

    @Override
    public String toString() {
        if (this.isNil()) return "nil";
        if (this.isTrue()) return "#Literal[true]";
        if (this.isFalse()) return "#Literal[false]";
        return this.value().toString();
    }

    @Override
    public String type() {
        if (this.isNil()) return "nil";
        if (this.isTrue() || this.isFalse()) return "bool";
        return "Literal";
    }
}
