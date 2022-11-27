package com.github.pqnelson.expr;

import java.util.function.Function;

import com.github.pqnelson.Env;
import com.github.pqnelson.Token;

import com.github.pqnelson.annotations.VisibleForTesting;

/**
 * Function abstract class.
 *
 * We create new {@code Fun} instances each time we encounter a
 * {@code fn*} literal.
 */
public class Fun extends Expr implements IObj {
    final Vector params;
    final Seq body;
    final Symbol name;
    private boolean macro;
    private Map meta = null;
    @VisibleForTesting
    final Function<Seq, Expr> f;

    public Fun(Function<Seq, Expr> f) {
        this(f, null, null, null);
    }

    public Fun(Function<Seq, Expr> f, Vector params) {
        this(f, params, null, null);
    }

    public Fun(Function<Seq, Expr> f, Vector params, Seq body) {
        this(f, params, body, null);
    }

    public Fun(Function<Seq, Expr> f, Vector params, Seq body, Symbol funName) {
        this.f = f;
        this.params = params;
        this.body = body;
        this.name = funName;
        this.macro = false;
    }

    /**
     * Copy constructor.
     */
    public Fun(Fun fn) {
        this(fn.f, fn.params, fn.body, fn.name);
        this.macro = fn.macro;
    }

    /**
     * Copy constructor with specified metadata map.
     */
    public Fun(Fun fn, Map meta) {
        this(fn);
        this.meta = meta;
    }

    @Override
    public Map meta() { return this.meta; }

    private boolean hasSameSignature(Fun rhs) {
        return ((null == this.params && null == rhs.params)
                || this.params.equals(rhs.params));
    }

    private boolean hasSameName(Fun rhs) {
        return ((null == this.name && null == rhs.name)
                  || this.name.equals(rhs.name));
    }

    @VisibleForTesting
    boolean hasSameImplementation(Fun rhs) {
        return this.f.equals(rhs.f);
    }

    private boolean hasSameBody(Fun rhs) {
        if (this.body == rhs.body) return true;
        if (null == this.body || null == rhs.body) return hasSameImplementation(rhs);
        return this.body.equals(rhs.body);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (null == obj) return false;
        if (!Fun.class.isInstance(obj)) return false;
        Fun rhs = (Fun)obj;
        return hasSameSignature(rhs) && hasSameName(rhs) &&
            hasSameBody(rhs) && hasSameImplementation(rhs);
    }

    @Override
    public Fun withMeta(Map newMeta) {
            if (this.meta.equals(newMeta)) return this;
            return new Fun(this, newMeta);
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

    public boolean isNative() {
        return (null == this.body) && (null != this.f);
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

    public Expr invoke(Seq args) {
        return this.f.apply(args);
    }

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
