package com.github.pqnelson;

import java.io.BufferedReader;
import java.io.PushbackReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A read-table driven Lisp reader.
 *
 * <p>This is an attempt to cleanup the Scanner/Reader mess.</p>
 *
 * <p>The reader will accumulate characters until it encounters either
 * whitespace or a character triggering a reader macro. "Reader macros"
 * are precisely instances of {@link ReaderMacro ReaderMacro}.</p>
 */
public class ReadTable {
    /**
     * Mapping of character [code points] to reader macros.
     */
    private Map<Integer, ReaderMacro<ReadTable>> table;
    /**
     * The underlying input reader source.
     */
    private final PushbackReader input;
    /**
     * The line number, for debugging information.
     */
    private int line = 1;
    /**
     * Have we finished processing all characters in our input stream?
     */
    private boolean finished = false;

    /**
     * Example of a reader macro: increment the {@code line} variable
     * but return nothing.
     */
    private final ReaderMacro<ReadTable> newlineReader = (s, r) -> {
        line++;
        return null;
    };

    public ReadTable(final String snippet) {
        this(new StringReader("" + snippet));
        // If snippet is null, then StringReader(null) throws an error,
        // but StringReader(""+null) works as we'd expect/hope.
    }

    public ReadTable(final InputStream stream) {
        this(new InputStreamReader(stream));
    }

    public ReadTable(final Reader reader) {
        this.table = new HashMap<Integer, ReaderMacro<ReadTable>>();
        this.input = new PushbackReader(new BufferedReader(reader));

        this.table.put((int) '(', delimitedReaderFactory(")"));
        this.table.put((int) ')', singleCharReader((int) ')'));
        this.table.put((int) '\n', newlineReader);
    }

    /**
     * Accumulate all objects read in until the token is the specific
     * right delimiter.
     *
     * @param until The right delimiter as a string.
     * @return A {@code ReaderMacro} which accumulates all objects read
     * in until the token matches the specific right delimiter.
     */
    private ReaderMacro<ReadTable> delimitedReaderFactory(final String until) {
        final ReaderMacro<ReadTable> result = (s, rdr) -> {
            ArrayList<Object> coll = new ArrayList<Object>();
            Object token = rdr.read();
            while (!token.equals(until)) {
                coll.add(token);
                token = rdr.read();
            }
            return coll;
        };
        return result;
    }

    /**
     * Treat a given character as a single-character token.
     *
     * <p>For example, {@code ")"} is a token to signal the end of the
     * list. So we need a reader which will emit this token.</p>
     *
     * @param c The code point for the character we're identifying as a
     * singleton token.
     * @return A reader macro for the operation.
     */
    private ReaderMacro<ReadTable> singleCharReader(final int c) {
        StringBuffer buf = new StringBuffer();
        buf.appendCodePoint(c);
        final String token = buf.toString();
        final ReaderMacro<ReadTable> result = (s, r) -> token;
        return result;
    }

    /**
     * Next character in the input stream.
     *
     * @return The code point for the next character in the input stream,
     * and {@code -1} if there's nothing left to read.
     */
    private int next() {
        try {
            return input.read();
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * Put the specific code point back into the input stream.
     *
     * @param c The code point for the character.
     */
    private void unread(final int c) {
        try {
            input.unread(c);
        } catch (IOException e) {
        }
    }

    /**
     * Is the underlying input stream ended?
     *
     * @return Returns true if there is nothing more from the input
     * stream to read.
     */
    private boolean isAtEnd() {
        if (!finished) {
            final int c = next();
            if (-1 == c) {
                finished = true;
            } else {
                unread(c);
            }
        }
        return finished;
    }

    private void pass() {
    }

    /**
     * Read the next object encoded in the reader's input stream.
     *
     * <p>By default, all whitespace is skipped. But this can be
     * overridden by reader macros if you want to, e.g., count line
     * numbers.</p>
     *
     * <p>When encountering a character which is neither bound to a
     * reader macro nor whitespace, the reader will start constructing a
     * token. The characters will accumulate into a token until either
     * whitespace or a character bound to a reader-macro is encountered;
     * at that point, the token will be interpreted as Lisp data and
     * returned.</p>
     *
     * @return The object encoded in the stream.
     */
    public final Object read() {
        Object result = null;
        while (true) {
            if (isAtEnd()) {
                return result;
            }
            int c = next();
            if (table.containsKey(c)) {
                result = table.get(c).read(this.input, this);
                if (null != result) {
                    return result;
                }
            } else if (Character.isWhitespace(c)) {
                pass(); // skip whitespace
            } else {
                return readToken(c);
            }
        }
    }

    /**
     * Accumulate characters into a token.
     *
     * <p>When the reader encounters a character bound to a reader
     * macro, or whitespace, then we finish constructing the token.</p>
     *
     * @param cp The initial character's code point for the new token.
     * @return The object encoded by the token.
     */
    private Object readToken(final int cp) {
        StringBuffer buf = new StringBuffer();
        buf.appendCodePoint(cp);
        while (!isAtEnd()) {
            int c = next();
            if (this.table.containsKey(c)) {
                unread(c);
                return buf.toString();
            } else if (Character.isWhitespace(c)) {
                return buf.toString();
            } else {
                buf.appendCodePoint(c);
            }
        }
        return buf.toString();
    }
}