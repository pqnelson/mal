

package com.github.pqnelson.reader_macro;

import java.io.Reader;
import java.util.InputMismatchException;

import com.github.pqnelson.ReadTable;
import com.github.pqnelson.expr.Symbol;

/**
 * Register a single character as a right-delimiting token. If encountered without
 * first finding the left-delimiter, a {@code InputMismatchException} will be thrown.
 */
public class DelimiterReaderMacro extends SingleCharReaderMacro {

    private DelimiterReaderMacro(final char c) {
        super(c);
    }

    public static void register(final char character, final ReadTable reader) {
        reader.addMacro(character, new DelimiterReaderMacro(character));
    }

    @Override
    public Symbol apply(final Reader stream, final ReadTable table, final int cp) {
        throw new InputMismatchException(super.token.name());
    }
}