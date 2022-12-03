package com.github.pqnelson;

import java.util.HashMap;
import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Symbol;
import com.github.pqnelson.expr.Vector;

public final class Env {
    private final Env outer;
    private HashMap<String, Expr> table = new HashMap<>();
    private static final Symbol AMPERSAND = new Symbol("&");

    public Env() {
        this(null);
    }

    public Env(final Env parent) {
        this.outer = parent;
    }

    /**
     * Extend an environment due to a function call or let-bindings.
     *
     * <p>Since these always use vectors for parameters (for functions) or
     * for bindings (for {@code let*}), we always expect a vector of variables.
     *
     * @param parent The environment we are extending.
     * @param vars A vector of symbols we are binding in the new Environment.
     * @param exprs A list of associated values for the new bindings.
     */
    public Env(final Env parent, final Vector vars, final Seq exprs) {
        /* assert ((vars.size() < exprs.size()
                       && vars.get(vars.size()-2).equals(AMPERSAND))
                   || (vars.size() == exprs.size())); */
        this.outer = parent;
        for (int i = 0; i < vars.size(); i++) {
            final Symbol s = ((Symbol) vars.get(i));
            if (s.equals(this.AMPERSAND)) {
                String k = ((Symbol) vars.get(i + 1)).name();
                table.put(k, exprs.slice(i));
                break;
            } else {
                table.put(s.name(), exprs.get(i));
            }
        }
    }

    public Env find(final Symbol key) {
        if (table.containsKey(key.name())) {
            return this;
        } else if (null != outer) {
            return outer.find(key);
        } else {
            return null;
        }
    }

    public Expr get(final Symbol key) {
        Env e = find(key);
        if (null == e) {
            throw new RuntimeException("'" + key.name() + "' not found");
        } else {
            return e.table.get(key.name());
        }
    }

    public Env set(final Symbol key, final Expr value) {
        table.put(key.name(), value);
        return this;
    }
}