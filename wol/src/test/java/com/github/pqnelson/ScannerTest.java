package com.github.pqnelson;

import java.util.InputMismatchException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import com.github.pqnelson.Scanner;

/**
 * Unit test for simple App.
 */
public class ScannerTest
{
    @Test
    public void advanceTest1() {
        String lexeme = "015";
        Scanner s = new Scanner(lexeme);
        assertEquals(Character.codePointAt(lexeme, 0), s.advance());
        assertFalse(s.isAtEnd());
        assertEquals(Character.codePointAt(lexeme, 1), s.peek());
        assertEquals(Character.codePointAt(lexeme, 2), s.peekNext());
    }

    @Test
    public void advanceTest2() {
        String lexeme = "015";
        Scanner s = new Scanner(lexeme);
        assertFalse(s.isAtEnd());
        assertEquals(Character.codePointAt(lexeme, 0), s.advance());
        assertFalse(s.isAtEnd());
        assertEquals(Character.codePointAt(lexeme, 1), s.advance());
        assertFalse(s.isAtEnd());
        assertEquals(Character.codePointAt(lexeme, 2), s.advance());
        assertTrue(s.isAtEnd());
    }

    @Test
    public void advanceTest3() {
        String lexeme = "015";
        Scanner s = new Scanner(lexeme);
        assertEquals(Character.codePointAt(lexeme, 0), s.peek());
        assertTrue(s.cachedNextNextChar.isEmpty());
        assertEquals(Character.codePointAt(lexeme, 0), s.advance());
        assertTrue(s.cachedNextChar.isEmpty());
        assertTrue(s.cachedNextNextChar.isEmpty());
        assertEquals(Character.codePointAt(lexeme, 1), s.peek());
        assertTrue(s.cachedNextNextChar.isEmpty());
        assertEquals(Character.codePointAt(lexeme, 2), s.peekNext());
        assertFalse(s.isAtEnd());
        assertEquals(Character.codePointAt(lexeme, 1), s.advance());
        assertTrue(s.cachedNextNextChar.isEmpty());
        assertEquals(Character.codePointAt(lexeme, 2), s.peek());
        assertTrue(s.cachedNextNextChar.isEmpty());
        assertEquals('\0', s.peekNext());
        assertEquals(Character.codePointAt(lexeme, 2), s.advance());
        assertEquals('\0', s.peek());
        assertEquals('\0', s.peekNext());
        assertEquals(-1, s.advance());
        assertTrue(s.isAtEnd());
    }

    @Test
    public void peekNextTest1() {
        String lexeme = "015";
        Scanner s = new Scanner(lexeme);
        assertEquals(Character.codePointAt(lexeme, 1), s.peekNext());
        assertEquals(Character.codePointAt(lexeme, 0), s.peek());
    }

    @Test
    public void peekTest1() {
        String lexeme = "015";
        Scanner s = new Scanner(lexeme);
        assertEquals(Character.codePointAt(lexeme, 0), s.peek());
        int i = s.cachedNextChar.orElse(-1);
        assertTrue(-1 != i);
        assertEquals(i, s.peek());
        assertEquals(s.advance(), i);
    }

    @Test
    public void peekTest2() {
        String lexeme = "015";
        Scanner s = new Scanner(lexeme);
        assertEquals(Character.codePointAt(lexeme, 1), s.peekNext());
        assertEquals(Character.codePointAt(lexeme, 0), s.peek());
        int i = s.cachedNextChar.orElse(-1);
        assertTrue(-1 != i);
        int j = s.cachedNextNextChar.orElse(-1);
        assertTrue(-1 != j);
        assertEquals(i, s.peek());
        assertEquals(j, s.peekNext());
        assertEquals(s.advance(), i);
        assertEquals(s.peek(), j);
    }

    /**
     * Test white space IS recognized as whitespace.
     */
    @Test
    public void tabShouldBeWhitespace() {
        int cp = Character.codePointAt("\t", 0);
        assertTrue(Scanner.isSpace(cp));
    }

