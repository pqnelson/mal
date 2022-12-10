package com.github.pqnelson.reader_macro;

import java.io.Reader;
import java.util.ArrayList;

import com.github.pqnelson.ReadTable;
import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Symbol;


/**
 * Macro for special forms like quotes {@literal 'expr}, backticks
 * {@literal `expr}, unquotes {@literal ~expr}, and splice {@literal ~@expr}.
 */
public class SpecialFormReaderMacro implements ReaderMacro {
    /**
     * The "needle" we're looking for.
     */
    private final String stopToken;

    private final Symbol form;

    /**
     * Construct an special form reader macro, using the specific
     * special form.
     *
     * <p>Assumes the character is already recognized by the Lisp Reader
     * as a one-character token.</p>
     *
     * @param character The single-char stopping delimiter.
     * @param form The special form.
     */
    public SpecialFormReaderMacro(final char character, final Symbol form) {
        this("" + character, form);
    }

    /**
     * Construct a special form.
     *
     * @param delimiter The "needle" in the haystack.
     * @param form The special form.
     */
    public SpecialFormReaderMacro(final String delimiter, final Symbol form) {
        this.stopToken = delimiter;
        this.form = form;
    }

    /**
     * Produces a special form using the next symbol.
     *
     * @param stream The underlying input stream.
     * @param table The Lisp Reader invoking {@code this} reader macro.
     * @param cp The codepoint for the character bound to the reader macro.
     * @return {@code null} if the table is finished, otherwise it returns
     *         an {@code ArrayList} of values from {@code table.read()}.
     */
    @Override
    public Expr apply(final Reader stream, final ReadTable table, final int cp) {
        if (table.isFinished()) {
            return null;
        }

        Seq result = new Seq();
        result.conj(this.form);
        result.conj(table.read());
        return result;
    }
}