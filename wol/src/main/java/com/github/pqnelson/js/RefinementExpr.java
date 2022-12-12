package com.github.pqnelson.js;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * A Javascript Identifier.
 *
 * <p>A Javascript identifier consists of letters, digits, underscore {@code _},
 * and dollar sign {@code $}. Althought the ECMA standard allows Unicode
 * letters, we restrict attention to the Latin alphabet {@code a-zA-Z}.</p>
 */
public class RefinementExpr extends JsExpr {
    public final Name identifier;
    public List<Name> components;

    private RefinementExpr() {
        this.identifier = null;
        this.components = null;
    }

    public RefinementExpr(Name name) {
        this.identifier = name;
        this.components = new ArrayList<>();
    }

    public RefinementExpr(Name name, Name... subcomponents) {
        this.identifier = name;
        this.components = new ArrayList<>(Arrays.asList(subcomponents));
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(this.identifier.toString());
        for (Name part : this.components) {
            buf.append("[");
            buf.append(part.toString());
            buf.append("]");
        }
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (null == obj) return false;
        if (this.getClass() != obj.getClass()) return false;
        RefinementExpr rhs = (RefinementExpr) obj;
        return this.identifier.equals(rhs.identifier)
            && this.components.equals(rhs.components);
    }

    @Override
    public int hashCode() {
        return 31*this.identifier.hashCode() + this.components.hashCode();
    }

    @Override
    public <T> T accept(final ExprVisitor<T> visitor) {
        return visitor.visitRefinement(this);
    }
}