    @Test
    public void newlineShouldNotBeWhitespace() {
        int cp = Character.codePointAt("\n", 0); // '\n' is system dependent?
        assertFalse(Scanner.isSpace(cp));
    }

    @Test
    public void lineFeedShouldNotBeWhitespace() {
        int cp = 0x0000a;
        assertFalse(Scanner.isSpace(cp));
    }

    @Test
    public void verticalTabShouldNotBeWhitespace() {
        int cp = 0x000b;
        assertFalse(Scanner.isSpace(cp));
    }

    @Test
    public void formFeedShouldNotBeWhitespace() {
        int cp = 0x000c;
        assertFalse(Scanner.isSpace(cp));
    }

    @Test
    public void carriageReturnShouldNotBeWhitespace() {
        int cp = 0x000d;
        assertFalse(Scanner.isSpace(cp));
    }

    @Test
    public void nextLineShouldNotBeWhitespace() {
        int cp = 0x0085;
        assertFalse(Scanner.isSpace(cp));
    }

    @Test
    public void lineSeparatorShouldNotBeWhitespace() {
        int cp = 0x2028;
        assertFalse(Scanner.isSpace(cp));
    }

    @Test
    public void paragraphSeparatorShouldNotBeWhitespace() {
        int cp = 0x2029;
        assertFalse(Scanner.isSpace(cp));
    }

    /**
     * Test vertical IS recognized as newline
     */
    @Test
    public void tabShouldBeNewline() {
        int cp = Character.codePointAt("\t", 0);
        assertFalse(Scanner.isNewline(cp));
    }

    @Test
    public void newlineShouldBeNewline() {
        int cp = Character.codePointAt("\n", 0); // '\n' is system dependent?
        assertTrue(Scanner.isNewline(cp));
    }

    @Test
    public void lineFeedShouldBeNewline() {
        int cp = 0x0000a; // Unix '\n'
        assertTrue(Scanner.isNewline(cp));
    }

    @Test
    public void verticalTabShouldBeNewline() {
        int cp = 0x000b;
        assertTrue(Scanner.isNewline(cp));
    }

    @Test
    public void formFeedShouldBeNewline() {
        int cp = 0x000c;
        assertTrue(Scanner.isNewline(cp));
    }

    @Test
    public void carriageReturnShouldBeNewline() {
        int cp = 0x000d;
        assertTrue(Scanner.isNewline(cp));
    }

    @Test
    public void nextLineShouldBeNewline() {
        int cp = 0x0085; // NEL
        assertTrue(Scanner.isNewline(cp));
    }

    @Test
    public void lineSeparatorShouldBeNewline() {
        int cp = 0x2028;
        assertTrue(Scanner.isNewline(cp));
    }

    @Test
    public void paragraphSeparatorShouldBeNewline() {
        int cp = 0x2029;
        assertTrue(Scanner.isNewline(cp));
    }

    /**
     * Parse a number.
     */
    @Test
    public void scanOneAsNumber() {
        Scanner s = new Scanner("1");
        Token t = s.scanTokens().get(0);
        assertEquals(TokenType.NUMBER, t.type);
    }

    @Test
    public void scanZeroAsNumber() {
        Scanner s = new Scanner("0");
        Token t = s.scanTokens().get(0);
        assertEquals(TokenType.NUMBER, t.type);
    }

    @Test
    public void scanPiAsNumber() {
        String lexeme = "3.14159265359";
        Scanner s = new Scanner(lexeme);
        List<Token> tokens = s.scanTokens();
        assertEquals(tokens.size(), 2);
        Token t = tokens.get(0);
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(t.lexeme, lexeme);
        t = tokens.get(1);
        assertEquals(TokenType.EOF, t.type);
    }

    @Test
    public void scanThreeE5AsNumber() {
        Scanner s = new Scanner("3e5");
        List<Token> tokens = s.scanTokens();
        assertEquals(tokens.size(), 2);
        Token t = tokens.get(0);
        assertEquals(TokenType.NUMBER, t.type);
        double x = Double.parseDouble("3e5");
    }

