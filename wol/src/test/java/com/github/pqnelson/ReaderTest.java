package com.github.pqnelson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.BigInt;
import com.github.pqnelson.expr.Float;
import com.github.pqnelson.expr.Fun;
import com.github.pqnelson.expr.Int;
import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Str;
import com.github.pqnelson.expr.Symbol;
import com.github.pqnelson.expr.Vector;

public class ReaderTest
{
    @Nested
    class ReadAtomTests {
        @Test
        public void readPiTest1() {
            String lexeme = "3.14159";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Literal.class, e);
        }

        @Test
        public void readPiTest2() {
            String lexeme = "3.14159";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Literal.class, e);
            Literal lit = (Literal)e;
            assertEquals(lit.token.lexeme, lexeme);
        }

        @Test
        public void readKeywordTest1() {
            String lexeme = ":my-keyword";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Keyword.class, e);
            Keyword kw = (Keyword)e;
            assertEquals(":"+kw.name(), lexeme);
        }

        @Test
        public void readNilTest1() {
            String lexeme = "nil";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Literal.class, e);
            Literal lit = (Literal)e;
            assertEquals(lit.token.lexeme, lexeme);
        }

        @Test
        public void readTrueTest1() {
            String lexeme = "true";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Literal.class, e);
            Literal lit = (Literal)e;
            assertEquals(lit.token.lexeme, lexeme);
        }

        @Test
        public void readFalseTest1() {
            String lexeme = "true";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Literal.class, e);
            Literal lit = (Literal)e;
            assertEquals(lit.token.lexeme, lexeme);
        }

        @Test
        public void readSymbolTest1() {
            String lexeme = "true?";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Symbol.class, e);
            Symbol lit = (Symbol)e;
            assertEquals(lit.name(), lexeme);
        }

    }

    @Nested
    class ReadVectorTests {
        @Test
        public void readVectorTest1() {
            String lexeme = "[1]";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Vector.class, e);
        }
        @Test
        public void readVectorTest2() {
            String lexeme = "[1 2 3 4]";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Vector.class, e);
            Vector vec = (Vector)e;
            assertEquals(vec.size(), 4);
        }
        @Test
        public void readVectorTest3() {
            String lexeme = "[1, 2, 3, 4]";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Vector.class, e);
            Vector vec = (Vector)e;
            assertEquals(vec.size(), 4);
        }
    }

    @Test
    public void readListTest1() {
        String lexeme = "(this is a list)";
        Expr e = Reader.readString(lexeme);
        assertInstanceOf(Seq.class, e);
    }
}
