package com.github.pqnelson.reader_macro;

import java.io.Reader;

import com.github.pqnelson.ReadTable;
import com.github.pqnelson.expr.Symbol;

/**
 * Register a single character as a self-contained token.
 */
public class SingleCharReaderMacro implements ReaderMacro {
    /**
     * Speak of the devil, and up he shall arise.
     */
    protected final Symbol token;

    protected SingleCharReaderMacro(final char character) {
        this.token = new Symbol("" + character);
    }

    /**
     * Register the character as a self-contained token.
     *
     * <p><b>Warning:</b> this will also bind the character to a new
     * {@code SingleCharReaderMacro} instance.</p>
     *
     * @param character The needle in the haystack.
     * @param reader The ReadTable which will add the reader macro.
     */
    public static void register(final char character, final ReadTable reader) {
        reader.addMacro(character, new SingleCharReaderMacro(character));
    }
    /**
     * Returns the token when encountered.
     *
     * @param stream The underlying input stream.
     * @param table The Lisp Reader invoking {@code this} reader macro.
     * @return The character being treated as a token.
     */
    @Override
    public Symbol apply(final Reader stream, final ReadTable table, final int cp) {
        return this.token;
    }
}