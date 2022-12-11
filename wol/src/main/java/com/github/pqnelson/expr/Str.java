package com.github.pqnelson.expr;

public class Str extends Literal {
    public Str(final String s) {
        super((Object) s);
    }

    @Override
    public Str clone() {
        return new Str(new String(this.value()));
    }
    
    @Override
    public String value() {
        return (String) super.value();
    }

    @Override
    public String toString() {
        return this.value();
    }

    public Str enquote() {
        return new Str("\""+this.toString()+"\"");
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
        Str rhs = (Str) obj;
        return this.value().equals(rhs.value());
    }

    @Override
    public int hashCode() {
        return this.value().hashCode();
    }

    public Str substring(final int start, final int end) {
        return new Str(this.value().substring(start, end));
    }

    public Expr seq() {
        if (this.value().equals("")) {
            return Literal.NIL;
        }
        Seq letters = new Seq();
        for (int i = 0; i < this.value().length(); i++) {
            letters.conj(this.substring(i, i + 1));
        }
        return letters;
    }

    @Override
    public String type() {
        return "Str";
    }
}
