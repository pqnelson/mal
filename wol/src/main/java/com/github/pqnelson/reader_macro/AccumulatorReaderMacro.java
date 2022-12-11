package com.github.pqnelson.reader_macro;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.function.Function;

import com.github.pqnelson.ReadTable;
import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.Int;
import com.github.pqnelson.expr.IObj;
import com.github.pqnelson.expr.Map;
import com.github.pqnelson.expr.Str;
import com.github.pqnelson.expr.Symbol;


/**
 * Collect values read until a delimiting token is encountered, then
 * return the collected values as an {@code ArrayList}.
 */
public class AccumulatorReaderMacro<E extends Expr & IObj<E>>
        implements ReaderMacro {
    /**
     * The "needle" we're looking for.
     */
    private final Symbol stopToken;

    private final Function<List<Expr>, ? extends E> reduce;

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
                                  final Function<List<Expr>, ? extends E> f) {
        this(new Symbol("" + character), f);
    }

    /**
     * Construct an accumulator reader macro, using the specific
     * stopping token is encountered.
     *
     * @param delimiter The "needle" in the haystack.
     */
    public AccumulatorReaderMacro(final String delimiter,
                                  final Function<List<Expr>, ? extends E> f) {
        this(new Symbol(delimiter), f);
    }

    /**
     * Construct an accumulator reader macro, using the specific
     * stopping token is encountered.
     *
     * @param delimiter The "needle" in the haystack.
     */
    public AccumulatorReaderMacro(final Symbol delimiter,
                                  final Function<List<Expr>, ? extends E> f) {
        this.stopToken = delimiter;
        this.reduce = f;
    }

    private Map metadata(int line, int offset) {
        HashMap<Expr, Expr> meta = new HashMap<>();
        meta.put(new Str("line"), new Int(line));
        meta.put(new Str("offset"), new Int(offset));
        Map result = new Map(meta);
        assert (null != result);
        return result;
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
    public E apply(final Reader stream, final ReadTable table, final int cp) {
        if (table.isFinished()) {
            return null;
        }
        int line = table.getLineNumber();
        int offset = table.getOffset();
        List<Expr> coll = new ArrayList<>();
        Expr entry;
        while (!table.isFinished()) {
            try {
                entry = table.read();
                if (this.stopToken.equals(entry)) {
                    Map metadata = metadata(line, offset);
                    E result = this.reduce.apply(coll).withMeta(metadata);
                    return result;
                } else {
                    coll.add(entry);
                }
            } catch (InputMismatchException e) {
                if (e.getMessage().equals(this.stopToken.name())) {
                    Map metadata = metadata(line, offset);
                    E result = this.reduce.apply(coll).withMeta(metadata);
                    return result;
                } else {
                    throw e;
                }
            }
        }
        throw new InputMismatchException("Line ["
                                         + Integer.toString(line) + ","
                                         + Integer.toString(offset)
                                         + "]: Runaway collection started, "
                                         + "starting delimiter "
                                         + Character.toString(cp));
    }
}
