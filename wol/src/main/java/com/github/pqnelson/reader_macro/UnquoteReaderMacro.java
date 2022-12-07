package com.github.pqnelson.reader_macro;

import java.io.Reader;

import com.github.pqnelson.ReadTable;
import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Symbol;

/**
 * Parse a token as either an {@code unquote} or {@code splice}.
 */
public class UnquoteReaderMacro implements ReaderMacro {
    public UnquoteReaderMacro() { }
    /**
     * Parses the next token as either an unquote, or a splice, special form.
     *
     * @param stream The input stream used by the reader macro.
     * @param reader The Reader instance which is calling the reader macro.
     * @return A (possibly null) object read from the input stream.
     */
    @Override
    public Expr apply(final Reader stream, final ReadTable reader, final int cp) {
        if (reader.isFinished()) return null;
        int peek;
        try {
            peek = stream.read();
        } catch (Exception e) {
            peek = -1;
        }
        Seq result = new Seq();
        if ('@' == peek) {
            result.conj(Symbol.SPLICE);
        } else {
            result.conj(Symbol.UNQUOTE);
        }
        result.conj((Expr) reader.read());
        return result;
    }
}