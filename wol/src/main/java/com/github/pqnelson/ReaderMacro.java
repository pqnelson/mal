package com.github.pqnelson;

import java.io.Reader;

/**
 * A functional interface for reader macros.
 *
 * <p>Reader macros are called by a Lisp reader while parsing an input
 * stream. An example of a reader macro would be the familiar one
 * invoked upon reading {@code '('}, which then assembles a list of
 * objects until the closing delimiter {@code ')'} is encountered.</p>
 */
interface ReaderMacro<R> {
    /**
     * Execute a reader macro, consuming characters from the input
     * stream, optionally returning an object.
     *
     * @param stream The input stream used by the reader macro.
     * @param reader The Reader instance which is calling the reader macro.
     * @return A (possibly null) object read from the input stream.
     */
    Object read(Reader stream, R reader);
}