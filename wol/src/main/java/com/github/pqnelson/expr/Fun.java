package com.github.pqnelson.expr;

import com.github.pqnelson.Env;
import com.github.pqnelson.Token;

/**
 * Function abstract class.
 *
 * We create new {@code Fun} instances each time we encounter a
 * {@code fn*} literal.
 */
public abstract class Fun extends Expr {
    final Vector params;
    final Seq body;
    final Symbol name;
    private boolean macro;

    public Fun() {
        this.params = null;
        this.body = null;
        this.name = null;
        this.macro = false;
    }

    public Fun(Symbol funName) {
        this.params = null;
        this.body = null;
        this.name = funName;
        this.macro = false;
    }

    public Fun(Vector params, Seq body) {
        this.params = params;
        this.body = body;
        this.name = null;
        this.macro = false;
    }

    public Fun(Vector params, Seq body, Symbol funName) {
        this.params = params;
        this.body = body;
        this.name = funName;
        this.macro = false;
    }

    /**
     * Mark the function as a macro. This is an "not undoable" operation.
     */
    public void setMacro() {
        this.macro = true;
    }

    public boolean isMacro() {
        return this.macro;
    }

    public boolean isInterpreted() {
        return (null != this.body);
    }

    /**
     * Generate the environment for a new function call, binding the formal
     * parameters to the given {@code args}.
     */
    public Env genEnv(Env env, Seq args) {
        return new Env(env, params, args);
    }

    public abstract Expr invoke(Seq args);

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitFun(this);
    }

    @Override
    public String toString() {
        if (null == name) {
            return "#function<"+(this.hashCode())+">";
        } else {
            return "#function<"+(this.name.name())+">";
        }
    }
}
