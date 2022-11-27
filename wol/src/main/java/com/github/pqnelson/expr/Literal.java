package com.github.pqnelson.expr;


import com.github.pqnelson.Token;
import com.github.pqnelson.TokenType;
import static com.github.pqnelson.TokenType.TRUE;
import static com.github.pqnelson.TokenType.FALSE;
import static com.github.pqnelson.TokenType.STRING;
import static com.github.pqnelson.TokenType.NUMBER;

public class Literal extends Expr {
    public final Token token;
    public static final Literal NIL = new Literal(new Token(TokenType.NIL));
    public static final Literal F = new Literal(new Token(TokenType.FALSE, "false", false));
    public static final Literal T = new Literal(new Token(TokenType.TRUE, "true", true));
    public static final Literal ZERO = new Int(0L);
    public static final Literal ONE = new Int(1L);

    public Literal(Token token) {
        this.token = token;
    }

    public boolean isNil() { return TokenType.NIL == token.type; }

    public boolean isTrue() { return TRUE == token.type; }

    public boolean isFalse() { return FALSE == token.type; }

    public boolean isFalsy() { return isFalse() || isNil(); }

    public boolean isString() { return STRING == token.type; }

    public boolean isNumber() { return NUMBER == token.type; }

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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (null == obj) return false;
        if (obj.getClass() != this.getClass()) return false;
        Literal rhs = (Literal)obj;
        return (this.token.type == rhs.token.type) && (this.value().equals(rhs.value()));
    }
    @Override
    public  int hashCode() {
        return this.value().hashCode();
    }
}