    @Test
    public void scanOctalNumber1() {
        String lexeme = "015";
        Scanner s = new Scanner(lexeme);
        List<Token> tokens = s.scanTokens();
        assertEquals(tokens.size(), 2);
        Token t = tokens.get(0);
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(t.lexeme, lexeme);
        t = tokens.get(1);
        assertEquals(TokenType.EOF, t.type);
    }

    @Test
    public void scanOctalNumber2() {
        String lexeme = "0001";
        Scanner s = new Scanner(lexeme);
        List<Token> tokens = s.scanTokens();
        assertEquals(tokens.size(), 2);
        Token t = tokens.get(0);
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(t.lexeme, lexeme);
        t = tokens.get(1);
        assertEquals(TokenType.EOF, t.type);
    }

    @Test
    public void scanOctalNumber3() {
        String lexeme = "0o7777777777n";
        Scanner s = new Scanner(lexeme);
        List<Token> tokens = s.scanTokens();
        assertEquals(tokens.size(), 2);
        Token t = tokens.get(0);
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(t.lexeme, lexeme);
        t = tokens.get(1);
        assertEquals(TokenType.EOF, t.type);
    }

    @Test
    public void scanOctalNumber4() {
        String lexeme = "0o7654x";
        Scanner s = new Scanner(lexeme);
        List<Token> tokens = s.scanTokens();
        assertEquals(tokens.size(), 3);
        Token t = tokens.get(0);
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(t.lexeme, lexeme.substring(0,lexeme.length()-1));
        t = tokens.get(1);
        assertEquals(TokenType.IDENTIFIER, t.type);
        t = tokens.get(2);
        assertEquals(TokenType.EOF, t.type);
    }

    @Test
    public void scanHexadecimalNumber1() {
        String lexeme = "0x1123";
        Scanner s = new Scanner(lexeme);
        List<Token> tokens = s.scanTokens();
        assertEquals(tokens.size(), 2);
        Token t = tokens.get(0);
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(t.lexeme, lexeme);
        t = tokens.get(1);
        assertEquals(TokenType.EOF, t.type);
    }

    @Test
    public void scanHexadecimalNumber2() {
        String lexeme = "0x00111";
        Scanner s = new Scanner(lexeme);
        List<Token> tokens = s.scanTokens();
        assertEquals(tokens.size(), 2);
        Token t = tokens.get(0);
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(t.lexeme, lexeme);
        t = tokens.get(1);
        assertEquals(TokenType.EOF, t.type);
    }

    @Test
    public void scanHexadecimalNumber3() {
        String lexeme = "0x123456789ABCDEFn";
        Scanner s = new Scanner(lexeme);
        List<Token> tokens = s.scanTokens();
        assertEquals(tokens.size(), 2);
        Token t = tokens.get(0);
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(t.lexeme, lexeme);
        t = tokens.get(1);
        assertEquals(TokenType.EOF, t.type);
    }

    @Test
    public void scanHexadecimalNumber4() {
        String lexeme = "-0x123456789ABCDEFn";
        Scanner s = new Scanner(lexeme);
        List<Token> tokens = s.scanTokens();
        assertEquals(tokens.size(), 2);
        Token t = tokens.get(0);
        assertEquals(TokenType.NUMBER, t.type);
        assertEquals(t.lexeme, lexeme);
        t = tokens.get(1);
        assertEquals(TokenType.EOF, t.type);
    }


    @Test
    public void identifierTest1() {
        String lexeme = "spam";
        Scanner s = new Scanner(lexeme);
        List<Token> tokens = s.scanTokens();
        assertEquals(tokens.size(), 2);
        Token t = tokens.get(0);
        assertEquals(TokenType.IDENTIFIER, t.type);
        assertEquals(t.lexeme, lexeme);
        t = tokens.get(1);
        assertEquals(TokenType.EOF, t.type);
    }

