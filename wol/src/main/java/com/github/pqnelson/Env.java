package com.github.pqnelson;

import java.util.HashMap;

public class Env {
    Env outer = null;
    HashMap<String, Expr> table = new HashMap<>();

    public Env(Env outer) {
        this.outer = outer;
    }

    public Env(Env outer, Expr.Seq vars, Expr.Seq exprs) {
        assert (vars.contents.size() == exprs.contents.size());
        this.outer = outer;
        for (int i=0; i < vars.contents.size(); i++) {
            String s = ((Expr.Symbol)vars.get(i)).name();
            if (s.equals("&")) {
                String k = ((Expr.Symbol)vars.get(i+1)).name();
                table.put(k, exprs.slice(i));
                break;
            } else {
                table.put(s, exprs.get(i));
            }
        }
    }

    public Env(Env outer, Expr.Vector vars, Expr.Seq exprs) {
        assert (vars.contents.size() == exprs.contents.size());
        this.outer = outer;
        for (int i=0; i < vars.contents.size(); i++) {
            String s = ((Expr.Symbol)vars.get(i)).name();
            if (s.equals("&")) {
                String k = ((Expr.Symbol)vars.get(i+1)).name();
                table.put(k, exprs.slice(i));
                break;
            } else {
                table.put(s, exprs.get(i));
            }
        }
    }

    public Env find(Expr.Symbol key) {
        if (table.containsKey(key.name())) {
            return this;
        } else if (null != outer) {
            return outer.find(key);
        } else {
            return null;
        }
    }

    public Expr get(Expr.Symbol key) {
        Env e = find(key);
        if (null == e) {
            throw new RuntimeException("'"+key.name()+"' not found");
        } else {
            return e.table.get(key.name());
        }
    }

    public Env set(Expr.Symbol key, Expr value) {
        table.put(key.name(), value);
        return this;
    }
}