package com.github.pqnelson.js;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * A function call for either a function identifier or lambda expression.
 * <p>For a lambda expression, it produces a result of the form
 * {@code '(((param1, ..., paramN) => body)(arg1, ..., argN))'}.</p>
 * <p>For a function identifier, it produces a result of the form
 * {@code 'function_name(arg1, ..., argN)'}.</p>
 */
public class FunctionCallExpr extends JsExpr {
    private JsExpr function;
    private List<JsExpr> args;
    public FunctionCallExpr(RefinementExpr functionName, JsExpr... arguments) {
        this.function = functionName;
        this.args = new ArrayList<>(Arrays.asList(arguments));
    }
    public FunctionCallExpr(LambdaExpr functionExpr, JsExpr... arguments) {
        this.function = functionExpr;
        this.args = new ArrayList<>(Arrays.asList(arguments));
    }

    @Override
    public <T> T accept(final ExprVisitor<T> visitor) {
        return visitor.visitFunctionCallExpr(this);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (LambdaExpr.class.isInstance(function)) {
            buf.append("((");
            buf.append(function.toString());
            buf.append(")");
        } else {
            buf.append(function.toString());
        }
        buf.append("(");
        if (!this.args.isEmpty()) {
            for (int i = 0; i < args.size() - 2; i++) {
                buf.append(args.get(i).toString());
                buf.append(", ");
            }
            buf.append(args.get(args.size()-1).toString());
        }
        buf.append(")");
        if (LambdaExpr.class.isInstance(function)) {
            buf.append(")");
        }
        return buf.toString();
    }
}
