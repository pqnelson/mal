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

import com.github.pqnelson.expr.Expr;
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
}
