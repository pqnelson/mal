package com.github.pqnelson.expr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class Map extends Expr implements Iterable<Expr>, IObj, ICountable {
    private final HashMap<Expr, Expr> table;
    public static final Map EMPTY = new Map();
    private Map meta = null;

    public Map() {
        this.table = new HashMap<Expr, Expr>();
    }

    public Map(Map other) {
        this.table = new HashMap<Expr, Expr>(other.table);
    }

    public Map(Map other, Map meta) {
        this.table = new HashMap<Expr, Expr>(other.table);
        this.meta = meta;
    }

    public Map(java.util.Map<Expr, Expr> other) {
        this.table = new HashMap<Expr, Expr>(other);
    }

    public Map(Expr key, Expr val) {
        this.table = new HashMap<Expr, Expr>();
        this.table.put(key, val);
    }

    @Override
    public Map meta() { return this.meta; }

    @Override
    public Map withMeta(Map newMeta) {
        if (this.meta.equals(newMeta)) return this;
        return new Map(this, newMeta);
    }

    public Expr get(Expr k) { return this.table.get(k); }

    public Expr get(Expr k, Expr defaultValue) {
        return this.table.getOrDefault(k, defaultValue);
    }

    public void assoc(Expr k, Expr v) {
        this.table.put(k, v);
    }

    public void dissoc(Expr k) {
        this.table.remove(k);
    }

    public Seq keys() {
        Seq result = new Seq();
        for (Expr k : this.table.keySet()) {
            result.conj(k);
        }
        return result;
    }
    public Seq values() {
        Seq result = new Seq();
        for (Expr v : this.table.values()) {
            result.conj(v);
        }
        return result;
    }

    @Override
    public Iterator<Expr> iterator() {
        return this.toSeq().iterator();
    }

    public boolean contains(Expr k) { return this.table.containsKey(k); }

    public boolean isEmpty() { return this.table.isEmpty(); }

    @Override
    public int size() { return this.table.size(); }

    public Seq toSeq() {
        if (this.isEmpty()) { return Seq.EMPTY; }
        Seq result = new Seq();
        for (java.util.Map.Entry<Expr, Expr> e : this.table.entrySet()) {
            Seq kv = new Seq();
            kv.conj(e.getKey());
            kv.conj(e.getValue());
            result.conj(kv);
        }
        return result;
    }

    public Expr seq() {
        if (this.isEmpty()) return Literal.NIL;
        else return this.toSeq();
    }

    public Map merge(Map newEntries) {
        HashMap<Expr, Expr> m = new HashMap<Expr, Expr>(this.table);
        m.putAll(newEntries.table);
        return new Map(m);
    }

    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitMap(this);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("{");
        for (Expr k : this.keys()) {
            buf.append(k.toString());
            buf.append(" ");
            buf.append(table.get(k).toString());
            buf.append(" ");
        }
        if (!this.isEmpty()) buf.deleteCharAt(buf.length() - 1);
        buf.append("}");
        return buf.toString();
    }

    @Override
    public int hashCode() {
        return this.table.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (null == obj) return false;
        if (obj.getClass() != this.getClass()) return false;
        Map rhs = (Map)obj;
        if (this.size() != rhs.size()) return false;
        for (Expr k : this.keys()) {
            if (!rhs.contains(k)) return false;
            if (!this.get(k).equals(rhs.get(k))) return false;
        }
        return true;
    }

    @Override
    public String type() {
        return "Map";
    }
}