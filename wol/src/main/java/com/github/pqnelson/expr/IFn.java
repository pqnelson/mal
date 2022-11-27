package com.github.pqnelson.expr;

import java.util.Objects;

/**
 * The interface for a function, used by the Evaluator.
 */
@FunctionalInterface
public interface IFn {
    public Expr invoke(Seq args) throws Throwable;
    /*
    default IFn compose(IFn before) {
        Objects.requireNonNull(before);
        return (Seq args) -> invoke(Seq.singleton(before.invoke(args)));
    }

    default IFn compose(Function<Seq,Expr> before) {
        Objects.requireNonNull(before);
        return (Seq args) -> invoke(Seq.singleton(before.apply(args)));
    }

    default IFn compose(Function<Seq,Seq> before) {
        Objects.requireNonNull(before);
        return (Seq args) -> invoke(before.apply(args));
    }
    */
}