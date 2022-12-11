package com.github.pqnelson.expr;

import com.github.pqnelson.Token;
import com.github.pqnelson.TokenType;

public class Symbol extends Expr implements IObj {
    public static final Symbol CATCH = specialForm("catch");
    public static final Symbol DEF = specialForm("def");
    public static final Symbol DEFMACRO = specialForm("defmacro");
    public static final Symbol DO = specialForm("do");
    public static final Symbol FN_STAR = specialForm("fn*");
    public static final Symbol IF = specialForm("if");
    public static final Symbol LET_STAR = specialForm("let*");
    public static final Symbol MACROEXPAND = specialForm("macroexpand");
    public static final Symbol QUASIQUOTE = specialForm("quasiquote");
    public static final Symbol QUASIQUOTE_EXPAND
        = specialForm("quasiquote-expand");
    public static final Symbol QUOTE = specialForm("quote");
    public static final Symbol SPLICE = specialForm("splice");
    public static final Symbol TRY = specialForm("try");
    public static final Symbol UNQUOTE = specialForm("unquote");

    private final String name;
    private final Map meta;
    private final boolean isIdentifier;

    public Symbol(final String name) {
        this(name, true, null);
    }

    public Symbol(final Token identifier) {
        this(identifier.lexeme,
             (TokenType.IDENTIFIER == identifier.type),
             null);
    }

    public Symbol(final String name, Map meta) {
        this(name, true, meta);
    }

    // Constructing a special form should be restricted to, well,
    // this file.
    private Symbol(final String name, boolean isIdentifier) {
        this(name, isIdentifier, null);
    }
    
    private Symbol(final String name, boolean isIdentifier, Map meta) {
        this.name = name;
        this.isIdentifier = isIdentifier;
        this.meta = meta;
    }

    // Helper function to make the creation of the constants above
    // more readable.
    private static final Symbol specialForm(final String name) {
        return new Symbol(name, false);
    }

    public boolean isIdentifier() {
        return this.isIdentifier;
    }

    public boolean isSpecialForm() {
        return !this.isIdentifier;
    }

    @Override
    public Map meta() {
        return this.meta;
    }

    @Override
    public Symbol withMeta(final Map metadata) {
        if ((null == this.meta && null == metadata)
            || (null != this.meta && this.meta.equals(metadata))
            || this.isSpecialForm()) {
            return this;
        }

        return new Symbol(this.name, this.isIdentifier, meta);
    }

    public final String name() {
        return this.name;
    }

    @Override
    public <T> T accept(final Visitor<T> visitor) {
        return visitor.visitSymbol(this);
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
        Symbol rhs = (Symbol) obj;
        // Since Symbols are used for any identifier (like "do" and "fn*"),
        // check the TokenTypes are the same
        return (this.isIdentifier == rhs.isIdentifier) &&
            (this.name.equals(rhs.name));
    }

    @Override
    public final int hashCode() {
        // Treating a symbol as an ordered pair of
        // (name, isIdentifier).
        return 31 * this.name.hashCode() + Boolean.hashCode(this.isIdentifier);
    }

    @Override
    public String type() {
        return "Symbol";
    }

    @Override
    public String toString() {
        return this.name;
    }
}
