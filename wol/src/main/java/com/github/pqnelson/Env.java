package com.github.pqnelson;

import java.util.HashMap;
import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Symbol;
import com.github.pqnelson.expr.Vector;

public class Env {
    Env outer = null;
    HashMap<String, Expr> table = new HashMap<>();

    public Env() { }

    public Env(Env outer) {
        this.outer = outer;
    }

    public Env(Env outer, Seq vars, Seq exprs) {
        assert (vars.size() == exprs.size());
        this.outer = outer;
        for (int i=0; i < vars.size(); i++) {
            String s = ((Symbol)vars.get(i)).name();
            if (s.equals("&")) {
                String k = ((Symbol)vars.get(i+1)).name();
                table.put(k, exprs.slice(i));
                break;
            } else {
                table.put(s, exprs.get(i));
            }
        }
    }

    public Env(Env outer, Vector vars, Seq exprs) {
        assert (vars.size() == exprs.size());
        this.outer = outer;
        for (int i=0; i < vars.size(); i++) {
            String s = ((Symbol)vars.get(i)).name();
            if (s.equals("&")) {
                String k = ((Symbol)vars.get(i+1)).name();
                table.put(k, exprs.slice(i));
                break;
            } else {
                table.put(s, exprs.get(i));
            }
        }
    }

    public Env find(Symbol key) {
        if (table.containsKey(key.name())) {
            return this;
        } else if (null != outer) {
            return outer.find(key);
        } else {
            return null;
        }
    }

    public Expr get(Symbol key) {
        Env e = find(key);
        if (null == e) {
            throw new RuntimeException("'"+key.name()+"' not found");
        } else {
            return e.table.get(key.name());
        }
    }

    public Env set(Symbol key, Expr value) {
        table.put(key.name(), value);
        return this;
    }
}