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
import com.github.pqnelson.reader_macro.StringReaderMacro;
import com.github.pqnelson.reader_macro.UnquoteReaderMacro;

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
    public boolean preferParsingNumbersAsFloats = true;
    /**
     * Mapping of character [code points] to reader macros.
     */
    private Map<Integer, ReaderMacro> table;

    //private
    private static final Map<String, Expr> literals;
    static {
        literals = new HashMap<String, Expr>();
        literals.put("catch", Symbol.CATCH);
        literals.put("def", Symbol.DEF);
        literals.put("defmacro", Symbol.DEFMACRO);
        literals.put("do", Symbol.DO);
        literals.put("false", Literal.F);
        literals.put("fn*", Symbol.FN_STAR);
        literals.put("if", Symbol.IF);
        literals.put("let*", Symbol.LET_STAR);
        literals.put("macroexpand", Symbol.MACROEXPAND);
        literals.put("nil", Literal.NIL);
        literals.put("quote", Symbol.QUOTE);
        literals.put("quasiquote", Symbol.QUASIQUOTE);
        literals.put("quasiquote-expand", Symbol.QUASIQUOTE_EXPAND);
        literals.put("splice", Symbol.SPLICE);
        literals.put("true", Literal.T);
        literals.put("try", Symbol.TRY);
        literals.put("unquote", Symbol.UNQUOTE);
    }

    /**
     * The underlying input reader source.
     */
    private final PushbackReader input;
    /**
     * The line number, for debugging information.
     */
    private int line = 1;
    private int offset = 0;
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
        offset = 0;
        return null;
    };

    private final ReaderMacro commaAsWhitespaceReader = (s, r, cp) -> {
        return null;
    };

    private final ReaderMacro commentReader = (s, r, cp) -> {
        while (!isFinished() && '\n' != peek()) {
            next();
        }
        return null;
    };

    public ReadTable(final String snippet) {
        this(new StringReader("" + snippet));
        // If snippet is null, then StringReader(null) throws an error,
        // but StringReader(""+null) works as we'd expect/hope.
    }

    public ReadTable(final InputStream stream) {
        this(new BufferedReader(new InputStreamReader(stream)));
    }

    public ReadTable(final Reader reader) {
        this.table = new HashMap<Integer, ReaderMacro>();
        this.input = new PushbackReader(reader, 32);
        this.initializeMacros();
    }

    private void initializeMacros() {
        // this.table.put((int) '\n', newlineReader);
        addMacro('\n', newlineReader);
        addMacro(',', commaAsWhitespaceReader);
        addMacro(';', commentReader);
        addMacro('\\', new ClojureCharReaderMacro());
        /* special forms */
        addMacro('\'', new SpecialFormReaderMacro('\'', Symbol.QUOTE));
        addMacro('`', new SpecialFormReaderMacro('`', Symbol.QUASIQUOTE));
        addMacro('~', new UnquoteReaderMacro());
        addMacro(':', new KeywordReaderMacro());
        /* parsing collections */
        addMacro('(', new AccumulatorReaderMacro<Seq>(")", Seq::new));
        DelimiterReaderMacro.register(')', this);

        addMacro('[', new AccumulatorReaderMacro<Vector>("]", Vector::new));
        DelimiterReaderMacro.register(']', this);

        addMacro('{', new AccumulatorReaderMacro<com.github.pqnelson.expr.Map>("}", (List<Expr> coll) -> {
                    try {
                        return ((com.github.pqnelson.expr.Map) Core.hash_map(new Seq(coll)));
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
        }));
        DelimiterReaderMacro.register('}', this);

        addMacro('"', new StringReaderMacro());
    }

    @Override
    public boolean isBoundToMacro(int codepoint) {
        return this.table.containsKey(codepoint);
    }

    public final int getOffset() {
        return this.offset;
    }

    public final int getLineNumber() {
        return this.line;
    }

    public void incLineNumber() {
        this.line++;
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
    @Override
    public int next() {
        try {
            int result = input.read();
            offset++;
            return result;
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * Put the specific code point back into the input stream.
     *
     * @param c The code point for the character.
     */
    @Override
    public void unread(final int c) {
        try {
            input.unread(c);
            offset--;
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
            // Pushback reader returns 65535 sometimes
            // char is unsigned; 65535 = 0xFFFF = -1
            if (-1 == c || 65535 == c) {
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
        reader.preferParsingNumbersAsFloats = this.preferParsingNumbersAsFloats;
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
                return finishToken(Character.toString(c));
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
        return this.nextToken(Character.toString(cp));
    }

    public final String nextToken(String tokenFragment) {
        StringBuffer buf = new StringBuffer(tokenFragment);
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
     * @param tokenFragment The initial fragment of the token.
     * @return The object encoded by the token.
     */
    @Override
    public Expr finishToken(final String tokenFragment) {
        String token = this.nextToken(tokenFragment);
        return asLiteralOrSymbol(token);
    }
}
