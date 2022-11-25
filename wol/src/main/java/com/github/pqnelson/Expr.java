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
        T visitPair(Pair expr);
        T visitDef(Def expr);
        T visitIf(If expr);
        T visitLet(Let expr);
        T visitFun(Fun expr);
        T visitVector(Vector expr);
        T visitSeq(Seq expr);
        T visitSymbol(Symbol expr);
        T visitKeyword(Keyword expr);
        T visitLiteral(Literal expr);
    }

    static class Pair extends Expr {
        Pair(Token specialForm, Expr body) {
            this.specialForm = specialForm;
            this.body = body;
        }
        final Token specialForm;
        final Expr body;
        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitPair(this);
        }
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
        Let(Vector bindings, Seq body) {
            this.bindings = bindings;
            this.body = body;
        }
        final Vector bindings;
        final Seq body;
        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLet(this);
        }
    }

    static class Fun extends Expr {
        Fun(Vector args, Seq body, Symbol funName) {
            this.args = args;
            this.body = body;
            this.name = funName;
        }
        Fun(Vector args, Seq body) {
            this.args = args;
            this.body = body;
            this.name = null;
        }
        final Vector args;
        final Seq body;
        final Symbol name;
        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitFun(this);
        }
    }

    /**
     * A vector.
     */
    static class Vector extends Expr {
        Vector(List<Expr> contents) {
            this.contents = contents;
        }
        final List<Expr> contents;
        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitVector(this);
        }
    }
    /**
     * A list.
     */
    static class Seq extends Expr {
        Seq(List<Expr> contents) {
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
            return visitor.visitSeq(this);
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
        Literal(Token token) {
            this(token, token.literal);
        }
        Literal(Token token, Object value) {
            this.token = token;
            this.value = value;
        }
        public final Token token;
        public final Object value;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLiteral(this);
        }
    }

    abstract <T> T accept(Visitor<T> visitor);
}