package com.github.pqnelson;

import org.apache.commons.text.StringEscapeUtils;

import com.github.pqnelson.expr.Visitor;
import com.github.pqnelson.expr.Literal;

/**
 * Print an {@code Expr} instance to an escaped string, suitable for printing
 * to the screen for use in a file.
 */
class PPrint extends Printer implements Visitor<String> {
    @Override
    public String visitLiteral(Literal expr) {
        StringBuffer buf = new StringBuffer();
        if (expr.isString()) {
            buf.append("\"");
            buf.append(StringEscapeUtils.escapeEcmaScript(expr.value().toString()));
            buf.append("\"");
        } else {
            buf.append(expr.value().toString());
        }
        return buf.toString();
    }
}