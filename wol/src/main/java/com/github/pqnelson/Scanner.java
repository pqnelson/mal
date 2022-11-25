package com.github.pqnelson;

// Consider using java.util.InputMismatchException throwables?
// https://docs.oracle.com/en/java/javase/19/docs/api/java.base/java/util/InputMismatchException.html

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.IntSupplier;
import java.util.function.IntPredicate;

import com.github.pqnelson.annotations.VisibleForTesting;
import static com.github.pqnelson.TokenType.*;

/**
 * A simple scanner to produce {@code Token} instances.
 *
 * @see {@link https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/Scanner.java}
 */
class Scanner {
    private final List<Token> tokens = new ArrayList<Token>();
    private final BufferedReader source;
    @VisibleForTesting
    OptionalInt cachedNextChar = OptionalInt.empty();
    @VisibleForTesting
    OptionalInt cachedNextNextChar = OptionalInt.empty();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int startLine = 1;
    private final StringBuffer currentLexeme = new StringBuffer();
    private final IntSupplier nextCharFromSource = new IntSupplier() {
            @Override
            public int getAsInt() {
                try {
                    return source.read();
                } catch (IOException e) {
                    error(line, "nextCharFromSource error:"+e);
                    return -222;
                }
            }
        };
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("def", DEF);
        keywords.put("do", DO);
        keywords.put("false", FALSE);
        keywords.put("fn*", FN_STAR);
        keywords.put("if", IF);
        keywords.put("let*", LET_STAR);
        keywords.put("nil", NIL);
        keywords.put("quote", QUOTE);
        keywords.put("true", TRUE);
    }
    /**
     * Create a scanner for code contained in a string.
     */
    public Scanner(String snippet) {
        this(new StringReader((null == snippet ? "" : snippet)));
    }

    public Scanner(InputStream source) {
        this(new InputStreamReader(source));
    }

    public Scanner(Reader reader) {
        this.source = new BufferedReader(reader);
    }

    /**
     * When advancing the scanner by one character, we must adjust
     * the next character caches accordingly.
     */
    private void shiftCachedNextChars() {
        // confusing book-keeping to shuffle the cached characters correctly
        cachedNextNextChar.ifPresent(i -> {
                cachedNextChar = OptionalInt.of(i);
            });
        if (cachedNextNextChar.isPresent()) {
            cachedNextNextChar = OptionalInt.empty();
        } else {
            cachedNextChar = OptionalInt.empty();
        }
    }

    /**
     * Obtain the next character code point in the input stream.
     *
     * <p>If the cached next character is not empty, then we use that. Otherwise
     * we read the next character from the {@code source}.</p>
     *
     * <p>Two side-effects:
     * 1. The current position is incremented.
     * 2. The cached next character is set to be the cached next-next character.
     * 3. The cached next-next character is emptied.
     * </p>
     *
     * @return The code point for the next character in the input stream.
     */
    @VisibleForTesting
    int advance() {
        current++;
        int result = cachedNextChar.orElseGet(nextCharFromSource);
        shiftCachedNextChars();
        return result;
    }

    /**
     * Tests if the input source is exhausted and finished.
     *
     * <p>Side effect: populates the {@code cachedNextChar} if it is empty.
     * So, if the result is not -1, the {@code cachedNextChar} will be
     * usable.</p>
     *
     * @return True if the reader is finished, or an IOException has occurred.
     */
    @VisibleForTesting
    boolean isAtEnd() {
        try {
            if (cachedNextChar.isEmpty()) {
                cachedNextChar = OptionalInt.of(source.read());
            }
            return (-1 == cachedNextChar.getAsInt());
        } catch (IOException e) {
            return true;
        }
        // post-condition: cachedNextChar.isPresent() || isAtEnd();
    }

    @VisibleForTesting
    char peek() {
        if (isAtEnd()) return '\0';
        // post-condition for `isAtEnd`: cachedNextChar.isPresent();
        if (-1 == cachedNextChar.getAsInt()) return '\0';
        char c[] = Character.toChars(cachedNextChar.getAsInt());
        assert(1 == c.length) : "peek() has a surrogate pair?";
        return c[0];
    }

    @VisibleForTesting
    char peekNext() {
        if (isAtEnd()) return '\0';
        if (cachedNextNextChar.isEmpty()) {
            if (cachedNextChar.isEmpty()) { peek(); }
            try {
                cachedNextNextChar = OptionalInt.of(source.read());
            } catch (IOException e) {
                cachedNextNextChar = OptionalInt.of(-1);
            }
        }
        if (-1 == cachedNextNextChar.getAsInt()) return '\0';
        char c[] = Character.toChars(cachedNextNextChar.getAsInt());
        assert(1 == c.length) : "peekNext() has a surrogate pair?";
        return c[0];
    }

    // I am fairly certain this can be optimized
    private void resetCurrentLexeme() {
        currentLexeme.delete(0, currentLexeme.length());
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * Pushes current lexeme as a token onto {@code tokens}. This also resets
     * the current lexeme.
     *
     * @param type The Token type for the current lexeme, must be non-null.
     * @param literal The literal value for the current lexeme, optional argument.
     */
    private void addToken(TokenType type, Object literal) {
        String lexeme = currentLexeme.toString();
        resetCurrentLexeme();
        tokens.add(new Token(type, lexeme, literal, startLine));
    }

    // see https://github.com/openjdk/jdk/blob/590de37bd703bdae56e8b41c84f5fca5e5a00811/src/java.base/share/classes/java/lang/Character.java#L11232-L11237
    static int SEPARATORS = ((1 << Character.LINE_SEPARATOR) |
                             (1 << Character.PARAGRAPH_SEPARATOR));
    private final static int CR_CODEPOINT = 0x000d;
    private final static int LF_CODEPOINT = 0x000a;
    private final static int NEL_CODEPOINT = 0x0085;  // NEL = Next Line

    /**
     * Test for a newline, using the various system-dependent versions
     * for newlines.
     */
    @VisibleForTesting
    static boolean isNewline(int codePoint) {
        return (((SEPARATORS >> Character.getType(codePoint)) & 1) != 0) ||
            (LF_CODEPOINT <= codePoint && codePoint <= CR_CODEPOINT) ||
            (NEL_CODEPOINT == codePoint);
    }

    private final static int WHITESPACE = (1 << Character.SPACE_SEPARATOR);
    private final static int TAB_CODEPOINT = Character.codePointAt("\t", 0);

    /**
     * Test for whitespace, including tabs as "whitespace".
     */
    @VisibleForTesting
    static boolean isSpace(int codePoint) {
        return (((WHITESPACE >> Character.getType(codePoint)) & 1) != 0)
            || (TAB_CODEPOINT == codePoint);
    }

    /**
     * Tokenize the input stream.
     *
     * If successful, its last element will always be an EOF Token.
     */
    List<Token> scanTokens() {
        if (!tokens.isEmpty()) return tokens;

        while(!isAtEnd()) {
            start = current;
            startLine = line;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return Collections.unmodifiableList(tokens);
    }

    void error(int line, String message) {
        throw new InputMismatchException("Line "+line+": "+message);
        //report(line, "", message);
    }

    void report(int lne, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
    }

    void pushToken(TokenType type) {
        advance();
        addToken(type);
    }
    void pushToken(TokenType type, int lexemeLength) {
        for (int i=0; i < lexemeLength; i++) advance();
        addToken(type);
    }

    private void scanToken() {
        int cp = peek();
        // skip whitespace
        if (Scanner.isSpace(cp)) {
            advance();
            return;
        }
        if (Scanner.isNewline(cp)) {
            advance();
            line++;
            return;
        }
        // otherwise, consume the character
        // currentLexeme.appendCodePoint(cp);
        char c = Character.toChars(cp)[0];
        switch(c) {
        case ';': comment(); break;
        case '(': pushToken(LEFT_PAREN); break;
        case ')': pushToken(RIGHT_PAREN); break;
        case '[': pushToken(LEFT_BRACKET); break;
        case ']': pushToken(RIGHT_BRACKET); break;
        case '{': pushToken(LEFT_BRACE); break;
        case '}': pushToken(RIGHT_BRACE); break;
        case '\'': pushToken(QUOTE); break;
        case '`': pushToken(BACKTICK); break;
        case '^': pushToken(CARET); break;
        case '"': string(); break;
        case ':': keyword(); break;
        case '~':
            boolean splice = '@' == peekNext();
            pushToken((splice ? SPLICE : UNQUOTE), (splice ? 2 : 1));
            break;
        default:
            if ((('-' == c || '+' == c) && Character.isDigit(peekNext()))
                || Character.isDigit(c)) {
                number();
            } else if (isIdentifierLeadingChar(c)) {
                identifier();
            } else {
                error(line, "Unexpected character.");
            }
        }
    }

    // Right now, only single-line comments are supported.
    void comment() {
        assert(';' == peek());
        int cp;
        do {
            cp = advance();
        } while (!isNewline(cp) && !isAtEnd());
    }

    /**
     * Tokenize a string, removing the quotation mark delimiters.
     *
     * @TODO handle unescaping the string.
     */
    @VisibleForTesting
    void string() {
        assert ('"' == peek());
        advance();
        while ('"' != peek() && !isAtEnd()) {
            int cp = advance();
            if (isNewline(cp)) line++;
            currentLexeme.appendCodePoint(cp);
        }

        if (isAtEnd()) {
            error(line, "Unterminated string");
        }

        advance();
        addToken(STRING, currentLexeme.toString());
    }

    @VisibleForTesting
    boolean isIdentifierLeadingChar(char c) {
        return (Character.isLetter(c) || "_$*+!?<>=".indexOf(c) > -1) &&
            !Character.isIdentifierIgnorable(c);
    }

    @VisibleForTesting
    boolean isIdentifierChar(char c) {
        return (Character.isDigit(c) || "'-".indexOf(c) > -1
                || isIdentifierLeadingChar(c));
    }

    /**
     * Tokenize an identifier.
     *
     * @TODO munge names? Or handle unicode?
     * @see {@link https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Lexical_grammar#identifiers}
     */
    @VisibleForTesting
    void identifier() {
        assert (isIdentifierLeadingChar(peek()));
        while (isIdentifierChar(peek())) {
            currentLexeme.appendCodePoint(advance());
        }
        String lexeme = currentLexeme.toString();
        TokenType type = keywords.get(lexeme);
        if (type != null) addToken(type);
        else addToken(IDENTIFIER, currentLexeme.toString());
    }

    /**
     * Tokenize a keyword.
     *
     * The lexeme is the keyword name (i.e., chops off the leading colon).
     */
    @VisibleForTesting
    void keyword() {
        assert (':' == peek());
        advance();
        assert (isIdentifierLeadingChar(peek()));
        while (isIdentifierChar(peek())) {
            currentLexeme.appendCodePoint(advance());
        }
        String lexeme = currentLexeme.toString();
        addToken(KEYWORD, currentLexeme.toString());
    }

    /**
     * Tokenize a number.
     *
     * A number can look like:
     * <blockquote>{@code
     *   number ::= '0[bBxXoO]\d+'     --- written in specific radix
     *           |  '\d+\\.?\d+([eE][+-]?\d+)' --- floating point
     * }</blockquote>
     * The default assumption is to treat a number as if it were floating
     * point.
     */
    @VisibleForTesting
    void number() {
        if ('+' == peek() || '-' == peek()) currentLexeme.appendCodePoint(advance());
        if ('0' == peek()) {
            switch (peekNext()) {
            case 'b':
            case 'B':
                radixNumber(2, 'b', 'B');
                return;
            case 'x':
            case 'X':
                radixNumber(16, 'x', 'X');
                return;
            case 'o':
            case 'O':
                radixNumber(8, 'o', 'O');
                return;
            default:
                break;
            }
        }
        floatingPointNumber();
    }

    /**
     * Tokenize a number as a double.
     */
    void floatingPointNumber() {
        floatMantissa();
        floatExponent();
        addToken(NUMBER, Double.parseDouble(currentLexeme.toString()));
    }

    void floatMantissa() {
        while (Character.isDigit(peek())) {
            currentLexeme.appendCodePoint(advance());
        }

        if('.' == peek() && Character.isDigit(peekNext())) {
            currentLexeme.appendCodePoint(advance());
            while (Character.isDigit(peek())) {
                currentLexeme.appendCodePoint(advance());
            }
        }
    }

    void floatExponent() {
        if (('e' == peek() || 'E' == peek()) &&
            ('+' == peekNext() || '-' == peekNext() || Character.isDigit(peekNext()))) {
            currentLexeme.appendCodePoint(advance());
            if (Character.isDigit(peek()) || Character.isDigit(peekNext())) {
                currentLexeme.appendCodePoint(advance());
            } else {
                // treat "12examples" as an error
                // treat "12.34ENOUGH_SCREAM_CASE" as an identifier?
                error(line, "Number has invalid character");
            }
        }

        while (Character.isDigit(peek())) {
            currentLexeme.appendCodePoint(advance());
        }
    }

    /**
     * Javascript indicates a BigInt by an {@code n} suffix.
     */
    private boolean isJavascriptBigintSuffix() {
        return 'n'==peek();
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
    private void radixNumber(int base, char b, char B) {
        assembleRadixNumberLexeme(base, b, B);
        tokenizeRadixNumberLexeme(base, b, B);
    }

    // Produce a predicate testing if a given character's codePoint is between
    // 0 and (whatever the number of digits in the radix is).
    IntPredicate radixBoundsFactory(int base, char b, char B) {
        final int upperBound = Math.min('9', '0' + base);
        final int lcLetterLowerBound = (base > 10 ? 'a' : '0');
        final int lcLetterUpperBound = (base > 10 ? ((base - 10) + 'a') : '0');
        // upper bounds = '0' means we're "turning off" alphabetic digits;
        // i.e., we're not in hexadecimal.
        final int ucLetterLowerBound = (base > 10 ? 'A' : '0');
        final int ucLetterUpperBound = (base > 10 ? ((base - 10) + 'A') : '0');
        IntPredicate withinBounds = (c) -> (('0' <= c && c <= upperBound) ||
                ((lcLetterLowerBound <= c && c <= lcLetterUpperBound) ||
                 (ucLetterLowerBound <= c && c <= ucLetterUpperBound)));
        return withinBounds;
    }

    void assembleRadixNumberLexeme(int base, char b, char B) {
        assert (('0' == peek()) && ((b == peekNext()) || (B == peekNext()) ||
                                    Character.isDigit(peekNext())));
        currentLexeme.appendCodePoint(advance());
        if ((b == peek()) || (B == peek())) { // octal formats make this optional :(
            currentLexeme.appendCodePoint(advance());
        }
        // build predicate to test if we're still in the right radix
        IntPredicate withinBounds = radixBoundsFactory(base, b, B);
        // CONSUME!
        while (withinBounds.test((int)peek())) {
            currentLexeme.appendCodePoint(advance());
        }
    }

    void tokenizeRadixNumberLexeme(int base, char b, char B) {
        int sign = ('-' == currentLexeme.charAt(0) ? -1 : 1);
        int signOffset = ('-' == currentLexeme.charAt(0) || '+' == currentLexeme.charAt(0)) ? 1 : 0;
        char radixSpec = currentLexeme.charAt(1+signOffset);
        int start = ((b == radixSpec || B == radixSpec) ? 2 : 1)+signOffset;
        if (isJavascriptBigintSuffix()) {
            currentLexeme.appendCodePoint(advance());
            BigInteger literal = new BigInteger(currentLexeme.substring(start, currentLexeme.length()-1), base);
            addToken(NUMBER, (-1 == sign ? literal.negate() : literal));
        } else {
            Long literal = Long.parseLong(currentLexeme.substring(start), base);
            addToken(NUMBER, sign*literal);
        }
    }
}