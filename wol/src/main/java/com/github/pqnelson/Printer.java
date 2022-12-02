package com.github.pqnelson;

import org.apache.commons.text.StringEscapeUtils;

import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.BigInt;
import com.github.pqnelson.expr.Float;
import com.github.pqnelson.expr.Fun;
import com.github.pqnelson.expr.Int;
import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.Map;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Str;
import com.github.pqnelson.expr.Symbol;
import com.github.pqnelson.expr.Vector;
import com.github.pqnelson.expr.Visitor;
/**
 * A String printer for expressions.
 */
public class Printer implements Visitor<String> {
    /**
     * "Readable" here means "We will handoff to System.out.println".
     */
    protected boolean isReadable = false;

    public Printer() {
        this(false);
    }

    public Printer(boolean isReadable) {
        this.isReadable = isReadable;
    }

    @Override
    public String visitFun(Fun f) {
        if (f.isInterpreted()) {
            StringBuffer buf = new StringBuffer("(fn* ");
            if(!f.name().equals("")) {
                buf.append(f.name());
                buf.append(" ");
            }
            buf.append(f.visitParams(this));
            buf.append(" ");
            buf.append(f.visitBody(this));
            buf.append(")");
            return buf.toString();
        } else {
            return f.toObfuscatedString();
        }
    }

    @Override
    public String visitVector(Vector vec) {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        if (!vec.isEmpty()) {
            for (Expr e : vec) {
                buf.append(e.accept(this));
                buf.append(" ");
            }
            buf.deleteCharAt(buf.length() - 1);
        }
        buf.append("]");
        return buf.toString();
    }

    @Override
    public String visitSeq(Seq seq) {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        if (!seq.isEmpty()) {
            for (Expr e : seq) {
                buf.append(e.accept(this));
                buf.append(" ");
            }
            buf.deleteCharAt(buf.length() - 1);
        }
        buf.append(")");
        return buf.toString();
    }

    @Override
    public String visitSymbol(Symbol expr) {
        return expr.name();
    }

    @Override
    public String visitKeyword(Keyword expr) {
        return expr.toString();
    }

    @Override
    public String visitLiteral(Literal expr) {
        StringBuffer buf = new StringBuffer();
        if (expr.isString() && this.isReadable) {
            buf.append(StringEscapeUtils.escapeJava(expr.toString()));
        } else if (expr.isTrue()) {
            buf.append("true");
        } else if (expr.isFalse()) {
            buf.append("false");
        } else {
            buf.append(expr.toString());
        }
        return buf.toString();
    }

    @Override
    public String visitMap(Map map) {
        StringBuffer buf = new StringBuffer("{");
        for (Expr k : map.keys()) {
            buf.append(k.accept(this));
            buf.append(" ");
            buf.append(map.get(k).accept(this));
            buf.append(" ");
        }
        if (!map.isEmpty()) buf.deleteCharAt(buf.length() - 1);
        buf.append("}");
        return buf.toString();
    }

    public static String print(Expr e) {
        return e.accept(new Printer());
    }
}