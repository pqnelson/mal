package com.github.pqnelson.expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Seq extends Expr implements Iterable<Expr>, IObj, ICountable {
    final List<Expr> contents;
    private Map meta = null;

    public Seq() {
        this(new ArrayList<>());
    }
    public Seq(final List<Expr> seqContents) {
        this.contents = seqContents;
    }
    public Seq(final List<Expr> seqContents, final Map metadata) {
        this.contents = seqContents;
        this.meta = metadata;
    }
    public Seq(final Seq seq) {
        this.contents = new ArrayList<>(seq.contents);
    }
    public Seq(final Seq seq, final Map metadata) {
        this.contents = new ArrayList<>(seq.contents);
        this.meta = metadata;
    }

    public static Seq singleton(final Expr e) {
        Seq coll = new Seq();
        coll.conj(e);
        return coll;
    }

    @Override
    public Map meta() {
        return this.meta;
    }

    @Override
    public Seq withMeta(final Map newMeta) {
        if (this.meta.equals(newMeta)) {
            return this;
        }
        return new Seq(this, newMeta);
    }

    public Expr rator() {
        if (!this.contents.isEmpty()) {
            return this.get(0);
        } else {
            return Literal.NIL;
        }
    }

    @Override
    public Iterator<Expr> iterator() {
        return contents.iterator();
    }

    public Seq slice(final int i) {
        if (i >= this.size()) {
            return new Seq();
        }
        return new Seq(this.contents.subList(i, this.size()));
    }
    public Seq slice(final int start, final int end) {
        if (start >= this.size()) {
            return new Seq();
        }
        return new Seq(this.contents.subList(start, end));
    }

    public Expr get(final int i) {
        return this.contents.get(i);
    }

    public Expr get(final int i, final Expr defaultValue) {
        if (this.contents.size() <= i) {
            return defaultValue;
        }
        return this.contents.get(i);
    }

    @Override
    public int size() {
        return this.contents.size();
    }

    public Expr last() {
        return this.get(size() - 1);
    }

    public void prepend(final Expr e) {
        this.contents.add(0, e);
    }

    public Seq cons(final Expr e) {
        Seq result = new Seq(this);
        result.prepend(e);
        return result;
    }

    public Seq butLast() {
        if (this.size() < 2) {
            return this;
        }
        return new Seq(this.contents.subList(0, size() - 2));
    }

    /**
     * Add an expression to the END of the list.
     */
    public void conj(final Expr e) {
        this.contents.add(e);
    }

    public Expr first() {
        if (contents.isEmpty()) {
            return null;
        }
        return contents.get(0);
    }

    public boolean isEmpty() {
        return this.contents.isEmpty();
    }

    @Override
    public <T> T accept(final Visitor<T> visitor) {
        return visitor.visitSeq(this);
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
        final Seq rhs = (Seq) obj;
        if (this.size() != rhs.size()) {
            return false;
        }
        for (int i = 0; i < this.size(); i++) {
            if (!this.get(i).equals(rhs.get(i))) {
                return false;
            }
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

    public Seq sort(final Comparator<Expr> c) {
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

    public void destructiveSort(final Comparator<Expr> c) {
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

    public Expr seq() {
        if (this.isEmpty()) {
            return Literal.NIL;
        }
        return this;
    }

    public Seq concat(final Expr obj) {
        if (obj.isNil()) {
            return this;
        }
        Seq other = (Seq) obj;
        if (other.isEmpty()) {
            return this;
        }
        if (this.isEmpty()) {
            return other;
        }
        Seq result = new Seq(this);
        for (Expr e : other) {
            result.conj(e);
        }
        return result;
    }

    @Override
    public String type() {
        return "Seq";
    }

    public Vector vec() {
        return new Vector(new ArrayList<>(this.contents));
    }

    public Seq filter(final Predicate<Expr> criteria) {
        return new Seq(this.contents
                       .stream()
                       .filter(criteria)
                       .collect(Collectors.<Expr>toList()));
    }

    public Seq remove(final Predicate<Expr> criteria) {
        return this.filter(criteria.negate());
    }

    public Seq takeWhile(final Predicate<Expr> criteria) {
        return new Seq(this.contents
                       .stream()
                       .takeWhile(criteria)
                       .collect(Collectors.<Expr>toList()));
    }

    public Seq dropWhile(final Predicate<Expr> criteria) {
        return new Seq(this.contents
                       .stream()
                       .dropWhile(criteria)
                       .collect(Collectors.<Expr>toList()));
    }
}