    /* Special forms tests */
    @Test
    public void defTest1() {
        String lexeme = "(def foo 3)";
        Scanner s = new Scanner(lexeme);
        List<Token> tokens = s.scanTokens();
        TokenType types[] = {TokenType.LEFT_PAREN, TokenType.DEF,
            TokenType.IDENTIFIER, TokenType.NUMBER, TokenType.RIGHT_PAREN,
            TokenType.EOF};
        for (int i=0; i < 6; i++) {
            assertEquals(tokens.get(i).type, types[i]);
        }
    }

    @Test
    public void doTest1() {
        String lexeme = "(do more stuff)";
        Scanner s = new Scanner(lexeme);
        List<Token> tokens = s.scanTokens();
        TokenType types[] = {TokenType.LEFT_PAREN, TokenType.DO,
            TokenType.IDENTIFIER, TokenType.IDENTIFIER, TokenType.RIGHT_PAREN,
            TokenType.EOF};
        for (int i=0; i < 6; i++) {
            assertEquals(tokens.get(i).type, types[i]);
        }
    }

    @Test
    public void fnTest1() {
        String lexeme = "(fn* [] stuff)";
        Scanner s = new Scanner(lexeme);
        List<Token> tokens = s.scanTokens();
        TokenType types[] = {TokenType.LEFT_PAREN, TokenType.FN_STAR,
            TokenType.LEFT_BRACKET, TokenType.RIGHT_BRACKET,
            TokenType.IDENTIFIER, TokenType.RIGHT_PAREN, TokenType.EOF};
        for (int i=0; i < types.length; i++) {
            assertEquals(tokens.get(i).type, types[i]);
        }
    }

    @Test
    public void ifTest1() {
        String lexeme = "(if test true-branch false-branch)";
        Scanner s = new Scanner(lexeme);
        List<Token> tokens = s.scanTokens();
        TokenType types[] = {TokenType.LEFT_PAREN, TokenType.IF,
            TokenType.IDENTIFIER, TokenType.IDENTIFIER, TokenType.IDENTIFIER,
            TokenType.RIGHT_PAREN, TokenType.EOF};
        for (int i=0; i < types.length; i++) {
            assertEquals(tokens.get(i).type, types[i]);
        }
    }

    @Test
    public void letTest1() {
        String lexeme = "(let* [x val] body)";
        Scanner s = new Scanner(lexeme);
        List<Token> tokens = s.scanTokens();
        TokenType types[] = {TokenType.LEFT_PAREN, TokenType.LET_STAR,
            TokenType.LEFT_BRACKET, TokenType.IDENTIFIER, TokenType.IDENTIFIER,
            TokenType.RIGHT_BRACKET, TokenType.IDENTIFIER, TokenType.RIGHT_PAREN,
            TokenType.EOF};
        for (int i=0; i < types.length; i++) {
            assertEquals(tokens.get(i).type, types[i]);
        }
    }

    @Test
    public void stringTest1() {
        String lexeme = "\"This is a happy string\"";
        Scanner s = new Scanner(lexeme);
        List<Token> tokens = s.scanTokens();
        TokenType types[] = {TokenType.STRING, TokenType.EOF};
        for (int i=0; i < types.length; i++) {
            assertEquals(tokens.get(i).type, types[i]);
        }
        assertEquals("\""+tokens.get(0).lexeme+"\"",
                     lexeme);
    }

    @Test
    public void unterminatedStringTest1() {
        String lexeme = "\"This is an unhappy string";
        Scanner s = new Scanner(lexeme);
        InputMismatchException e = assertThrows(InputMismatchException.class,
                                                () -> s.scanTokens());
        assertEquals("Line 1: Unterminated string", e.getMessage());
    }

    @Test
    public void keywordTest() {
        String lexeme = ":keyword";
        Scanner s = new Scanner(lexeme);
        List<Token> tokens = s.scanTokens();
        TokenType types[] = {TokenType.KEYWORD, TokenType.EOF};
        for (int i=0; i < types.length; i++) {
            assertEquals(tokens.get(i).type, types[i]);
        }
        assertEquals(":"+tokens.get(0).lexeme,
                     lexeme);
    }

    // @Test
    // public void () {
    // }
}
