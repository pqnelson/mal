package com.github.pqnelson.expr;


import com.github.pqnelson.Token;
import com.github.pqnelson.TokenType;
import static com.github.pqnelson.TokenType.TRUE;
import static com.github.pqnelson.TokenType.FALSE;
import static com.github.pqnelson.TokenType.CHAR;
import static com.github.pqnelson.TokenType.STRING;
import static com.github.pqnelson.TokenType.NUMBER;

public class Literal extends Expr {
    public final Token token;
    public static final Literal NIL
        = new Literal(new Token(TokenType.NIL, "nil", null));
    public static final Literal F
        = new Literal(new Token(TokenType.FALSE, "false", false));
    public static final Literal T
        = new Literal(new Token(TokenType.TRUE, "true", true));
    public static final Literal ZERO = new Int(0L);
    public static final Literal ONE = new Int(1L);

    public Literal(final Token token) {
        this.token = token;
    }

    public static final Literal Char(char c) {
        return new Literal(new Token(TokenType.CHAR, Character.toString(c), c));
    }

    public boolean isNil() {
        return TokenType.NIL == token.type;
    }

    public boolean isTrue() {
        return TRUE == token.type;
    }

    public static boolean exprIsTrue(final Expr e) {
        return e.isLiteral() && ((Literal) e).isTrue();
    }

    public boolean isFalse() {
        return FALSE == token.type;
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
        switch (this.token.type) {
        case TRUE: return Boolean.TRUE;
        case FALSE: return Boolean.FALSE;
        default: return null;
        }
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
        return (this.token.type == rhs.token.type) &&
            (null == this.value() ? null == rhs.value() : (this.value().equals(rhs.value())));
    }
    @Override
    public  int hashCode() {
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