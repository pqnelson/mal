package com.github.pqnelson.reader_macro;

import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.InputMismatchException;

import com.github.pqnelson.ReadTable;
import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.Str;

import com.github.pqnelson.annotations.VisibleForTesting;

public class StringReaderMacro implements ReaderMacro {
    // see https://github.com/openjdk/jdk/blob/590de37bd703bdae56e8b41c84f5fca5e5a00811/src/java.base/share/classes/java/lang/Character.java#L11232-L11237
    static final int SEPARATORS = ((1 << Character.LINE_SEPARATOR)
                                   | (1 << Character.PARAGRAPH_SEPARATOR));
    private static final int CR_CODEPOINT = 0x000d;
    private static final int LF_CODEPOINT = 0x000a;
    private static final int NEL_CODEPOINT = 0x0085;  // NEL = Next Line

    /**
     * Test for a newline, using the various system-dependent versions
     * for newlines.
     */
    @VisibleForTesting
    static final boolean isNewline(final int codePoint) {
        return (((SEPARATORS >> Character.getType(codePoint)) & 1) != 0)
            || (LF_CODEPOINT <= codePoint && codePoint <= CR_CODEPOINT)
            || (NEL_CODEPOINT == codePoint);
    }

    private int next(PushbackReader input) {
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
    private void unread(final int c, PushbackReader input) {
        try {
            input.unread(c);
        } catch (IOException e) {
        }
    }

    private int peek(PushbackReader input) {
        final int result = next(input);
        unread(result, input);
        return result;
    }

    @Override
    public Expr apply(Reader stream, ReadTable reader, final int ch) {
        assert ('"' == ch);
        PushbackReader input = (PushbackReader) stream;
        StringBuffer currentLexeme = new StringBuffer();
        final long line = reader.getLineNumber();
        final long offset = reader.getOffset();
        while ('"' != peek(input) && !reader.isFinished()) {
            final int cp = next(input);
            if (isNewline(cp)) {
                reader.incLineNumber();
            }
            currentLexeme.appendCodePoint(cp);
            if ('\\' == cp && '"' == peek(input)) {
                currentLexeme.appendCodePoint(next(input));
            }
        }

        if (reader.isFinished()) {
            throw new InputMismatchException("Line [" + Long.toString(line)
                                             + "," + Long.toString(offset)
                                             + "]: Unterminated string");
        }

        next(input);
        return new Str(StringEscapeUtils.unescapeJava(currentLexeme.toString()));
    }
}
