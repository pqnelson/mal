package com.github.pqnelson.reader_macro;

import java.io.Reader;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.function.Function;

import com.github.pqnelson.ReadTable;
import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.Symbol;


/**
 * Collect values read until a delimiting token is encountered, then
 * return the collected values as an {@code ArrayList}.
 */
public class AccumulatorReaderMacro implements ReaderMacro {
    /**
     * The "needle" we're looking for.
     */
    private final Symbol stopToken;

    private final Function<List<Expr>, Expr> reduce;

    /**
     * Construct an accumulator reader macro, using the specific
     * character as a standalone stopping token.
     *
     * <p>Assumes the character is already recognized by the Lisp Reader
     * as a one-character token.</p>
     *
     * @param character The single-char stopping delimiter.
     */
    public AccumulatorReaderMacro(final char character,
                                  final Function<List<Expr>, Expr> f) {
        this(new Symbol("" + character), f);
    }

    /**
     * Construct an accumulator reader macro, using the specific
     * stopping token is encountered.
     *
     * @param delimiter The "needle" in the haystack.
     */
    public AccumulatorReaderMacro(final String delimiter,
                                  final Function<List<Expr>, Expr> f) {
        this(new Symbol(delimiter), f);
    }

    /**
     * Construct an accumulator reader macro, using the specific
     * stopping token is encountered.
     *
     * @param delimiter The "needle" in the haystack.
     */
    public AccumulatorReaderMacro(final Symbol delimiter,
                                  final Function<List<Expr>, Expr> f) {
        this.stopToken = delimiter;
        this.reduce = f;
    }

    /**
     * Accumulate a collection of values until the stopping token is
     * read, then return the {@code List} of values.
     *
     * @param stream The underlying input stream.
     * @param table The Lisp Reader invoking {@code this} reader macro.
     * @param cp The codepoint for the character binding to this reader macro.
     * @return {@code null} if the table is finished, otherwise it returns
     *         an {@code ArrayList} of values from {@code table.read()}.
     */
    @Override
    public Expr apply(final Reader stream, final ReadTable table, final int cp) {
        if (table.isFinished()) {
            return null;
        }

        List<Expr> coll = new ArrayList<>();
        Expr entry;
        while (!table.isFinished()) {
            try {
                entry = table.read();
                if (this.stopToken.equals(entry)) {
                    break;
                } else {
                    coll.add(entry);
                }
            } catch (InputMismatchException e) {
                if (e.getMessage().equals(this.stopToken.name())) {
                    break;
                } else {
                    throw e;
                }
            }
        }

        return this.reduce.apply(coll);
    }
}