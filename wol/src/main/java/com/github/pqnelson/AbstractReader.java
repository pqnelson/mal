package com.github.pqnelson;

import com.github.pqnelson.expr.Expr;

/**
 * Since we are exploring the "space" of Lisp readers, it helps to have
 * an abstract base class to use when writing the {@code ReaderMacro} interface.
 */
public abstract class AbstractReader {
    /**
     * Check if there's anything left to read.
     *
     * @return True if the underlying input is exhausted.
     */
    public abstract boolean isFinished();
    /**
     * Read Lisp data from some underlying input stream.
     *
     * @return The Lisp data as a Java object.
     */
    public abstract Object read();
    public abstract int next();
    public abstract void unread(int c);

    public abstract String nextToken();
    public abstract String nextToken(int codepoint);

    public abstract Expr finishToken(String tokenFragment);

    public abstract boolean isBoundToMacro(int codepoint);
}
