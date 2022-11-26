package com.github.pqnelson;

import java.util.ArrayList;
import java.util.List;

import static com.github.pqnelson.TokenType.*;
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
    interface IFn {
        Expr invoke(Seq args);
    }
    interface Visitor<T> {
        T visitFun(Fun expr);
        T visitVector(Vector expr);
        T visitSeq(Seq expr);
        T visitSymbol(Symbol expr);
        T visitKeyword(Keyword expr);
        T visitLiteral(Literal expr);
    }

    abstract static class Fun extends Expr {
        Fun() {
            this.params = null;
            this.body = null;
            this.name = null;
            this.macro = false;
        }
        Fun(Symbol funName) {
            this.params = null;
            this.body = null;
            this.name = funName;
            this.macro = false;
        }
        Fun(Vector params, Seq body) {
            this.params = params;
            this.body = body;
            this.name = null;
            this.macro = false;
        }
        Fun(Vector params, Seq body, Symbol funName) {
            this.params = params;
            this.body = body;
            this.name = funName;
            this.macro = false;
        }
        final Vector params;
        final Seq body;
        final Symbol name;
        private boolean macro;
        void setMacro() {
            this.macro = true;
        }
        boolean isMacro() {
            return this.macro;
        }
        Env genEnv(Env env, Seq args) {
            return new Env(env, params, args);
        }
        abstract Expr invoke(Seq args);
        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitFun(this);
        }
    }
    /**
     * A vector.
     */
    static class Vector extends Expr {
        Vector() {
            this(new ArrayList<Expr>());
        }
        Vector(List<Expr> contents) {
            this.contents = contents;
        }
        final List<Expr> contents;
        int size() { return contents.size(); }
        Expr get(int i) { return contents.get(i); }
        Expr last() { return contents.get(contents.size()-1); }
        Vector slice(int i) {
            return new Vector(this.contents.subList(i, this.size()));
        }
        Seq seq() {
            return new Seq(List.copyOf(this.contents));
        }
        void conj(Expr e) {
            this.contents.add(e);
        }
        Expr first() {
            if (contents.isEmpty()) return null;
            return contents.get(0);
        }
        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitVector(this);
        }
    }
    /**
     * A list.
     */
    static class Seq extends Expr {
        Seq() {
            this(new ArrayList<>());
        }
        Seq(List<Expr> contents) {
            this.contents = contents;
        }
        Seq(Seq seq) {
            this.contents = List.copyOf(seq.contents);
        }
        final List<Expr> contents;
        Expr rator() {
            if (!this.contents.isEmpty()) {
                return this.get(0);
            } else {
                return null;
            }
        }
        Seq slice(int i) {
            return new Seq(this.contents.subList(i, this.size()));
        }
        Seq slice(int start, int end) {
            return new Seq(this.contents.subList(start, end));
        }
        Expr get(int i) {
            return this.contents.get(i);
        }
        int size() { return this.contents.size(); }
        Expr last() {
            return this.get(size()-1);
        }
        void prepend(Expr e) {
            this.contents.add(0, e);
        }
        Seq butLast() {
            if (this.size() < 2) { return this; }

            return new Seq(this.contents.subList(0, size()-2));
        }
        void conj(Expr e) {
            this.contents.add(e);
        }
        Expr first() {
            if (contents.isEmpty()) return null;
            return contents.get(0);
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
        String name() {
            return this.identifier.lexeme;
        }

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
        boolean isNil() { return NIL == token.type; }
        boolean isTrue() { return TRUE == token.type; }
        boolean isFalse() { return FALSE == token.type; }
        boolean isFalsy() { return isFalse() || isNil(); }
        boolean isString() { return STRING == token.type; }
        boolean isNumber() { return NUMBER == token.type; }
        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLiteral(this);
        }
    }

    abstract <T> T accept(Visitor<T> visitor);
    boolean isFunction() { return Fun.class.isInstance(this); }
    boolean isList() { return Seq.class.isInstance(this); }
    boolean isSymbol() { return Symbol.class.isInstance(this); }
    boolean isVector() { return Vector.class.isInstance(this); }
    boolean isLiteral() { return Literal.class.isInstance(this); }
}