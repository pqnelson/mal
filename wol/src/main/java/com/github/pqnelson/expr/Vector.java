package com.github.pqnelson.expr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A vector, i.e., ordered tuple.
 */
public class Vector extends Expr implements Iterable<Expr>, IObj, ICountable {
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
        this.contents = new ArrayList<>(other.contents);
    }

    public Vector(Vector other, Map meta) {
        this.contents = new ArrayList<>(other.contents);
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

    @Override
    public int size() { return contents.size(); }

    public Expr get(int i) { return contents.get(i); }


    Expr _get(int i, Expr defaultValue) {
        if (i < 0 || this.contents.size() <= i) return defaultValue;
        return this.contents.get(i);
    }


    public Expr get(Expr i, Expr defaultValue) throws NoSuchMethodException {
        if (!i.isInt()) throw new NoSuchMethodException("Vector::get requires an integer index");
        return this._get(((Int)i).value().intValue(), defaultValue);
    }

    public Expr last() { return contents.get(contents.size()-1); }

    public Vector slice(int i) {
        return new Vector(this.contents.subList(i, this.size()));
    }

    public Expr seq() {
        if (this.isEmpty()) return Literal.NIL;
        return new Seq(new ArrayList<>(this.contents));
    }

    public void conj(Expr e) {
        this.contents.add(e);
    }

    public Expr first() {
        if (contents.isEmpty()) return null;
        return contents.get(0);
    }

    public boolean isEmpty() { return contents.isEmpty(); }

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

    @Override public String type() {
        return "Vector";
    }
}
