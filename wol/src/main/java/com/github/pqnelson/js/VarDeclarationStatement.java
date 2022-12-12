package com.github.pqnelson.js;

import java.util.ArrayList;
import java.util.List;

/**
 * Declarations of new variables.
 *
 * <p>This includes all {@code var}, {@code let}, and {@code const}
 * declarations. This defaults to {@code let}.</p>
 */
public class VarDeclarationStatement extends Statement {
    public enum Scope {
        VAR,
        LET,
        CONST
    };
    class Binding {
        public final Name name;
        public final JsExpr definiens;
        public Binding(Name n, JsExpr e) {
            this.name = n;
            this.definiens = e;
        }
    }
    private Scope scope;
    private List<Binding> bindings;

    public VarDeclarationStatement() {
        this.scope = Scope.LET;
        bindings = new ArrayList<>();
    }

    public VarDeclarationStatement(Name name, JsExpr definiens) {
        this(Scope.LET, name, definiens);
    }

    public VarDeclarationStatement(Scope scope, Name name, JsExpr definiens) {
        this.scope = scope;
        this.bindings = new ArrayList<>();
        this.bindings.add(new Binding(name, definiens));
    }

    public Scope getScope() {
        return this.scope;
    }

    public void addBinding(Name name, JsExpr definiens) {
        bindings.add(new Binding(name, definiens));
    }

    @Override
    public <T> T accept(final Visitor<T> visitor) {
        return visitor.visitDeclaration(this);
    }

    @Override
    public String toString() {
        if (this.bindings.isEmpty()) return "";
        StringBuffer buf = new StringBuffer(this.scope.name().toLowerCase());
        buf.append(" ");
        for (Binding binding : this.bindings) {
            buf.append(binding.name.toString());
            buf.append(" = ");
            buf.append(binding.definiens.toString());
            buf.append(", ");
        }
        buf.delete(buf.length()-2,buf.length());
        buf.append(";");
        return buf.toString();
    }
}
