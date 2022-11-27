package com.github.pqnelson.expr;

/**
 * The interface for a function, used by the Evaluator.
 */
@FunctionalInterface
public interface IFn {
    public Expr invoke(Seq args) throws Throwable;
}