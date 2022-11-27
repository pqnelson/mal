package com.github.pqnelson;

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
class Printer implements Visitor<String> {

    @Override
    public String visitFun(Fun expr) {
        return expr.toString();
    }

    @Override
    public String visitVector(Vector expr) {
        return expr.toString();
    }

    @Override
    public String visitSeq(Seq expr) {
        return expr.toString();
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
        return expr.toString();
    }

    @Override
    public String visitMap(Map expr) {
        return expr.toString();
    }

    public String print(Expr e) {
        return e.accept(this);
    }
}