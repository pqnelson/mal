package com.github.pqnelson.expr;

import java.util.HashMap;
import java.util.TreeMap;

import java.util.concurrent.Callable;

import com.github.pqnelson.Env;
import com.github.pqnelson.Printer;

import com.github.pqnelson.annotations.VisibleForTesting;

/**
 * Function abstract class.
 *
 * We create new {@code Fun} instances each time we encounter a
 * {@code fn*} literal.
 */
public class Fun extends Expr implements IObj<Fun> {
    public static class FnMethod {
        final Vector params;
        final Expr body;
        private boolean isVariadic;
        /**
         * When {@code this.isVariadic}, the arity is the minimum number
         * of arguments required.
         * <p>When missing, it is set to -1.</p>
         */
        private int arity;
        private IFn f;
        FnMethod(final IFn fn) {
            this(fn, -1, false);
        }
        FnMethod(final IFn fn, int arity) {
            this(fn, arity, false);
        }
        FnMethod(final IFn fn, final int arity, final boolean isVariadic) {
            this.f = fn;
            this.arity = arity;
            this.isVariadic = isVariadic;
            this.params = null;
            this.body = null;
        }
        FnMethod(final Vector params, final Expr body) {
            this(null, params, body);
        }
        FnMethod(final IFn fn, final Vector params, final Expr body) {
            this.f = fn;
            this.body = body;
            this.params = params;
            this.isVariadic = params.contains(new Symbol("&"));
            this.arity = params.size() + (this.isVariadic ? -2 : 0);
        }
        

        String toString(boolean parenthesize) {
            StringBuffer buf = new StringBuffer((parenthesize ? "(" : ""));
            if (null == this.body) {
                buf.append("#function");
                buf.append(this.f.hashCode());
            } else {
                buf.append(this.params.toString());
                buf.append(" ");
                buf.append(this.body.toString());
            }
            buf.append((parenthesize ? ")" : ""));
            return buf.toString();
        }
        
        int arity() {
            return this.arity;
        }
        
        boolean isVariadic() {
            return this.isVariadic;
        }

        boolean isNative() {
            return (null == this.body) && (null != this.f);
        }

        boolean isInterpreted() {
            return (null != this.body);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (null == obj) return false;
            FnMethod rhs = (FnMethod) obj;
            return (rhs.arity == this.arity)
                && (null == this.body || this.body.equals(rhs.body))
                && (rhs.isVariadic == this.isVariadic)
                && (null == this.f || this.f.equals(rhs.f));
        }

        Env genEnv(final Env env, final Seq args) {
            return new Env(env, params, args);
        }

        Expr invoke(final Seq args) throws Throwable {
            return this.f.invoke(args);
        }
    }
    private FnMethod defaultFn;
    private Symbol name;
    private java.util.Map<Integer, FnMethod> methods = new TreeMap<>();
    private boolean macro = false;
    private Map meta = null;
    // If there are 8 or more different overloads, switch to using a HashMap
    private static final int HASHMAP_CUTOFF = 8;

    public Fun(final IFn f) {
        this(f, null, null, null);
    }
    public Fun(final IFn f, final int arity, final boolean isVariadic) {
        if (isVariadic) this.defaultFn = new FnMethod(f, arity, isVariadic);
        else methods.put(arity, new FnMethod(f, arity, isVariadic));
    }
    public Fun(final IFn f, final int arity, final boolean isVariadic,
               final Symbol name) {
        this(f, arity, isVariadic);
        this.name = name;
    }

    
    public Fun(final IFn f, final Vector params) {
        this(f, params, null, null);
    }
    public Fun(final IFn f, final Vector params, final Expr body) {
        this(f, params, body, null);
    }
    public Fun(final IFn f,
               final Vector params,
               final Expr body,
               final Symbol funName) {
        FnMethod fnExpr = new FnMethod(f, params, body);
        if (fnExpr.isVariadic) this.defaultFn = fnExpr;
        else methods.put(fnExpr.arity(), fnExpr);
        this.name = funName;
    }

