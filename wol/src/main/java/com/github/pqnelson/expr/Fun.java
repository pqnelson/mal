package com.github.pqnelson.expr;

import java.util.function.Function;

import com.github.pqnelson.Env;
import com.github.pqnelson.Printer;
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
    final Expr body;
    final Symbol name;
    private boolean macro;
    private Map meta = null;
    @VisibleForTesting
    IFn f;

    public Fun(IFn f) {
        this(f, null, null, null);
    }

    public Fun(IFn f, Vector params) {
        this(f, params, null, null);
    }

    public Fun(IFn f, Vector params, Expr body) {
        this(f, params, body, null);
    }

    public Fun(IFn f, Vector params, Expr body, Symbol funName) {
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

    public void setIFn(IFn f) {
        if (null == this.f)
            this.f = f;
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

    public Expr invoke(Seq args) throws Throwable {
        return this.f.invoke(args);
    }

    public Expr getBody() {
        return this.body;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitFun(this);
    }
    public <T> T visitParams(Visitor<T> visitor) {
        return this.params.accept(visitor);
    }
    public <T> T visitBody(Visitor<T> visitor) {
        return this.body.accept(visitor);
    }

    public String toObfuscatedString() {
        if (null == name) {
            return "#function<"+(this.hashCode())+">";
        } else {
            return "#function<"+(this.name.name())+">";
        }
    }

    @Override
    public String toString() {
        if (null == body) return this.toObfuscatedString();
        return Printer.print(this);
    }

    public String name() {
        if (null == this.name) return "";
        return this.name.name();
    }

    @Override public String type() {
        return "Fun";
    }
}
