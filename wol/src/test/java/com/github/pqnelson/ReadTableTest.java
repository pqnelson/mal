package com.github.pqnelson;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.Int;
import com.github.pqnelson.expr.Float;
import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Str;
import com.github.pqnelson.expr.Symbol;
import com.github.pqnelson.expr.Vector;

public class ReadTableTest {
    @Test
    public void emptyStringTest() {
        ReadTable r = new ReadTable("    \t\n     ");
        assertEquals(null, r.read());
    }

    @Nested
    public class ReadListTests {
        @Test
        public void nonemptyStringTest() {
            ReadTable r = new ReadTable("foo    spam\t\n     ");
            assertEquals(new Symbol("foo"), r.read());
            assertEquals(new Symbol("spam"), r.read());
        }

        @Test
        public void listTest() {
            ReadTable r = new ReadTable("(foo    spam\t\n     )");
            ArrayList<Expr> expected = new ArrayList<>();
            expected.add(new Symbol("foo"));
            expected.add(new Symbol("spam"));
            assertEquals(new Seq(expected), r.read());
        }

        @Test
        public void nestedListTest() {
            ReadTable r = new ReadTable("(foo (eggs but) and spam)");
            ArrayList<Expr> expected = new ArrayList<>();
            ArrayList<Expr> tmp = new ArrayList<>();
            tmp.add(new Symbol("eggs"));
            tmp.add(new Symbol("but"));
            expected.add(new Symbol("foo"));
            expected.add(new Seq(tmp));
            expected.add(new Symbol("and"));
            expected.add(new Symbol("spam"));
            assertEquals(new Seq(expected), r.read());
        }

        @Test
        public void nestedNestedListTest() {
            ReadTable r = new ReadTable("(foo (eggs (scrambed (stuff) suggests) but) and spam)");
            ArrayList<Expr> expected = new ArrayList<>();
            ArrayList<Expr> tmp = new ArrayList<>();
            ArrayList<Expr> inner = new ArrayList<>();
            inner.add(new Symbol("stuff"));
            tmp.add(new Symbol("scrambed"));
            tmp.add(new Seq(inner));
            tmp.add(new Symbol("suggests"));
            inner = tmp;
            tmp = new ArrayList<>();
            tmp.add(new Symbol("eggs"));
            tmp.add(new Seq(inner));
            tmp.add(new Symbol("but"));
            expected = new ArrayList<>();
            expected.add(new Symbol("foo"));
            expected.add(new Seq(tmp));
            expected.add(new Symbol("and"));
            expected.add(new Symbol("spam"));
            assertEquals(new Seq(expected), r.read());
        }
    }

    @Nested
    public class QuoteTests {
        @Test
        public void readQuoteTest() {
            ReadTable r = new ReadTable("'foo");
            Seq expected = new Seq();
            expected.conj(Symbol.QUOTE);
            expected.conj(new Symbol("foo"));
            assertEquals(expected, r.read());
        }

        @Test
        public void quotedListTest() {
            ReadTable r = new ReadTable("'(foo    spam\t\n     )");
            ArrayList<Expr> coll = new ArrayList<>();
            coll.add(new Symbol("foo"));
            coll.add(new Symbol("spam"));
            Seq expected = new Seq();
            expected.conj(Symbol.QUOTE);
            expected.conj(new Seq(coll));
            assertEquals(expected, r.read());
        }
    }

    @Nested
    public class ReadVector {
        @Test
        public void readVectorTest() {
            ReadTable r = new ReadTable("[foo    spam\t\n     ]");
            ArrayList<Expr> coll = new ArrayList<>();
            coll.add(new Symbol("foo"));
            coll.add(new Symbol("spam"));
            Vector expected = new Vector(coll);
            assertEquals(expected, r.read());
        }
    }

    @Test
    public void readKeyword() {
        ReadTable r = new ReadTable("    :spam\t\n     ");
        Expr expected = new Keyword("spam");
        assertEquals(expected, r.read());
    }

    @Nested
    public class ReadLiteralTests {
        @Test
        public void readFalseTest() {
            ReadTable r = new ReadTable("false");
            Expr expected = Literal.F;
            assertEquals(expected, r.read());
        }
        @Test
        public void readTrueTest() {
            ReadTable r = new ReadTable("true");
            Expr expected = Literal.T;
            assertEquals(expected, r.read());
        }
        @Test
        public void readNilTest() {
            ReadTable r = new ReadTable("nil");
            Expr expected = Literal.NIL;
            assertEquals(expected, r.read());
        }
        @Test
        public void readTryTest() {
            ReadTable r = new ReadTable("try");
            Expr expected = Symbol.TRY;
            assertEquals(expected, r.read());
        }
        @Test
        public void readCatchTest() {
            ReadTable r = new ReadTable("catch");
            Expr expected = Symbol.CATCH;
            assertEquals(expected, r.read());
        }
    }

