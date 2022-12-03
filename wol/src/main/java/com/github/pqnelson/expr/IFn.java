package com.github.pqnelson.expr;

/**
 * The interface for a function, used by the Evaluator.
 */
@FunctionalInterface
public interface IFn {
    Expr invoke(Seq args) throws Throwable;
}