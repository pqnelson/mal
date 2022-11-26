package com.github.pqnelson.expr;

/**
 * The interface for a function, used by the Evaluator.
 */
interface IFn {
    Expr invoke(Seq args);
}