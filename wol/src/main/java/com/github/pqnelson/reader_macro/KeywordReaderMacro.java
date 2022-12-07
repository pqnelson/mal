package com.github.pqnelson.reader_macro;

import java.io.Reader;

import com.github.pqnelson.ReadTable;
import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Str;
import com.github.pqnelson.expr.Symbol;

/**
 * Parse a token as a keyword.
 */
public class KeywordReaderMacro implements ReaderMacro {
    public KeywordReaderMacro() { }
    /**
     * Parses the next token as a keyword.
     *
     * @param stream The input stream used by the reader macro.
     * @param reader The Reader instance which is calling the reader macro.
     * @return A (possibly null) object read from the input stream.
     */
    @Override
    public Keyword apply(final Reader stream, final ReadTable reader, final int cp) {
        Expr expr = reader.read();
        if (expr.isSymbol()) {
            return new Keyword(((Symbol) expr).name());
        } else if (expr.isString()) {
            return new Keyword(((Str) expr).value());
        }
        throw new RuntimeException("keyword cannot be formed from: "+expr.toString());
    }
}