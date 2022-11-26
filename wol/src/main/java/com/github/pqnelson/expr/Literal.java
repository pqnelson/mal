package com.github.pqnelson.expr;


import com.github.pqnelson.Token;
import static com.github.pqnelson.TokenType.NIL;
import static com.github.pqnelson.TokenType.TRUE;
import static com.github.pqnelson.TokenType.FALSE;
import static com.github.pqnelson.TokenType.STRING;
import static com.github.pqnelson.TokenType.NUMBER;

public class Literal extends Expr {
    public final Token token;

    public Literal(Token token) {
        this.token = token;
    }

    public boolean isNil() { return NIL == token.type; }

    public boolean isTrue() { return TRUE == token.type; }

    public boolean isFalse() { return FALSE == token.type; }

    public boolean isFalsy() { return isFalse() || isNil(); }

    public boolean isString() { return STRING == token.type; }

    public boolean isNumber() { return NUMBER == token.type; }

    public Object value() {
        switch (this.token.type) {
        case NIL: return null;
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
}