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
 * Print an {@code Expr} instance to an escaped string, suitable for printing
 * to the screen for use in a file. Basically, this will be the same output
 * as from a {@code new Printer(true)}, but with quotation marks around
 * strings.
 */
class PPrinter extends Printer implements Visitor<String> {

    public PPrinter() {
        super(true);
        assert (this.isReadable);
    }

    @Override
    public String visitLiteral(Literal expr) {
        StringBuffer buf = new StringBuffer();
        if (expr.isString()) {
            buf.append("\"");
            buf.append(super.visitLiteral(expr));
            buf.append("\"");
        } else {
            buf.append(super.visitLiteral(expr));
        }
        return buf.toString();
    }

    public static String print(Expr e) {
        return e.accept(new PPrinter());
    }

}