    @Nested
    public class CharTests {
        @Test
        public void readNewlineTest() {
            ReadTable r = new ReadTable("\\newline");
            Expr expected = Literal.Char('\n');
            assertEquals(expected, r.read());
        }
        @Test
        public void readSpaceTest() {
            ReadTable r = new ReadTable("\\space");
            Expr expected = Literal.Char(' ');
            assertEquals(expected, r.read());
        }
        @Test
        public void readTabTest() {
            ReadTable r = new ReadTable("\\tab");
            Expr expected = Literal.Char('\t');
            assertEquals(expected, r.read());
        }
        @Test
        public void readBackspaceTest() {
            ReadTable r = new ReadTable("\\backspace");
            Expr expected = Literal.Char('\b');
            assertEquals(expected, r.read());
        }
        @Test
        public void readFormfeedTest() {
            ReadTable r = new ReadTable("\\formfeed");
            Expr expected = Literal.Char('\f');
            assertEquals(expected, r.read());
        }
        @Test
        public void readReturnTest() {
            ReadTable r = new ReadTable("\\return");
            Expr expected = Literal.Char('\r');
            assertEquals(expected, r.read());
        }
        @ParameterizedTest
        @ValueSource(strings = {"!", "\"", "#", "$", "%", "&", "'", "(", ")",
                                "*", "+", ",", "-", ".", "/", "0", "1", "2",
                                "3", "4", "5", "6", "7", "8", "9", ":", ";",
                                "<", "=", ">", "?", "@", "[", "\\", "]", "^",
                                "_", "`", "{", "|", "}", "a", "b", "c", "d",
                                "e", "f", "g", "h", "i", "j", "k", "l", "m",
                                "n", "o", "p", "q", "r", "s", "t", "u", "v",
                                "w", "x", "y", "z", "A", "B", "C", "D", "E",
                                "F", "G", "H", "I", "J", "K", "L", "M", "N",
                                "O", "P", "Q", "R", "S", "T", "U", "V", "W",
                                "X", "Y", "Z"})
        public void singleStringCharTest(String token) {
            ReadTable r = new ReadTable("\\"+token);
            Expr expected = Literal.Char(token.charAt(0));
            assertEquals(expected, r.read());
        }
        @ParameterizedTest
        @ValueSource(strings = {"!", "\"", "#", "$", "%", "&", "'", "(", ")",
                                "*", "+", ",", "-", ".", "/", "0", "1", "2",
                                "3", "4", "5", "6", "7", "8", "9", ":", ";",
                                "<", "=", ">", "?", "@", "[", "\\", "]", "^",
                                "_", "`", "{", "|", "}", "a", "b", "c", "d",
                                "e", "f", "g", "h", "i", "j", "k", "l", "m",
                                "n", "o", "p", "q", "r", "s", "t", "u", "v",
                                "w", "x", "y", "z", "A", "B", "C", "D", "E",
                                "F", "G", "H", "I", "J", "K", "L", "M", "N",
                                "O", "P", "Q", "R", "S", "T", "U", "V", "W",
                                "X", "Y", "Z"})
        public void unicodeCharTest(String token) {
            ReadTable r = new ReadTable("\\u00"+Integer.toHexString(Character.codePointAt(token,0)));
            Expr expected = Literal.Char(token.charAt(0));
            assertEquals(expected, r.read());
        }

        @Test
        public void surrogateFail1Test() {
            ReadTable r = new ReadTable("\\uD800");
            assertThrows(RuntimeException.class,
                         () -> r.read());
        }

        @Test
        public void surrogateFail2Test() {
            ReadTable r = new ReadTable("\\uDFFF");
            assertThrows(RuntimeException.class,
                         () -> r.read());
        }
    }

    @Nested
    public class ReadNumberTests {
        @Test
        public void readDeadBeefTest() {
            ReadTable r = new ReadTable("0xDEADBEEF");
            Expr expected = new Int(Long.decode("0xDEADBEEF"));
            assertEquals(expected, r.read());
        }

        @Test
        public void fauxOctalTest() {
            ReadTable r = new ReadTable("05678");
            Expr expected = new Int(Long.decode("5678"));
            assertEquals(expected, r.read());
        }

        @Test
        public void readPiTest() {
            final String lexeme = "3.14159265359";
            ReadTable r = new ReadTable(lexeme);
            Expr expected = new Float(Double.parseDouble(lexeme));
            assertEquals(expected, r.read());
        }
    }
}
