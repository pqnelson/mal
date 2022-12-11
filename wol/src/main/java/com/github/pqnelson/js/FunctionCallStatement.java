package com.github.pqnelson.js;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class FunctionCallStatement extends Statement {
    private FunctionCallExpr expr;

    public FunctionCallStatement(RefinementExpr functionName, Expr... arguments) {
        this(new FunctionCallExpr(functionName, arguments));
    }

    public FunctionCallStatement(FunctionCallExpr funcall) {
        this.expr = funcall;
    }

    @Override
    public <T> T accept(final Visitor<T> visitor) {
        return visitor.visitFunctionCallStatement(this);
    }

    @Override
    public String toString() {
        return this.expr.toString()+";";
    }
}