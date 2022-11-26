package com.github.pqnelson;

/**
 * A String printer for expressions.
 */
class Printer implements Expr.Visitor<String> {
    @Override
    public String visitPair(Expr.Pair expr) {
        StringBuffer buf = new StringBuffer();
        buf.append(expr.specialForm.lexeme);
        buf.append(expr.accept(this));
        return buf.toString();
    }
    /*
    @Override
    public String visitDef(Expr.Def expr) {
        StringBuffer buf = new StringBuffer();
        buf.append("(def ");
        buf.append(expr.name.accept(this));
        if (null != expr.value) {
            buf.append(" ");
            buf.append(expr.value.accept(this));
        }
        buf.append(")");
        return buf.toString();
    }

    @Override
    public String visitIf(Expr.If expr) {
        StringBuffer buf = new StringBuffer();
        buf.append("(if ");
        buf.append(expr.test.accept(this));
        buf.append(" ");
        buf.append(expr.trueBranch.accept(this));
        if (null != expr.falseBranch) {
            buf.append(" ");
            buf.append(expr.falseBranch.accept(this));
        }
        buf.append(")");
        return "";
    }

    @Override
    public String visitLet(Expr.Let expr) {
        StringBuffer buf = new StringBuffer();
        buf.append("(let* ");
        buf.append(expr.bindings.accept(this));
        buf.append(" ");
        String body = expr.body.accept(this);
        if (body.length() > 0) {
            buf.append(body.substring(1));
        } else {
            buf.append(")");
        }
        return buf.toString();
    }

*/
    @Override
    public String visitFun(Expr.Fun expr) {
        StringBuffer buf = new StringBuffer();
        if (null != expr.body) {
            buf.append("(fn* ");
            if (null != expr.name) {
                buf.append(expr.name.accept(this));
                buf.append(" ");
            }
            buf.append(expr.params.accept(this));
            buf.append(" ");
            String body = expr.body.accept(this);
            if (body.length() > 0) {
                buf.append(body.substring(1));
            } else {
                buf.append(")");
            }
        } else if (null != expr.name) {
            buf.append(expr.name.accept(this));
        } else {
            buf.append("#<function");
            buf.append(expr.hashCode());
            buf.append(">");
        }
        return buf.toString();
    }

    @Override
    public String visitVector(Expr.Vector expr) {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for (Expr e : expr.contents) {
            buf.append(e.accept(this));
            buf.append(" ");
        }
        buf.deleteCharAt(buf.length() - 1);
        buf.append("]");
        return buf.toString();
    }

    @Override
    public String visitSeq(Expr.Seq expr) {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        for (Expr e : expr.contents) {
            buf.append(e.accept(this));
            buf.append(" ");
        }
        buf.deleteCharAt(buf.length() - 1);
        buf.append(")");
        return buf.toString();
    }

    @Override
    public String visitSymbol(Expr.Symbol expr) {
        StringBuffer buf = new StringBuffer(expr.identifier.lexeme);
        return buf.toString();
    }

    @Override
    public String visitKeyword(Expr.Keyword expr) {
        StringBuffer buf = new StringBuffer(":");
        buf.append(expr.identifier.lexeme);
        return buf.toString();
    }

    @Override
    public String visitLiteral(Expr.Literal expr) {
        StringBuffer buf = new StringBuffer(expr.token.lexeme);
        return buf.toString();
    }

    public String print(Expr e) {
        return e.accept(this);
    }
}