    public Fun(final Symbol funName) {
        this.name = funName;
    }

    /**
     * Copy constructor.
     */
    public Fun(final Fun fn) {
        this.defaultFn = fn.defaultFn;
        this.name = fn.name;
        this.macro = fn.macro;
        if (fn.methods.size() >= HASHMAP_CUTOFF) {
            this.methods = new HashMap<Integer, FnMethod>(fn.methods.size());
        }
        for (FnMethod f : fn.methods.values()) {
            this.methods.put(f.arity(), f);
        }
    }

    /**
     * Copy constructor with specified metadata map.
     */
    public Fun(final Fun fn, final Map meta) {
        this(fn);
        this.meta = meta.immutableCopy();
    }

    @Override
    public Fun clone() {
        return this;
    }

    @Override
    public Map meta() {
        return this.meta;
    }

    private void checkMethodValidity(final FnMethod fnExpr) {
        if (fnExpr.isVariadic() && null != this.defaultFn)  {
            throw new RuntimeException("Cannot have two variadic signatures for a fn");
        }
        if (null != this.defaultFn && fnExpr.arity() > this.defaultFn.arity()) {
            throw new RuntimeException("Can't have fixed arity function with more params than variadic function");
        }
        if (this.methods.containsKey(fnExpr.arity())) {
            throw new RuntimeException("Cannot have two overloads with the same arity");
        }
    }

    private void checkMapWorks() {
        if (this.methods instanceof HashMap) return;
        if (this.methods.size() >= HASHMAP_CUTOFF) {
            HashMap<Integer, FnMethod> map = new HashMap<>(this.methods);
            this.methods = map;
        }
    }
    
    private void addMethod(final FnMethod fnExpr) {
        checkMethodValidity(fnExpr);
        if (fnExpr.isVariadic()) this.defaultFn = fnExpr;
        else this.methods.put(fnExpr.arity(), fnExpr);
        checkMapWorks();
    }
    
    public void addMethod(final IFn fn, final int arity, final boolean isVariadic) {
        addMethod(new FnMethod(fn, arity, isVariadic));
    }

    public void addMethod(final IFn fn, final Vector params, final Expr body) {
        addMethod(new FnMethod(fn, params, body));
    }
    
    private boolean hasSameSignature(final Fun rhs) {
        return ((this.methods.size() == rhs.methods.size())
                 && this.methods.keySet().equals(rhs.methods.keySet()));
    }

    private boolean hasSameName(final Fun rhs) {
        if (null == this.name) return (null == rhs.name);
        return this.name.equals(rhs.name);
    }

    private boolean hasSameDefaultFn(final Fun rhs) {
        if (this.defaultFn == rhs.defaultFn) return true;
        if (null == this.defaultFn) return false;
        return this.defaultFn.equals(rhs.defaultFn);
    }

    @VisibleForTesting
    boolean hasSameImplementation(final Fun rhs) {
        return this.hasSameDefaultFn(rhs) && this.methods.equals(rhs.methods);
    }

