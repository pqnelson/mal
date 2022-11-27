package com.github.pqnelson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.Float;
import com.github.pqnelson.expr.Int;
import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.Fun;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Symbol;
import com.github.pqnelson.expr.Vector;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

class CoreTest {
    @Nested
    class AddTests {
        @Test
        public void onePlusTwoIsThree() {
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
        public void onePlusTwoPlusThreeIsSix() {
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
    }

    @Nested
    class EqualityTests {
        @Test
        public void oneEqualsItself() {
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
        public void threeEqualsItself() {
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
        public void threeThreesAreEqual() {
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
        public void onePlusTwoEqualsThree() {
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
    }

    @Test
    public void falsumIsFalse() {
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
    public void falsumIsFalse1() {
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
    public void boxTrueResultSatisfiesLiteralIsTrue() {
        assertTrue(Literal.exprIsTrue(Core.boxBool.apply(true)));
    }

    @Test
    public void boxFalseResultSatisfiesLiteralIsFalse() {
        assertTrue(Literal.exprIsFalse(Core.boxBool.apply(false)));
    }
}
