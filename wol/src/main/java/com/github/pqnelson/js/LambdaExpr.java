package com.github.pqnelson.js;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * An anonymous "arrow" function.
 *
 * <p>This encodes <em>both</em> arrow functions with an expression body
 * {@code (params) => expr} and arrow functions with a statement body, of
 * the form:</p>
 * <blockquote>
 * {@code (param1, ..., paramN) =>} { <br />
 * &nbsp; &nbsp; &nbsp; &nbsp; {@code statements} <br />
 * }
 * </blockquote>
 */
public class LambdaExpr extends Expr {
    private final ArrayList<Name> params;
    private final Expr ebody;
    private final BlockStatement sbody;

    public LambdaExpr(Name params[], Expr body) {
        this.params = new ArrayList<>(Arrays.asList(params));
        this.ebody = body;
        this.sbody = null;
    }
    /**
     * Syntactic sugar for an arrow function with a block statement
     * given by the sequence of statements.
     */
    public LambdaExpr(Name params[], Statement... body) {
        this(params, new BlockStatement(body));
    }
    public LambdaExpr(Name params[], BlockStatement body) {
        this.params = new ArrayList<>(Arrays.asList(params));
        this.sbody = body;
        this.ebody = null;
    }

    @Override
    public <T> T accept(final Visitor<T> visitor) {
        return visitor.visitLambdaExpr(this);
    }

    public boolean isBodyBlockStatement() {
        return (null != this.sbody);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("(");
        if (!this.params.isEmpty()) {
            for (int i = 0; i < this.params.size() - 2; i++) {
                buf.append(this.params.get(i).toString());
                buf.append(", ");
            }
            buf.append(this.params.get(this.params.size() - 1).toString());
        }
        buf.append(") => ");
        if (null == this.ebody) {
            buf.append(this.sbody.toString());
        } else {
            buf.append(this.ebody.toString());
        }
        return buf.toString();
    }
}