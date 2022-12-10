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
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Str;
import com.github.pqnelson.expr.Symbol;
import com.github.pqnelson.expr.Vector;
import com.github.pqnelson.reader_macro.AccumulatorReaderMacro;
import com.github.pqnelson.reader_macro.ClojureCharReaderMacro;
import com.github.pqnelson.reader_macro.DelimiterReaderMacro;
import com.github.pqnelson.reader_macro.KeywordReaderMacro;
import com.github.pqnelson.reader_macro.NumberReader;
import com.github.pqnelson.reader_macro.ReaderMacro;
import com.github.pqnelson.reader_macro.SingleCharReaderMacro;
import com.github.pqnelson.reader_macro.SpecialFormReaderMacro;
import com.github.pqnelson.reader_macro.UnquoteReaderMacro;

/*
 * TODO 1: have a reader macro to handle strings.
 * TODO 2: unit test reading numbers further
 */
/**
 * A read-table driven Lisp reader.
 *
 * <p>This is an attempt to cleanup the Scanner/Reader mess.</p>
 *
 * <p>The reader will accumulate characters until it encounters either
 * whitespace or a character triggering a reader macro. "Reader macros"
 * are precisely instances of {@link ReaderMacro ReaderMacro}.</p>
 */
public class ReadTable extends AbstractReader {
    /**
     * Mapping of character [code points] to reader macros.
     */
    private Map<Integer, ReaderMacro> table;

    private static final Map<String, Expr> literals;
    static {
        literals = new HashMap<String, Expr>();
        literals.put("false", Literal.F);
        literals.put("nil", Literal.NIL);
        literals.put("true", Literal.T);
        literals.put("try", Symbol.TRY);
        literals.put("catch", Symbol.CATCH);
        literals.put("macroexpand", Symbol.MACROEXPAND);
        literals.put("quasiquote-expand", Symbol.QUASIQUOTE_EXPAND);
    }

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
    private final ReaderMacro newlineReader = (s, r, cp) -> {
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
        this.table = new HashMap<Integer, ReaderMacro>();
        this.input = new PushbackReader(new BufferedReader(reader), 32);
        this.initializeMacros();
    }

    private void initializeMacros() {
        this.table.put((int) '\n', newlineReader);
        addMacro('\\', new ClojureCharReaderMacro());
        /* special forms */
        addMacro('\'', new SpecialFormReaderMacro('\'', Symbol.QUOTE));
        addMacro('`', new SpecialFormReaderMacro('`', Symbol.QUASIQUOTE));
        addMacro('~', new UnquoteReaderMacro());
        addMacro(':', new KeywordReaderMacro());
        /* parsing collections */
        addMacro('(', new AccumulatorReaderMacro(")", Seq::new));
        DelimiterReaderMacro.register(')', this);

        addMacro('[', new AccumulatorReaderMacro("]", Vector::new));
        DelimiterReaderMacro.register(']', this);

        addMacro('{', new AccumulatorReaderMacro("}", (List<Expr> coll) -> {
                    try {
                        return Core.hash_map(new Seq(coll));
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
        }));
        DelimiterReaderMacro.register('}', this);
    }

    @Override
    public boolean isBoundToMacro(int codepoint) {
        return this.table.containsKey(codepoint);
    }

    public final int getLineNumber() {
        return this.line;
    }

    public void addMacro(char c, ReaderMacro macro) {
        this.addMacro((int) c, macro);
    }

    public void addMacro(int codepoint, ReaderMacro macro) {
        this.table.put(codepoint, macro);
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
    @Override
    public boolean isFinished() {
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

    Expr asLiteralOrSymbol(String token) {
        if (ReadTable.literals.containsKey(token)) {
            return ReadTable.literals.get(token);
        }
        return new Symbol(token);
    }

    private int peek() {
        final int result = next();
        unread(result);
        return result;
    }

    private boolean isNumber(char c) {
        return Character.isDigit(c) ||
            (('-' == c || '+' == c) && Character.isDigit(peek()));
    }

    private boolean isNumber(int cp) {
        return Character.isDigit(cp) ||
            (('-' == cp || '+' == cp) && Character.isDigit(peek()));
    }

    private Expr number(int cp) {
        NumberReader reader = new NumberReader(this.input, this, cp);
        return reader.read();
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
    @Override
    public final Expr read() {
        Expr result = null;
        while (true) {
            if (isFinished()) {
                return result;
            }
            int c = next();
            if (table.containsKey(c)) {
                result = table.get(c).apply(this.input, this, c);
                if (null != result) {
                    return result;
                }
            } else if (Character.isWhitespace(c)) {
                continue;
            } else if (isNumber(c)) {
                return number(c);
            } else {
                return readToken(c);
            }
        }
    }

    @Override
    public final String nextToken() {
        while (true) {
            if (isFinished()) {
                return "";
            }
            int c = next();
            if (table.containsKey(c)) {
                return Character.toString(c);
            } else if (Character.isWhitespace(c)) {
                continue;
            } else {
                return this.nextToken(c);
            }
        }
    }

    @Override
    public final String nextToken(int cp) {
        StringBuffer buf = new StringBuffer();
        buf.appendCodePoint(cp);
        while (!isFinished()) {
            int c = next();
            if (this.table.containsKey(c)) {
                unread(c);
                break;
            } else if (Character.isWhitespace(c)) {
                break;
            } else {
                buf.appendCodePoint(c);
            }
        }
        return buf.toString();
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
    private Expr readToken(final int cp) {
        return this.finishToken(Character.toString(cp));
    }

    @Override
    public Expr finishToken(final String tokenFragment) {
        StringBuffer buf = new StringBuffer(tokenFragment);
        while (!isFinished()) {
            int c = next();
            if (this.table.containsKey(c)) {
                unread(c);
                return asLiteralOrSymbol(buf.toString());
            } else if (Character.isWhitespace(c)) {
                return asLiteralOrSymbol(buf.toString());
            } else {
                buf.appendCodePoint(c);
            }
        }
        return asLiteralOrSymbol(buf.toString());
    }
}