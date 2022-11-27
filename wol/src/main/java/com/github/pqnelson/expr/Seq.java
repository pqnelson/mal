package com.github.pqnelson.expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Seq extends Expr implements Iterable<Expr>, IObj {
    final List<Expr> contents;
    public static final Seq EMPTY = new Seq();
    private Map meta = null;

    public Seq() {
        this(new ArrayList<>());
    }
    public Seq(List<Expr> contents) {
        this.contents = contents;
    }
    public Seq(List<Expr> contents, Map meta) {
        this.contents = contents;
        this.meta = meta;
    }
    public Seq(Seq seq) {
        this.contents = List.copyOf(seq.contents);
    }
    public Seq(Seq seq, Map meta) {
        this.contents = List.copyOf(seq.contents);
        this.meta = meta;
    }

    @Override
    public Map meta() {
        return this.meta;
    }

    @Override
    public Seq withMeta(Map newMeta) {
        if (this.meta.equals(newMeta)) return this;
        return new Seq(this, newMeta);
    }

    public Expr rator() {
        if (!this.contents.isEmpty()) {
            return this.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Iterator<Expr> iterator() {
        return contents.iterator();
    }

    public Seq slice(int i) {
        return new Seq(this.contents.subList(i, this.size()));
    }
    public Seq slice(int start, int end) {
        return new Seq(this.contents.subList(start, end));
    }

    public Expr get(int i) {
        return this.contents.get(i);
    }

    public int size() { return this.contents.size(); }

    public Expr last() {
        return this.get(size()-1);
    }

    public void prepend(Expr e) {
        this.contents.add(0, e);
    }

    public Seq butLast() {
        if (this.size() < 2) { return this; }

        return new Seq(this.contents.subList(0, size()-2));
    }

    public void conj(Expr e) {
        this.contents.add(e);
    }

    public Expr first() {
        if (contents.isEmpty()) return null;
        return contents.get(0);
    }

    public boolean isEmpty() { return this.contents.isEmpty(); }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitSeq(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (null == obj) return false;
        if (obj.getClass() != this.getClass()) return false;
        Seq rhs = (Seq)obj;
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

    /**
     * Create a sorted copy of the Seq comparing entries's {@code hashCode()}.
     */
    public Seq sort() {
        return this.sort((o1, o2) -> (o2.hashCode() - o1.hashCode()));
    }

    public Seq sort(Comparator<Expr> c) {
        ArrayList<Expr> sorted = new ArrayList<>(this.contents);
        Collections.sort(sorted, c);
        return new Seq(sorted);
    }

    /**
     * Destructively sort in-place, comparing entries's {@code hashCode()}.
     */
    public void destructiveSort() {
        this.destructiveSort((o1, o2) -> (o2.hashCode() - o1.hashCode()));
    }

    public void destructiveSort(Comparator<Expr> c) {
        Collections.sort(this.contents, c);
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        for (Expr e : this) {
            buf.append(e.toString());
            buf.append(" ");
        }
        buf.deleteCharAt(buf.length() - 1);
        buf.append(")");
        return buf.toString();
    }
}
