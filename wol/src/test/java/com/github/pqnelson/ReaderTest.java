package com.github.pqnelson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


public class ReaderTest
{
    @Nested
    class ReadAtomTests {
        @Test
        public void readPiTest1() {
            String lexeme = "3.14159";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Expr.Literal.class, e);
        }

        @Test
        public void readPiTest2() {
            String lexeme = "3.14159";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Expr.Literal.class, e);
            Expr.Literal lit = (Expr.Literal)e;
            assertEquals(lit.token.lexeme, lexeme);
        }

        @Test
        public void readKeywordTest1() {
            String lexeme = ":my-keyword";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Expr.Keyword.class, e);
            Expr.Keyword kw = (Expr.Keyword)e;
            assertEquals(":"+kw.identifier.lexeme, lexeme);
        }

        @Test
        public void readNilTest1() {
            String lexeme = "nil";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Expr.Literal.class, e);
            Expr.Literal lit = (Expr.Literal)e;
            assertEquals(lit.token.lexeme, lexeme);
        }

        @Test
        public void readTrueTest1() {
            String lexeme = "true";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Expr.Literal.class, e);
            Expr.Literal lit = (Expr.Literal)e;
            assertEquals(lit.token.lexeme, lexeme);
        }

        @Test
        public void readFalseTest1() {
            String lexeme = "true";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Expr.Literal.class, e);
            Expr.Literal lit = (Expr.Literal)e;
            assertEquals(lit.token.lexeme, lexeme);
        }

        @Test
        public void readSymbolTest1() {
            String lexeme = "true?";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Expr.Symbol.class, e);
            Expr.Symbol lit = (Expr.Symbol)e;
            assertEquals(lit.identifier.lexeme, lexeme);
        }

    }

    @Nested
    class ReadVectorTests {
        @Test
        public void readVectorTest1() {
            String lexeme = "[1]";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Expr.Vector.class, e);
        }
        @Test
        public void readVectorTest2() {
            String lexeme = "[1 2 3 4]";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Expr.Vector.class, e);
            Expr.Vector vec = (Expr.Vector)e;
            assertEquals(vec.contents.size(), 4);
        }
        @Test
        public void readVectorTest3() {
            String lexeme = "[1, 2, 3, 4]";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Expr.Vector.class, e);
            Expr.Vector vec = (Expr.Vector)e;
            assertEquals(vec.contents.size(), 4);
        }
    }


    @Nested
    class ReadSpecialFormTests {
        @Test
        public void readDefTest1() {
            String lexeme = "(def foo [1 2 3 4])";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Expr.Def.class, e);
            Expr.Def def = (Expr.Def)e;
            assertInstanceOf(Expr.Symbol.class, def.name);
            assertEquals(def.name.identifier.lexeme, "foo");
        }
        @Test
        public void readFunTest1() {
            String lexeme = "(fn* foo [1 2 3 4] then do stuff)";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Expr.Fun.class, e);
        }
        @Test
        public void readFunTest2() {
            String lexeme = "(fn* foo [1 2 3 4] then do stuff)";
            Expr e = Reader.readString(lexeme);
            Expr.Fun fn = (Expr.Fun)e;
            assertInstanceOf(Expr.Symbol.class, fn.name);
            assertEquals(fn.name.identifier.lexeme, "foo");
        }
        @Test
        public void readFunNameTest() {
            String lexeme = "(fn* foo [1 2 3 4] then do stuff)";
            Expr e = Reader.readString(lexeme);
            Expr.Fun fn = (Expr.Fun)e;
            assertEquals(fn.name.identifier.lexeme, "foo");
        }
        @Test
        public void readLetTest1() {
            String lexeme = "(let* [x 3] now do stuff)";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Expr.Let.class, e);
        }
        @Test
        public void readIfTest1() {
            String lexeme = "(if (= x y) (go-there) (stay-here))";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Expr.If.class, e);
        }
        @Test
        public void readIfTest2() {
            String lexeme = "(if (= x y) (go-there))";
            Expr e = Reader.readString(lexeme);
            assertInstanceOf(Expr.If.class, e);
        }
    }
    @Test
    public void readListTest1() {
        String lexeme = "(this is a list)";
        Expr e = Reader.readString(lexeme);
        assertInstanceOf(Expr.Seq.class, e);
    }
}
