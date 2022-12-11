package com.github.pqnelson.expr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class Map extends Expr implements Iterable<Expr>, IObj<Map>, ICountable {
    private final HashMap<Expr, Expr> table;
    private final Map meta;
    private final boolean isImmutable;
    public Map() {
        this(new HashMap<Expr, Expr>(), null, false);
    }

    public Map(final Map other) {
        this(other.table, null, other.isImmutable);
    }

    public Map(final Map other, final Map meta) {
        this(other.table, meta, other.isImmutable);
    }

    public Map(final java.util.Map<Expr, Expr> other) {
        this(other, null, false);
    }

    public Map(final java.util.Map<Expr, Expr> other, final Map meta) {
        this(other, meta, false);
    }

    public Map(final java.util.Map<Expr, Expr> other,
               final Map meta,
               final boolean immutable) {
        this.table = new HashMap<Expr, Expr>(other);
        this.meta = (null == meta ? null : meta.immutableCopy());
        this.isImmutable = immutable;
    }

    public Map(final Expr key, final Expr val) {
        this.table = new HashMap<Expr, Expr>();
        this.meta = null;
        this.isImmutable = false;
        this.table.put(key, val);
    }

    public Map immutableCopy() {
        HashMap<Expr, Expr> copy = new HashMap<>();
        for (Expr k : this.table.keySet()) {
            copy.put(k.clone(), this.table.get(k).clone());
        }
        return new Map(java.util.Map.copyOf(copy), null, true);
    }

    @Override
    public Map clone() {
        HashMap<Expr, Expr> copy = new HashMap<>();
        for (Expr k : this.table.keySet()) {
            copy.put(k.clone(), this.table.get(k).clone());
        }
        return new Map(copy, this.meta.immutableCopy());
    }
    
    @Override
    public Map meta() {
        return this.meta;
    }

    @Override
    public Map withMeta(final Map newMeta) {
        if ((null != this.meta) && (this.meta.equals(newMeta))) return this;
        return new Map(this, newMeta);
    }

    public Expr get(final Expr k) {
        return this.table.get(k);
    }

    public Expr get(final Expr k, final Expr defaultValue) {
        return this.table.getOrDefault(k, defaultValue);
    }

    public void assoc(final Expr k, final Expr v) {
        if (this.isImmutable) {
            throw new RuntimeException("Trying to assoc an immutable map");
        }
        this.table.put(k, v);
    }

    public void dissoc(final Expr k) {
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

    public boolean contains(final Expr k) {
        return this.table.containsKey(k);
    }

    public boolean isEmpty() {
        return this.table.isEmpty();
    }

    @Override
    public int size() {
        return this.table.size();
    }

    public Seq toSeq() {
        Seq result = new Seq();
        for (java.util.Map.Entry<Expr, Expr> e : this.table.entrySet()) {
            Vector kv = new Vector();
            kv.conj(e.getKey());
            kv.conj(e.getValue());
            result.conj(kv);
        }
        return result;
    }

    public Expr seq() {
        if (this.isEmpty()) {
            return Literal.NIL;
        }
        else return this.toSeq();
    }

    public Map merge(final Map newEntries) {
        HashMap<Expr, Expr> m = new HashMap<Expr, Expr>(this.table);
        m.putAll(newEntries.table);
        return new Map(m);
    }

    public <T> T accept(final Visitor<T> visitor) {
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
        if (!this.isEmpty()) {
            buf.deleteCharAt(buf.length() - 1);
        }
        buf.append("}");
        return buf.toString();
    }

    @Override
    public int hashCode() {
        return this.table.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        final Map rhs = (Map) obj;
        if (this.size() != rhs.size()) {
            return false;
        }
        for (Expr k : this.keys()) {
            if (!rhs.contains(k) || !this.get(k).equals(rhs.get(k))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String type() {
        return "Map";
    }
}
