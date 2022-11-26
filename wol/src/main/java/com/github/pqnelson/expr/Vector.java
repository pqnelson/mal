package com.github.pqnelson.expr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A vector, i.e., ordered tuple.
 */
public class Vector extends Expr implements Iterable<Expr> {
    final List<Expr> contents;

    public Vector() {
        this(new ArrayList<Expr>());
    }

    public Vector(List<Expr> contents) {
        this.contents = contents;
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
}
