package com.github.pqnelson.expr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A vector, i.e., ordered tuple.
 */
public class Vector extends Expr implements Iterable<Expr>, IObj {
    final List<Expr> contents;
    private Map meta = null;
    public final static Vector EMPTY = new Vector();

    public Vector() {
        this(new ArrayList<Expr>());
    }

    public Vector(List<Expr> contents) {
        this.contents = contents;
    }

    public Vector(Vector other) {
        this.contents = List.copyOf(other.contents);
    }

    public Vector(Vector other, Map meta) {
        this.contents = List.copyOf(other.contents);
        this.meta = meta;
    }

    @Override
    public Map meta() { return this.meta; }

    @Override
    public Vector withMeta(Map newMeta) {
        if (this.meta.equals(newMeta)) return this;
        return new Vector(this, newMeta);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (null == obj) return false;
        if (obj.getClass() != this.getClass()) return false;
        Vector rhs = (Vector)obj;
        if (this.size() != rhs.size()) return false;
        for (int i = 0; i < this.size(); i++) {
            if (!this.get(i).equals(rhs.get(i))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return this.contents.hashCode();
    }

    @Override
    public Iterator<Expr> iterator() {
        return contents.iterator();
    }

    public int size() { return contents.size(); }

    public Expr get(int i) { return contents.get(i); }

    public Expr last() { return contents.get(contents.size()-1); }

    public Vector slice(int i) {
        return new Vector(this.contents.subList(i, this.size()));
    }

    public Seq seq() {
        return new Seq(List.copyOf(this.contents));
    }

    public void conj(Expr e) {
        this.contents.add(e);
    }

    public Expr first() {
        if (contents.isEmpty()) return null;
        return contents.get(0);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitVector(this);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for (Expr e : this) {
            buf.append(e.toString());
            buf.append(" ");
        }
        buf.deleteCharAt(buf.length() - 1);
        buf.append("]");
        return buf.toString();
    }
}