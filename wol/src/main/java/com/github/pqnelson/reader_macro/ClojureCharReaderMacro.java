package com.github.pqnelson.reader_macro;

import java.io.Reader;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.function.Function;

import com.github.pqnelson.ReadTable;
import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.Literal;


/**
 * Read a character literal following Clojure.
 *
 * <p>A character can look like, using the following BNF-ish grammar:</p>
 * <blockquote>
 * <dl>
 * <dt><i>SingleCharLiteral:</i></dt>
 * <dd>{@code '\'} <i>Char</i></dd>
 * <dt><i>SpecialCharLiteral:</i></dt>
 * <dd>{@code '\newline'}</dd>
 * <dd>{@code '\space'}</dd>
 * <dd>{@code '\tab'}</dd>
 * <dd>{@code '\backspace'}</dd>
 * <dd>{@code '\formfeed'}</dd>
 * <dd>{@code '\return'}</dd>
 * <dt><i>UnicodeLiteral:</i></dt>
 * <dd>{@code \u005Cu[0-9a-fA-F]}&lbrace;4&rbrace;  (i.e., {@code '\u005Cu'} followed by 4 hexadecimal digits)</dd>
 * <dt><i>CharacterLiteral</i></dt>
 * <dd><i>SingleCharLiteral</i></dd>
 * <dd><i>SpecialCharLiteral</i></dd>
 * <dd><i>UnicodeLiteral</i></dd>
 * </dl>
 * </blockquote>
 */
public class ClojureCharReaderMacro implements ReaderMacro {
    public ClojureCharReaderMacro() { }

    private int readUnicodeChar(String token, int offset, int length, int base) {
        if (token.length() != offset + length) {
            throw new IllegalArgumentException("Invalid unicode character: \\"
                                               + token);
        }
        int uc = 0;
        for (int i = offset; i < offset + length; ++i) {
            int d = Character.digit(token.charAt(i), base);
            if (-1 == d)
                throw new IllegalArgumentException("Invalid digit: "
                                                   + token.charAt(i));
            uc = uc * base + d;
		}
        return (char) uc;
    }

    @Override
    public Expr apply(Reader stream, ReadTable reader, int cp) {
        assert ('\\' == cp);
        final String token = reader.nextToken();
        if (1 == token.length()) {
            return Literal.Char(token.charAt(0));
        }

        switch (token) {
        case "newline":   return Literal.Char('\n');
        case "space":     return Literal.Char(' ');
        case "tab":       return Literal.Char('\t');
        case "backspace": return Literal.Char('\b');
        case "formfeed":  return Literal.Char('\f');
        case "return":    return Literal.Char('\r');
        default:
            if (token.startsWith("u")) {
                char c = (char)readUnicodeChar(token, 1, 4, 16);
                if (Character.isSurrogate(c)) {
                    throw new RuntimeException("Invalid character constant: \\u" + Integer.toString(c, 16));
                }
                return Literal.Char(c);
            }
        }
        throw new RuntimeException("Unsupported character: \\" + token);
    }
}
