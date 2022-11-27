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
import com.github.pqnelson.expr.Map;
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
        @Test
        public void readVectorTest4() {
            String lexemes[] = {"1,", "2,", "3,", "4,"};
            String lexeme = "["+String.join(" ", lexemes)+"]";
            Expr v = (Vector)Reader.readString(lexeme);
            Vector vec = new Vector();
            for (String lex : lexemes) {
                vec.conj(Reader.readString(lex));
            }
            assertEquals(vec, v);
        }
    }

    @Nested
    class ReadListTests {
        @Test
        public void readListTest1() {
            String lexeme = "(this is a list)";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Seq.class, e);
        }
        @Test
        public void readListTest2() {
            String lexemes[] = {"1,", ":key2,", "spam-symbol,", "0x04,"};
            String lexeme = "("+String.join(" ", lexemes)+")";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Seq.class, e);
            Seq coll = (Seq)e;
            Seq list = new Seq();
            for (String lex : lexemes) {
                list.conj(Reader.readString(lex));
            }
            assertEquals(list, coll);
        }
    }

    @Nested
    class ReadKeywordTests {
        @Test
        public void readKeywordTest1() {
            String lexeme = ":keyword";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Keyword.class, e);
        }
        @Test
        public void readKeywordTest2() {
            String lexeme = ":keyword";
            Keyword kw1 = (Keyword)Reader.readString(lexeme);
            Keyword kw2 = (Keyword)Reader.readString(lexeme);
            assertEquals(kw1, kw2);
            assertFalse(kw1 == kw2);
        }
    }

    @Nested
    class ReadMapTests {
        @Test
        public void readEmptyMapTest1() {
            String lexeme = "{}";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Map.class, e);
        }
        @Test
        public void readEmptyMapTest2() {
            String lexeme = "{}";
            Map m = (Map)Reader.readString(lexeme);
            assertTrue(m.isEmpty());
        }
        @Test
        public void readMapTest1() {
            String lexeme = "{:key 1}";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Map.class, e);
        }
        @Test
        public void readMapTest2() {
            String lexeme = "{:key 1}";
            Map m = (Map)Reader.readString(lexeme);
            Keyword k = (Keyword)Reader.readString(":key");
            assertTrue(m.contains(k));
        }
        @Test
        public void readMapRequiresEvenNumberOfFormsTest() {
            String lexeme = "{:key 1 :spam}";
            assertThrows(java.util.InputMismatchException.class,
                         () -> Reader.readString(lexeme));
        }
    }
}