    private boolean hasSameBody(final Fun rhs) {
        if (null == this.defaultFn) {
            if (null == rhs.defaultFn) return this.methods.equals(rhs.methods);
            return false;
        }
        if (!this.defaultFn.equals(rhs.defaultFn)) return false;
        return this.methods.equals(rhs.methods);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj) {
            return false;
        }
        if (!Fun.class.isInstance(obj)) {
            return false;
        }
        Fun rhs = (Fun) obj;
        return hasSameSignature(rhs) && hasSameName(rhs)
            && hasSameBody(rhs) && hasSameImplementation(rhs);
    }

    @Override
    public Fun withMeta(Map newMeta) {
        if (this.meta.equals(newMeta)) {
            return this;
        }
        return new Fun(this, newMeta);
    }

    /**
     * Mark the function as a macro. This is an "not undoable" operation.
     */
    public void setMacro() {
        this.macro = true;
    }

    public boolean getMacro() {
        return this.macro;
    }
    /*
    public boolean isNative() {
        return (null == this.body) && (null != this.f);
    }

    public boolean isInterpreted() {
        return (null != this.body);
    }
    */

    public Vector arities() {
        Vector result = new Vector();
        if (null != this.defaultFn) {
            result.conj(new Int(this.defaultFn.arity()));
        }
        for (int arity : this.methods.keySet()) {
            result.conj(new Int(arity));
        }
        return result;
    }

    FnMethod getMethodWithArity(int arity) {
        FnMethod f = this.methods.get(arity);
        if (null == f) {
            if (null == this.defaultFn) {
                throw new RuntimeException("Wrong number of args (" + arity
                                           + ")"
                                           + (null == this.name
                                              ? ""
                                              : " passed to "+this.name.toString()));
            } else if (this.defaultFn.arity() > arity) {
                throw new RuntimeException("Wrong number of args (" + arity
                                           + ")"
                                           + (null == this.name
                                              ? ""
                                              : " passed to "+this.name.toString()));
            } else {
                f = this.defaultFn;
            }
        }
        return f;
    }

    public Expr getBody(int arity) {
        return getMethodWithArity(arity).body;
    }

    public Expr getIFn(int arity) {
        return getMethodWithArity(arity).body;
    }

    public boolean isInterpreted(int arity) {
        return (null != getBody(arity));
    }
    /**
     * Generate the environment for a new function call, binding the formal
     * parameters to the given {@code args}.
     */
    public Env genEnv(final Env env, final Seq args) {
        return getMethodWithArity(args.size()).genEnv(env, args);
    }
    
    public Expr invoke(final Seq args) throws Throwable {
        FnMethod f = getMethodWithArity(args.size());
        return f.invoke(args);
    }

    /*
    @Override
    public Expr call() {
        return this.invoke(new Seq());
    }
    @Override
    public Expr run() {
        return this.invoke(new Seq());
    }
    public <T> T visitMethods(Visitor<T> visitor) {
        Seq methods = new Seq(this.methods.values());
        if (null != this.defaultFn) methods.conj(this.defaultFn);
        return methods.accept(visitor);
    }
    */
        
    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitFun(this);
    }
    /*
    public <T> T visitParams(Visitor<T> visitor) {
        return this.params.accept(visitor);
    }
    public <T> T visitBody(Visitor<T> visitor) {
        return this.body.accept(visitor);
    }
    */
    
    public String toObfuscatedString() {
        if (null == name) {
            return "#function<" + (this.hashCode()) + ">";
        } else {
            return "#function<" + (this.name.name()) + ">";
        }
    }

    private String printSingletonMethod(FnMethod f) {
        StringBuffer buf = new StringBuffer("(fn* ");
        if (null != this.name) {
            buf.append(this.name.toString());
            buf.append(" ");
        }
        buf.append(f.toString(false));
        buf.append(")");
        return buf.toString();
    }

    @Override
    public String toString() {
        if (null != this.defaultFn && this.methods.isEmpty()) {
            return printSingletonMethod(this.defaultFn);
        }
        if (null == this.defaultFn && 1 == this.methods.size()) {
            for (FnMethod f : this.methods.values()) {
                return printSingletonMethod(f);
            }
        }
        
        StringBuffer buf = new StringBuffer("(fn* ");
        if (null != this.name) {
            buf.append(this.name.toString());
            buf.append(" ");
        }
        if (null != this.defaultFn) {
            buf.append(this.defaultFn.toString(true));
        }
        for (FnMethod f : this.methods.values()) {
            buf.append(" ");
            buf.append(f.toString(true));
        }
        buf.append(")");
        return buf.toString();
    }

    public String name() {
        if (null == this.name) {
            return "";
        }
        return this.name.name();
    }

    @Override
    public String type() {
        return "Fun";
    }
}
