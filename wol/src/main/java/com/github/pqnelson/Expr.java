package com.github.pqnelson;

import java.util.List;
/**
 * The abstract syntax tree for Lisp.
 *
 * This more or less corresponds to "types.js".
 *
 * @TODO HashMap
 * @TODO Set
 * @TODO Atom(?)
 */
abstract class Expr {
    interface Visitor<T> {
        T visitDef(Def expr);
        T visitIf(If expr);
        T visitLet(Let expr);
        T visitFun(Fun expr);
        T visitColl(Coll expr);
        T visitSymbol(Symbol expr);
        T visitKeyword(Keyword expr);
        T visitLiteral(Literal expr);
    }

    static class Def extends Expr {
        Def(Symbol name, Expr value) {
            this.name = name;
            this.value = value;
        }
        final Symbol name;
        final Expr value;
        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitDef(this);
        }
    }

    static class If extends Expr {
        If(Expr test, Expr trueBranch) {
            this(test, trueBranch, null);
        }
        If(Expr test, Expr trueBranch, Expr falseBranch) {
            this.test = test;
            this.trueBranch = trueBranch;
            this.falseBranch = falseBranch;
        }
        final Expr test;
        final Expr trueBranch;
        final Expr falseBranch;
        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitIf(this);
        }
    }

    static class Let extends Expr {
        Let(Coll bindings, Coll body) {
            this.bindings = bindings;
            this.body = body;
        }
        final Coll bindings;
        final Coll body;
        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLet(this);
        }
    }

    static class Fun extends Expr {
        Fun(Coll args, Coll body, Symbol funName) {
            this.args = args;
            this.body = body;
            this.name = funName;
        }
        Fun(Coll args, Coll body) {
            this.args = args;
            this.body = body;
            this.name = null;
        }
        final Coll args;
        final Coll body;
        final Symbol name;
        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitFun(this);
        }
    }

    /**
     * A vector or list; since we're emitting them as Javascript arrays, they're
     * the same.
     */
    static class Coll extends Expr {
        Coll(List<Expr> contents) {
            this.contents = contents;
        }
        final List<Expr> contents;
        Expr rator() {
            if (!this.contents.isEmpty()) {
                return this.contents.get(0);
            } else {
                return null;
            }
        }
        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitColl(this);
        }
    }
    static class Symbol extends Expr {
        Symbol(Token identifier) {
            this.identifier = identifier;
        }
        final Token identifier;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitSymbol(this);
        }
    }
    static class Keyword extends Expr {
        Keyword(Token identifier) {
            this.identifier = identifier;
        }
        final Token identifier;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitKeyword(this);
        }
    }
    static class Literal extends Expr {
        Literal(Object value) {
            this.value = value;
        }
        final Object value;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLiteral(this);
        }
    }

    abstract <T> T accept(Visitor<T> visitor);
}