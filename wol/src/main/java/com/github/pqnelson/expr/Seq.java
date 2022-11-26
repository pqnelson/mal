package com.github.pqnelson.expr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Seq extends Expr implements Iterable<Expr> {
    final List<Expr> contents;
    public Seq() {
        this(new ArrayList<>());
    }
    public Seq(List<Expr> contents) {
        this.contents = contents;
    }
    public Seq(Seq seq) {
        this.contents = List.copyOf(seq.contents);
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

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitSeq(this);
    }
}
