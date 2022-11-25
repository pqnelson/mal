package com.github.pqnelson;

import org.apache.commons.text.StringEscapeUtils;

/**
 * Print an {@code Expr} instance to an escaped string, suitable for printing
 * to the screen for use in a file.
 */
class PPrint extends Printer implements Expr.Visitor<String> {
    @Override
    public String visitLiteral(Expr.Literal expr) {
        StringBuffer buf = new StringBuffer();
        if (TokenType.STRING == expr.token.type) {
            buf.append("\"");
            buf.append(StringEscapeUtils.escapeEcmaScript(expr.token.lexeme));
            buf.append("\"");
        } else {
            buf.append(expr.token.lexeme);
        }
        return buf.toString();
    }
}