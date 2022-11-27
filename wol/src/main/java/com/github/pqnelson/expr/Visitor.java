package com.github.pqnelson.expr;

/**
 * Visitor pattern to simplify life.
 */
public interface Visitor<T> {
    T visitFun(Fun expr);
    T visitVector(Vector expr);
    T visitSeq(Seq expr);
    T visitSymbol(Symbol expr);
    T visitKeyword(Keyword expr);
    T visitLiteral(Literal expr);
    T visitMap(Map expr);
}