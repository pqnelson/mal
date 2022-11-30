package com.github.pqnelson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.pqnelson.expr.Expr;
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class CoreTest {

    @Test
    public void aritiesToStr1Test() {
        Set<Integer> arities = new HashSet<>();
        assertEquals("{}", Core.aritiesToStr(arities));
    }

    @Test
    public void aritiesToStr2Test() {
        Set<Integer> arities = new HashSet<>();
        arities.add(3);
        assertEquals("{3}", Core.aritiesToStr(arities));
    }

    @Test
    public void aritiesToStr3Test() {
        Set<Integer> arities = new TreeSet<>();
        arities.add(2);
        arities.add(3);
        arities.add(4);
        assertEquals("{2, 3, 4}", Core.aritiesToStr(arities));
    }


    @Nested
    public class AddTest {
        @Test
        public void onePlusTwoIsThreeTest() {
            Int i = new Int(1L);
            Int j = new Int(2L);
            Int k = new Int(3L);
            Seq args = new Seq();
            args.conj(i);
            args.conj(j);
            try {
                Expr result = Core.add(args);
                assertTrue(result.isInt());
                assertEquals((Int)result, k);
            } catch (NoSuchMethodException e) {
                assertTrue(false, "NoSuchMethodException thrown?!");
            }
        }
        @Test
        public void onePlusTwoPlusThreeIsSixTest() {
            Int i = new Int(1L);
            Int j = new Int(2L);
            Int k = new Int(3L);
            Int expected = new Int(6L);
            Seq args = new Seq();
            args.conj(i);
            args.conj(j);
            args.conj(k);
            try {
                Expr result = Core.add(args);
                assertTrue(result.isInt());
                assertEquals((Int)result, expected);
            } catch (NoSuchMethodException e) {
                assertTrue(false, "NoSuchMethodException thrown?!");
            }
        }
        @Test
        public void addEmptyIsZeroTest() {
            Int expected = new Int(0L);
            Seq args = new Seq();
            try {
                assertEquals(expected, (Int)Core.add(args));
            } catch (NoSuchMethodException e) {
                assertTrue(false, "NoSuchMethodException thrown?!");
            }
        }
    }

    @Nested
    public class SubtractionTest {
        @Test
        public void subtractEmptyIsZeroTest() {
            Int expected = new Int(0L);
            Seq args = new Seq();
            try {
                assertEquals(expected, (Int)Core.subtract(args));
            } catch (NoSuchMethodException e) {
                assertTrue(false, e.getMessage());
            }
        }
    }

    @Nested
    public class EqualityTest {
        @Test
        public void oneEqualsItselfTest() {
            Int i = new Int(1L);
            Int j = new Int(1L);
            Seq args = new Seq();
            args.conj(i);
            args.conj(j);
            assertFalse(i == j);
            try {
                Expr result = Core.equality(args);
                assertTrue(Literal.exprIsTrue(result));
            } catch (NoSuchMethodException e) {
                assertTrue(false, "NoSuchMethodException thrown?!");
            }
        }
        @Test
        public void threeEqualsItselfTest() {
            Int i = new Int(3L);
            Int j = new Int(3L);
            Seq args = new Seq();
            args.conj(i);
            args.conj(j);
            assertFalse(i == j);
            try {
                Expr result = Core.equality(args);
                assertTrue(Literal.exprIsTrue(result));
            } catch (NoSuchMethodException e) {
                assertTrue(false, "NoSuchMethodException thrown?!");
            }
        }
        @Test
        public void threeThreesAreEqualTest() {
            Int i = new Int(3L);
            Int j = new Int(3L);
            Int k = new Int(3L);
            Seq args = new Seq();
            args.conj(i);
            args.conj(j);
            args.conj(k);
            assertFalse(i == j);
            assertFalse(i == k);
            assertFalse(j == k);
            try {
                Expr result = Core.equality(args);
                assertTrue(Literal.exprIsTrue(result));
            } catch (NoSuchMethodException e) {
                assertTrue(false, "NoSuchMethodException thrown?!");
            }
        }
        @Test
        public void onePlusTwoEqualsThreeTest() {
            Int i = new Int(1L);
            Int j = new Int(2L);
            Int k = new Int(3L);
            Seq args = new Seq();
            args.conj(i);
            args.conj(j);
            try {
                Expr result = Core.add(args);
                assertTrue(result.isInt());
                assertEquals((Int)result, k);
                args = new Seq();
                args.conj((Int)result);
                args.conj(k);
                result = Core.equality(args);
                assertTrue(Literal.exprIsTrue(result));
            } catch (NoSuchMethodException e) {
                assertTrue(false, "NoSuchMethodException thrown?!");
            }
        }

        @Test
        public void emptyEqualityTest() {
            Seq args = new Seq();
            Expr expected = Literal.T;
            try {
                assertEquals(expected, Core.equality(args));
            } catch (Throwable ex) {
                assertTrue(false, ex.getMessage());
            }
        }
        @Test
        public void unaryEqualityTest() {
            Seq args = new Seq();
            args.conj(new Int(31));
            Expr expected = Literal.T;
            try {
                assertEquals(expected, Core.equality(args));
            } catch (Throwable ex) {
                assertTrue(false, ex.getMessage());
            }
        }
        @Test
        public void failingEqualityTest() {
            Seq args = new Seq();
            args.conj(new Int(13));
            args.conj(new Int(31));
            Expr expected = Literal.F;
            try {
                assertEquals(expected, Core.equality(args));
            } catch (Throwable ex) {
                assertTrue(false, ex.getMessage());
            }
        }
        @Test
        public void failingEqualityWithMultipleArgsTest() {
            Seq args = new Seq();
            args.conj(new Int(13));
            args.conj(new Int(13));
            args.conj(new Int(13));
            args.conj(new Int(31));
            Expr expected = Literal.F;
            try {
                assertEquals(expected, Core.equality(args));
            } catch (Throwable ex) {
                assertTrue(false, ex.getMessage());
            }
        }
    }

    @Test
    public void falsumIsFalseTest() {
        Seq args = new Seq();
        args.conj(Literal.F);
        try {
            Expr result = Core.false_QMARK_.invoke(args);
            assertEquals(Literal.T, result);
        } catch (Throwable e) {
            assertTrue(false, "Throwable thrown?!");
        }
    }
    @Test
    public void falsumIsFalse1Test() {
        Seq args = new Seq();
        args.conj(Literal.F);
        try {
            Expr result = Literal.exprIsFalse(args.first()) ? Literal.T : Literal.F;
            assertEquals(Literal.T, result);
        } catch (Throwable e) {
            assertTrue(false, "Throwable thrown?!");
        }
    }

    @Test
    public void boxTrueResultSatisfiesLiteralIsTrueTest() {
        assertTrue(Literal.exprIsTrue(Core.boxBool.apply(true)));
    }

    @Test
    public void boxFalseResultSatisfiesLiteralIsFalseTest() {
        assertTrue(Literal.exprIsFalse(Core.boxBool.apply(false)));
    }

    @Nested
    public class EmptyQmarkTest {
        @Test
        public void emptySeqIsEmptyTest() {
            Expr coll = new Seq();
            Seq args = new Seq();
            args.conj(coll);
            try {
                assertEquals(Literal.T, Core.empty_QMARK_(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void nonemptySeqIsNotEmptyTest() {
            Seq coll = new Seq();
            coll.conj(new Int(3));
            Seq args = new Seq();
            args.conj(coll);
            try {
                assertEquals(Literal.F, Core.empty_QMARK_(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void emptyVectorIsEmptyTest() {
            Expr coll = new Vector();
            Seq args = new Seq();
            args.conj(coll);
            try {
                assertEquals(Literal.T, Core.empty_QMARK_(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void nonemptyVectorIsNotEmptyTest() {
            Vector coll = new Vector();
            coll.conj(new Int(3));
            Seq args = new Seq();
            args.conj(coll);
            try {
                assertEquals(Literal.F, Core.empty_QMARK_(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void emptyMapIsEmptyTest() {
            Expr coll = new Map();
            Seq args = new Seq();
            args.conj(coll);
            try {
                assertEquals(Literal.T, Core.empty_QMARK_(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void nonemptyMapIsNotEmptyTest() {
            Map coll = new Map();
            coll.assoc(new Keyword("Foo"), new Int(3));
            Seq args = new Seq();
            args.conj(coll);
            try {
                assertEquals(Literal.F, Core.empty_QMARK_(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
    }

    @Nested
    public class SeqTest {
        @Test
        public void seqNilIsNilTest() {
            Seq args = new Seq();
            args.conj(Literal.NIL);
            try {
                assertEquals(Literal.NIL, Core.seq(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void seqSeqTest() {
            Seq v = new Seq();
            Seq expected = new Seq();
            Int i;
            int array[] = {1,2,3,4};
            for (int val : array) {
                i = new Int(val);
                v.conj(i);
                expected.conj(i);
            }
            Seq args = new Seq();
            args.conj(v);
            try {
                assertEquals(expected, Core.seq(args));
                assertTrue(v == Core.seq(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void seqVectorTest() {
            Vector v = new Vector();
            Seq expected = new Seq();
            Int i;
            int array[] = {1,2,3,4};
            for (int val : array) {
                i = new Int(val);
                v.conj(i);
                expected.conj(i);
            }
            Seq args = new Seq();
            args.conj(v);
            try {
                assertEquals(expected, Core.seq(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void seqStrTest() {
            Str s = new Str("abc");
            Seq expected = new Seq();
            Str i;
            String array[] = {"a", "b", "c"};
            for (String val : array) {
                i = new Str(val);
                expected.conj(i);
            }
            Seq args = new Seq();
            args.conj(s);
            try {
                assertEquals(expected, Core.seq(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void seqMapTest() {
            Map m = new Map();
            Keyword k = new Keyword("key");
            Int val = new Int(31);
            Seq expected = new Seq();
            Vector entry = new Vector();
            entry.conj(k);
            entry.conj(val);
            expected.conj(entry);
            m.assoc(k, val);
            Seq args = new Seq();
            args.conj(m);
            try {
                assertEquals(expected, Core.seq(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void seqErrTest() {
            Int i = new Int(3);
            Seq args = new Seq();
            args.conj(i);
            assertThrows(NoSuchMethodException.class,
                         () -> Core.seq(args));
        }
    }

    @Nested
    public class FirstTest {
        @Test
        public void firstOfNilIsNilTest() {
            Expr expected = Literal.NIL;
            Seq args = new Seq();
            args.conj(Literal.NIL);
            try {
                assertEquals(expected, Core.seq(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void firstOfEmptySeqIsNilTest() {
            Expr expected = Literal.NIL;
            Seq args = new Seq();
            args.conj(new Seq());
            try {
                assertEquals(expected, Core.seq(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void firstOfIntIsErrorTest() {
            Int i = new Int(3);
            Seq args = new Seq();
            args.conj(i);
            assertThrows(NoSuchMethodException.class,
                         () -> Core.first(args));
        }
    }

    @Nested
    public class RestTest {
        @Test
        public void checkArityOfRest1Test() {
            Seq args = new Seq();
            assertThrows(NoSuchMethodException.class,
                         () -> Core.rest(args));
        }
        @Test
        public void checkArityOfRest2Test() {
            Seq args = new Seq();
            args.conj(new Vector());
            args.conj(new Seq());
            assertThrows(NoSuchMethodException.class,
                         () -> Core.rest(args));
        }
        @Test
        public void restOfNilIsNilTest() {
            Seq args = new Seq();
            args.conj(Literal.NIL);
            try {
                assertEquals(Literal.NIL, Core.rest(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void rest1Test() {
            Seq args = new Seq();
            Seq coll = new Seq();
            Seq expected = new Seq();
            coll.conj(new Keyword("first"));
            coll.conj(new Str("b"));
            coll.conj(new Int(3));
            expected.conj(new Str("b"));
            expected.conj(new Int(3));
            args.conj(coll);
            try {
                assertEquals(expected, Core.rest(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
    }

    @Nested
    public class ConcatTest {
        @Test
        public void concatGivenNothingIsNilTest() {
            Seq args = new Seq();
            Expr expected = Literal.NIL;
            try {
                assertEquals(expected, Core.concat(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void concatOfEmptySeqIsNilTest() {
            Seq args = new Seq();
            args.conj(new Seq());
            Expr expected = Literal.NIL;
            try {
                assertEquals(expected, Core.concat(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void concat1Test() {
            Seq coll1 = new Seq();
            Seq expected = new Seq();
            coll1.conj(new Int(1));
            coll1.conj(new Str("b"));
            expected.conj(new Int(1));
            expected.conj(new Str("b"));

            Seq coll2 = new Seq();
            coll2.conj(new Int(4));
            coll2.conj(new Str("e"));
            expected.conj(new Int(4));
            expected.conj(new Str("e"));
            Seq args = new Seq();
            args.conj(coll1);
            args.conj(coll2);
            PPrinter p = new PPrinter();
            try {
                assertEquals(expected, Core.concat(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
    }

    @Nested
    public class ConsTest {
        @Test
        public void consArity1Test() {
            Seq args = new Seq();
            assertThrows(NoSuchMethodException.class,
                         () -> Core.seq(args));
        }
        @Test
        public void consArity2Test() {
            Seq args = new Seq();
            Expr e = new Int(33);
            args.conj(e);
            assertThrows(NoSuchMethodException.class,
                         () -> Core.seq(args));
        }
        @Test
        public void cons1Test() {
            Seq args = new Seq();
            Expr e = new Int(33);
            Seq expected = new Seq();
            args.conj(e);
            expected.conj(e);
            Seq coll = new Seq();
            Expr e2 = new Keyword("foobar");
            coll.conj(e2);
            expected.conj(e2);
            args.conj(coll);
            try {
                assertEquals(expected, Core.cons(args));
            } catch (Throwable ex) {
                assertTrue(false, ex.getMessage());
            }
        }
    }

    @Nested
    class SymbolConstructorTest {
        @Test
        public void symbolRequiresAtLeastOneArgTest() {
            Seq args = new Seq();
            try {
                assertThrows(NoSuchMethodException.class,
                             () -> Core.symbol(args));
            } catch (IllegalArgumentException e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void symbolRequiresNoMoreThanOneArgTest() {
            Seq args = new Seq();
            args.conj(new Str("symbol"));
            args.conj(new Str("test"));
            try {
                assertThrows(NoSuchMethodException.class,
                         () -> Core.symbol(args));
            } catch (IllegalArgumentException e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void symbolOfSymbolTest1() {
            Expr expected = new Symbol("symbol");
            Seq args = new Seq();
            args.conj(new Symbol("symbol"));
            try {
                assertEquals(expected, Core.symbol(args));
            } catch (IllegalArgumentException e) {
                assertTrue(false, e.getMessage());
            } catch (NoSuchMethodException e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void symbolOfSymbolShouldBeIdenticalTest() {
            Expr expected = new Symbol("symbol");
            Seq args = new Seq();
            args.conj(expected);
            try {
                assertTrue(expected==Core.symbol(args));
            } catch (IllegalArgumentException e) {
                assertTrue(false, e.getMessage());
            } catch (NoSuchMethodException e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void symbolOfKeywordTest1() {
            String name = "keyword";
            Expr expected = new Symbol(name);
            Seq args = new Seq();
            args.conj(new Keyword(name));
            try {
                assertEquals(expected, Core.symbol(args));
            } catch (IllegalArgumentException e) {
                assertTrue(false, e.getMessage());
            } catch (NoSuchMethodException e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void symbolOfStrTest1() {
            Expr expected = new Symbol("symbol");
            Seq args = new Seq();
            args.conj(new Str("symbol"));
            try {
                assertEquals(expected, Core.symbol(args));
            } catch (IllegalArgumentException e) {
                assertTrue(false, e.getMessage());
            } catch (NoSuchMethodException e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void symbolOfVectorFailsTest1() {
            Expr expected = new Symbol("symbol");
            Seq args = new Seq();
            args.conj(new Vector());
            try {
                assertThrows(IllegalArgumentException.class,
                             () -> Core.symbol(args));
            } catch (IllegalArgumentException e) {
                assertTrue(false, e.getMessage());
            }
        }
    }

    @Nested
    class KeywordConstructorTest {
        @Test
        public void keywordRequiresAtLeastOneArgTest() {
            Seq args = new Seq();
            try {
                assertThrows(NoSuchMethodException.class,
                             () -> Core.keyword(args));
            } catch (IllegalArgumentException e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void keywordRequiresNoMoreThanOneArgTest() {
            Seq args = new Seq();
            args.conj(new Str("symbol"));
            args.conj(new Str("test"));
            try {
                assertThrows(NoSuchMethodException.class,
                         () -> Core.keyword(args));
            } catch (IllegalArgumentException e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void keywordOfSymbolTest1() {
            Expr expected = new Keyword("symbol-arg");
            Seq args = new Seq();
            args.conj(new Symbol("symbol-arg"));
            try {
                assertEquals(expected, Core.keyword(args));
            } catch (IllegalArgumentException e) {
                assertTrue(false, e.getMessage());
            } catch (NoSuchMethodException e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void keywordOfKeywordShouldBeIdenticalTest() {
            Expr expected = new Keyword("expected-symbol");
            Seq args = new Seq();
            args.conj(expected);
            try {
                assertTrue(expected==Core.keyword(args));
            } catch (IllegalArgumentException e) {
                assertTrue(false, e.getMessage());
            } catch (NoSuchMethodException e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void keywordOfStringTest1() {
            String name = "keyword";
            Expr expected = new Keyword(name);
            Seq args = new Seq();
            args.conj(new Str(name));
            try {
                assertEquals(expected, Core.keyword(args));
            } catch (IllegalArgumentException e) {
                assertTrue(false, e.getMessage());
            } catch (NoSuchMethodException e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void keywordOfVectorFailsTest1() {
            Expr expected = new Symbol("symbol");
            Seq args = new Seq();
            args.conj(new Vector());
            try {
                assertThrows(IllegalArgumentException.class,
                             () -> Core.keyword(args));
            } catch (IllegalArgumentException e) {
                assertTrue(false, e.getMessage());
            }
        }
    }

    @Nested
    public class PrintlnTest {
        private final PrintStream standardOut = System.out;
        private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        @BeforeEach
        public void setUp() {
            System.setOut(new PrintStream(outputStreamCaptor));
        }

        @AfterEach
        public void tearDown() {
            System.setOut(standardOut);
        }

        @Test
        public void printlnNilTest() {
            String expected = "nil";
            Printer p = new Printer();
            Seq args = new Seq();
            args.conj(Literal.NIL);
            try {
                Core.println(args);
                assertEquals(expected, outputStreamCaptor.toString().trim());
            } catch (NoSuchMethodException e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void printlnTrueTest() {
            String expected = "true";
            Printer p = new Printer();
            Seq args = new Seq();
            args.conj(Literal.T);
            try {
                Core.println(args);
                assertEquals(expected, outputStreamCaptor.toString().trim());
            } catch (NoSuchMethodException e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void printlnFalseTest() {
            String expected = "false";
            Printer p = new Printer();
            Seq args = new Seq();
            args.conj(Literal.F);
            try {
                Core.println(args);
                assertEquals(expected, outputStreamCaptor.toString().trim());
            } catch (NoSuchMethodException e) {
                assertTrue(false, e.getMessage());
            }
        }
    }

    @Nested
    public class PrnTest {
        private final PrintStream standardOut = System.out;
        private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        @BeforeEach
        public void setUp() {
            System.setOut(new PrintStream(outputStreamCaptor));
        }

        @AfterEach
        public void tearDown() {
            System.setOut(standardOut);
        }

        @Test
        public void prnNilTest() {
            Seq args = new Seq();
            args.conj(Literal.NIL);
            try {
                Core.prn(args);
                assertEquals(Core.pr_str(args),
                             new Str(outputStreamCaptor.toString().trim()));
            } catch (NoSuchMethodException e) {
                assertTrue(false, e.getMessage());
            }
        }

        @Test
        public void prnTrueTest() {
            Seq args = new Seq();
            args.conj(Literal.T);
            try {
                Core.prn(args);
                assertEquals(Core.pr_str(args),
                             new Str(outputStreamCaptor.toString().trim()));
            } catch (NoSuchMethodException e) {
                assertTrue(false, e.getMessage());
            }
        }

        @Test
        public void prnFalseTest() {
            Seq args = new Seq();
            args.conj(Literal.F);
            try {
                Core.prn(args);
                assertEquals(Core.pr_str(args),
                             new Str(outputStreamCaptor.toString().trim()));
            } catch (NoSuchMethodException e) {
                assertTrue(false, e.getMessage());
            }
        }

        @Test
        public void prnTest1() {
            Seq args = new Seq();
            Vector v = new Vector();
            v.conj(new Int(1));
            v.conj(new Str("bcd"));
            v.conj(new Float(45.67e8));
            v.conj(new Map());
            args.conj(v);
            try {
                Printer p = new Printer();
                Core.prn(args);
                assertEquals(Core.pr_str(args),
                             new Str(outputStreamCaptor.toString().trim()));
            } catch (NoSuchMethodException e) {
                assertTrue(false, e.getMessage());
            }
        }
    }

    @Nested
    public class AssocTest {
        @Test
        public void assocRequiresAtLeastThreeArgs1Test() {
            Map m = new Map();
            Keyword k = new Keyword("foobar");
            Int val = new Int(3);
            Seq args = new Seq();
            args.conj(m);
            assertThrows(NoSuchMethodException.class,
                         () -> Core.assoc(args));
        }
        @Test
        public void assocRequiresAtLeastThreeArgs2Test() {
            Map m = new Map();
            Keyword k = new Keyword("foobar");
            Int val = new Int(3);
            Seq args = new Seq();
            args.conj(m);
            args.conj(k);
            assertThrows(NoSuchMethodException.class,
                         () -> Core.assoc(args));
        }
        @Test
        public void assocRequiresEvenNumberOfForms1Test() {
            Map m = new Map();
            Keyword k = new Keyword("foobar");
            Int val = new Int(3);
            Seq args = new Seq();
            args.conj(m);
            args.conj(k);
            args.conj(val);
            args.conj(new Keyword("foo"));
            assertThrows(NoSuchMethodException.class,
                         () -> Core.assoc(args));
        }
        @Test
        public void assocProducesANewMapTest() {
            Map m = new Map();
            Keyword k = new Keyword("foobar");
            Int val = new Int(3);
            Seq args = new Seq();
            args.conj(m);
            args.conj(k);
            args.conj(val);
            try {
                Expr result = Core.assoc(args);
                assertTrue(m != result);
            } catch (NoSuchMethodException e) {
                assertTrue(false, e.getMessage());
            }
        }
    }

    @Nested
    public class DissocTest {
        @Test
        public void dissocRequiresSomeArgTest() {
            Seq args = new Seq();
            assertThrows(NoSuchMethodException.class,
                         () -> Core.dissoc(args));
        }
        @Test
        public void dissocWithNoKeysIsIdentityTest() {
            Map m = new Map();
            Keyword k = new Keyword("foobar");
            Int val = new Int(3);
            m.assoc(k, val);
            Seq args = new Seq();
            args.conj(m);
            Map expected = new Map();
            expected.assoc(k, val);
            try {
                assertEquals(expected, Core.dissoc(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void dissoc1Test() {
            Map m = new Map();
            Keyword k = new Keyword("foobar");
            Int val = new Int(3);
            m.assoc(k, val);
            Seq args = new Seq();
            args.conj(m);
            args.conj(k);
            Map expected = new Map();
            try {
                assertEquals(expected, Core.dissoc(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
    }

    @Nested
    public class NthTest {
        @Test
        public void nthNilTest() {
            Seq args = new Seq();
            args.conj(Literal.NIL);
            args.conj(new Int(3));
            Expr expected = Literal.NIL;

            try {
                assertEquals(expected, Core.nth(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4, 5})
        public void nth1Test(int subcase) {
            int i = subcase;
            Int index = new Int(i);
            Seq args = new Seq();
            String letters[] = {"a", "b", "c", "d", "e", "f"};
            Str expected = new Str(letters[i]);
            Seq coll = new Seq();
            for (String letter : letters) {
                coll.conj(new Str(letter));
            }
            args.conj(coll);
            args.conj(index);
            try {
                assertEquals(expected, Core.nth(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void nth2Test() {
            Seq coll = new Seq();
            Keyword k = new Keyword("k1");
            coll.conj(k);
            k = new Keyword("k2");
            coll.conj(k);
            k = new Keyword("k3");
            coll.conj(k);
            Keyword expected = new Keyword("k3");
            Int i = new Int(2);
            Seq args = new Seq();
            args.conj(coll);
            args.conj(i);
            try {
                assertEquals(expected, Core.nth(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
        @Test
        public void nth3Test() {
            Seq coll = new Seq();
            Keyword k = new Keyword("k1");
            coll.conj(k);
            k = new Keyword("k2");
            coll.conj(k);
            k = new Keyword("k3");
            coll.conj(k);
            Keyword expected = new Keyword("k3");
            Int i = new Int(0);
            Seq args = new Seq();
            args.conj(coll);
            args.conj(i);
            try {
                Seq firstArgs = new Seq();
                firstArgs.conj(coll);
                assertEquals(Core.first(firstArgs), Core.nth(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }

        @Test
        public void nthNonIntIndexTest() {
            Seq coll = new Seq();
            Keyword k = new Keyword("k1");
            coll.conj(k);
            k = new Keyword("k2");
            coll.conj(k);
            k = new Keyword("k3");
            coll.conj(k);
            Expr i = new Float(2.0);
            Seq args = new Seq();
            args.conj(coll);
            args.conj(i);
            try {
                assertThrows(NoSuchMethodException.class, () -> Core.nth(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }

        @Test
        public void nthNonSeqableCollTest() {
            Expr coll = new Int(3);
            Expr i = new Float(2.0);
            Seq args = new Seq();
            args.conj(coll);
            args.conj(i);
            try {
                assertThrows(NoSuchMethodException.class, () -> Core.nth(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3, 4, 5})
        public void nthVector1Test(int subcase) {
            int i = subcase;
            Int index = new Int(i);
            Seq args = new Seq();
            String letters[] = {"a", "b", "c", "d", "e", "f"};
            Str expected = new Str(letters[i]);
            Vector coll = new Vector();
            for (String letter : letters) {
                coll.conj(new Str(letter));
            }
            args.conj(coll);
            args.conj(index);
            try {
                assertEquals(expected, Core.nth(args));
            } catch (Throwable e) {
                assertTrue(false, e.getMessage());
            }
        }
    }
}
