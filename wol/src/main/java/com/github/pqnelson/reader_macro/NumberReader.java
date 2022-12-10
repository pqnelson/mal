package com.github.pqnelson.reader_macro;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

import java.math.BigInteger;

import java.util.InputMismatchException;

import java.util.function.IntSupplier;
import java.util.function.IntPredicate;

import com.github.pqnelson.AbstractReader;

import com.github.pqnelson.annotations.VisibleForTesting;
import com.github.pqnelson.expr.BigInt;
import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.Int;
import com.github.pqnelson.expr.Float;

public class NumberReader extends AbstractReader {
    private final PushbackReader input;
    private final StringBuffer currentLexeme;
    private boolean finished;
    private final AbstractReader caller;

    public NumberReader(PushbackReader stream, AbstractReader lispReader, int codepoint) {
        assert (('0' <= codepoint && codepoint <= '9')
                || ('-' == codepoint) || ('+' == codepoint));

        this.input = stream;
        this.currentLexeme = new StringBuffer();
        this.currentLexeme.appendCodePoint(codepoint);
        this.finished = false;
        this.caller = lispReader;
    }

    /**
     * Next character in the input stream.
     *
     * @return The code point for the next character in the input stream,
     * and {@code -1} if there's nothing left to read.
     */
    private int next() {
        if (this.finished || this.caller.isFinished()) return -1;
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

    private int peek() {
        final int result = this.next();
        this.unread(result);
        return result;
    }

    private int peekNext() {
        final int top = this.next();
        final int result = this.peek();
        this.unread(top);
        return result;
    }

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

    @Override
    public Expr read() {
        return this.number();
    }

    /**
     * Tokenize a number.
     *
     * <p>A number can look like, using the following BNF-ish grammar:</p>
     * <blockquote>
     * <dl>
     * <dt><i>FloatValue:</i>
     * <dd><i>Sign<sub>opt</sub></i> {@code [0-9]+ . [0-9]+}
     * <dd><i>Sign<sub>opt</sub></i> {@code [0-9]+ [eE] [e-9]+}
     * <dd><i>Sign<sub>opt</sub></i> {@code [0-9]+ . [0-9]+ [eE] [e-9]+}
     * </dl>
     * <dl>
     * <dt><i>BinaryLiteral:</i>
     * <dd><i>Sign<sub>opt</sub></i> {@code 0 [bB] [01]+}
     * <dt><i>OctalLiteral:</i>
     * <dd><i>Sign<sub>opt</sub></i> {@code 0 [0-7]+}
     * <dd><i>Sign<sub>opt</sub></i> {@code 0 [oO] [0-7]+}
     * <dt><i>HexadecimalLiteral:</i>
     * <dd><i>Sign<sub>opt</sub></i> {@code 0 [xX] [0-9a-fA-F]+}
     * <dt><i>SignedInteger</i>
     * <dd><i>Sign<sub>opt</sub></i> {@code 0}
     * <dd><i>Sign<sub>opt</sub></i> {@code [1-9][0-9]+}
     * <dt><i>NumberInteger</i>
     * <dd><i>BinaryLiteral</i>
     * <dd><i>OctalLiteral</i>
     * <dd><i>HexadecimalLiteral</i>
     * <dd><i>SignedInteger</i>
     * <dt><i>BigInteger</i>
     * <dd><i>NumberInteger</i> {@code n}
     * <dt><i>NumericLiteral</i>
     * <dd><i>NumberInteger</i>
     * <dd><i>BigInteger</i>
     * <dd><i>Float</i>
     * </dl>
     * </blockquote>
     * <p>Where <i>Sign<sub>opt</sub></i> is an optional {@code +} or {@code -}.
     * A more readily consumable version of these statements:</p>
     * <pre>
     *   sign?       ::= '[+-]?'
     *   digits      ::= '[0-9]+'
     *   binary      ::= '0[bB][01]+'
     *   octal       ::= '0[oO]?[0-7]+'
     *   hexadecimal ::= '0[xX][0-9a-fA-F]+'
     *   integer  ::= digits
     *             |  binary
     *             |  octal
     *             |  hexadecimal
     *   bigint   ::= integer 'n'    --- an integer literal suffixed by 'n'
     *   exponent ::= '[eE]' sign? digits
     *   float  ::= digits '.' digits
     *           |  digits '.' digits exponent
     *           |  digits exponent
     *   number ::= sign? integer
     *           |  sign? bigint
     *           |  sign? float
     * </pre>
     * <p>The default assumption is to treat a number as if it were floating
     * point.</p>
     */
    @VisibleForTesting
    Expr number() {
        char peek = currentLexeme.charAt(0);
        char peekNext = (char) peek();
        if ('+' == peek || '-' == peek) {
            currentLexeme.appendCodePoint(next());
            peek = peekNext;
            peekNext = (char) peekNext();
        }
        try {
            if ('0' == peek && '\0' != peekNext) {
                switch (peekNext) {
                case 'b':
                case 'B':
                    return radixNumber(2, 'b', 'B');
                case 'x':
                case 'X':
                    return radixNumber(16, 'x', 'X');
                case 'o':
                case 'O':
                    return radixNumber(8, 'o', 'O');
                default:
                    return tryScanningOctal();
                }
            }
            return floatingPointNumber();
        } catch (InputMismatchException e) {
            return this.finishToken(this.currentLexeme.toString());
        }
    }

    /**
     * Javascript interprets all numbers as double precision. ES6 is starting
     * to change that. Should we follow pre-ES6 Javascript and parse numbers
     * as double-precision floats? ({@code true} is "yes, follow old-school JS")
     */
    public boolean preferParsingNumbersAsFloats = true;

    /**
     * Tokenize a number as a double.
     *
     * <p>We first try to tokenize the Mantissa, with {@code floatMantissa()}
     * then check if there's an exponent part with {@code floatExponent()}.</p>
     *
     * <p>If it turns out this is an integer, then we parse the lexeme as an
     * integer token <em>unless</em> the {@code preferParsingNumberAsFloat}
     * flag has been set to {@code true}.</p>
     */
    final Expr floatingPointNumber() {
        boolean mustParseAsFloat = floatMantissa();
        mustParseAsFloat = floatExponent() || mustParseAsFloat;
        if (mustParseAsFloat || preferParsingNumbersAsFloats) {
            System.out.println("Current lexeme: "+currentLexeme.toString());
            return new Float(Double.parseDouble(currentLexeme.toString()));
        } else {
            return new Int(Long.parseLong(currentLexeme.toString()));
        }
    }

    private void checkForValidBreak() {
        if (Character.isWhitespace(peek())
            || this.caller.isBoundToMacro(peek())
            || isFinished()
            || caller.isFinished()) {
            return;
        }
        throw new InputMismatchException();
    }

    final boolean floatMantissa() {
        while (Character.isDigit(peek())) {
            currentLexeme.appendCodePoint(next());
        }

        if (('.' == peek()) && Character.isDigit(peekNext())) {
            currentLexeme.appendCodePoint(next());
            while (Character.isDigit(peek())) {
                currentLexeme.appendCodePoint(next());
            }
            return true;
        }
        return false;
    }

    final boolean isFloatExponent() {
        return ((('e' == peek()) || ('E' == peek()))
                && (('+' == peekNext())
                    || ('-' == peekNext())
                    || Character.isDigit(peekNext()))
               );
    }

    final boolean floatExponent() {
        boolean tokenIsFloat = false;
        if (isFloatExponent()) {
            currentLexeme.appendCodePoint(next());
            if (Character.isDigit(peek()) || Character.isDigit(peekNext())) {
                currentLexeme.appendCodePoint(next());
            } else {
                // treat "12examples" as an error
                // treat "12.34ENOUGH_SCREAM_CASE" as an identifier?
                throw new InputMismatchException();
            }
            tokenIsFloat = true;
        }

        while (Character.isDigit(peek())) {
            currentLexeme.appendCodePoint(next());
        }
        checkForValidBreak();
        return tokenIsFloat;
    }

    /**
     * Javascript indicates a BigInt by an {@code n} suffix.
     */
    private boolean isJavascriptBigintSuffix() {
        return 'n' == peek();
    }

    /**
     * Tokenize a number '0[bBoOxX]\d+[n]?'.
     *
     * If successful, this pushes a new token. If fails, defer to the
     * {@code error()} reporting, possibly throwing an exception.
     *
     * BigIntegers have a suffix 'n'. This is Javascript conventions.
     *
     * @param base The radix of the number.
     * @param b The lowercase character of the radix separator.
     * @param B The uppercase character of the radix separator.
     */
    private Expr radixNumber(final int base, final char b, final char B) {
        assembleRadixNumberLexeme(base, b, B);
        return tokenizeRadixNumberLexeme(base, b, B);
    }

    // Produce a predicate testing if a given character's codePoint is between
    // 0 and (whatever the number of digits in the radix is).
    final IntPredicate radixBoundsFactory(final int base,
                                          final char b,
                                          final char B) {
        final int upperBound = Math.min('9', '0' + base);
        final int lcLetterLowerBound = (base > 10 ? 'a' : '0');
        final int lcLetterUpperBound = (base > 10 ? ((base - 10) + 'a') : '0');
        // upper bounds = '0' means we're "turning off" alphabetic digits;
        // i.e., we're not in hexadecimal.
        final int ucLetterLowerBound = (base > 10 ? 'A' : '0');
        final int ucLetterUpperBound = (base > 10 ? ((base - 10) + 'A') : '0');
        if (base < 10) {
            IntPredicate withinBounds = (c) -> ('0' <= c && c < upperBound);
            return withinBounds;
        }
        IntPredicate withinBounds = (c) -> (('0' <= c && c <= '9')
            || ((lcLetterLowerBound <= c && c <= lcLetterUpperBound)
                || (ucLetterLowerBound <= c && c <= ucLetterUpperBound)));
        return withinBounds;
    }

    @VisibleForTesting
    final Expr tryScanningOctal() {
        // build predicate to test if we're still in the right radix
        IntPredicate withinBounds = radixBoundsFactory(8, 'o', 'O');
        // CONSUME!
        do {
            if ('.' == peek()) {
                return floatingPointNumber();
            } else if (Character.isDigit(peek()) &&
                       !withinBounds.test((int) peek())) {
                // whoops, it currently looks like an integer
                return tryScanningInt();
            } else if (Character.isDigit(peek()) &&
                       withinBounds.test((int) peek())) {
                currentLexeme.appendCodePoint(next());
            } else {
                throw new InputMismatchException();
            }
        }  while (Character.isDigit(peek()));
        return tokenizeRadixNumberLexeme(8, 'o', 'O');
    }

    final Expr tryScanningInt() {
        while (Character.isDigit(peek())) {
            currentLexeme.appendCodePoint(next());
            if (isFloatExponent()) {
                if (floatExponent()) {
                    return new Float(Double.parseDouble(currentLexeme.toString()));
                } else {
                    return new Int(Long.parseLong(currentLexeme.toString()));
                }
            }
        }
        checkForValidBreak();
        return tokenizeRadixNumberLexeme(10, 'd', 'D');
    }

    final void assembleRadixNumberLexeme(final int base,
                                         final char b,
                                         final char B) {
        assert ('0' == currentLexeme.charAt(0));
        if ((b == peek()) || (B == peek())) {
            // octal formats make this optional :(
            currentLexeme.appendCodePoint(next());
        }
        // build predicate to test if we're still in the right radix
        IntPredicate withinBounds = radixBoundsFactory(base, b, B);
        // CONSUME!
        while (withinBounds.test((int) peek())) {
            currentLexeme.appendCodePoint(next());
        }
        checkForValidBreak();
    }

    final Expr tokenizeRadixNumberLexeme(final int base,
                                         final char b,
                                         final char B) {
        final int sign = ('-' == currentLexeme.charAt(0) ? -1 : 1);
        final int signOffset = ('-' == currentLexeme.charAt(0)
                                || '+' == currentLexeme.charAt(0))
            ? 1 : 0;
        // check if it could possibly be a single digit number
        if (1 == currentLexeme.length()) {
            final Long literal
                = Long.parseLong(currentLexeme.substring(signOffset), base);
            return new Int(literal);
        }
        // no? It must be a radix number
        final char radixSpec = currentLexeme.charAt(1+signOffset);
        final int start = ((b == radixSpec || B == radixSpec) ? 2 : 1)
            + signOffset;
        if (isJavascriptBigintSuffix()) {
            currentLexeme.appendCodePoint(next());
            final String lexeme = currentLexeme
                .substring(start, currentLexeme.length() - 1);
            final BigInteger literal
                = new BigInteger(lexeme, base);
            return new BigInt(-1 == sign ? literal.negate() : literal);
        } else {
            Long literal = Long.parseLong(currentLexeme.substring(start), base);
            return new Int(literal);
        }
    }

    @Override
    public Expr finishToken(final String tokenFragment) {
        return this.caller.finishToken(tokenFragment);
    }

    @Override
    public boolean isBoundToMacro(int cp) {
        return false;
    }

    @Override
    public final String nextToken() {
        return caller.nextToken();
    }

    @Override
    public final String nextToken(int cp) {
        return caller.nextToken(cp);
    }

}
