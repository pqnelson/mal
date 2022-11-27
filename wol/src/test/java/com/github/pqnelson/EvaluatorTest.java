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
import com.github.pqnelson.expr.Symbol;
import com.github.pqnelson.expr.Vector;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Test the evaluator works as expected with the initial environment.
 *
 * Works by running through scripts found in {@literal /src/test/resources/}
 * and executing them, then compares bound results to what we expect.
 *
 * It seems like there should be a way to automate this. There probably
 * is, but it seems like it would require trusting various functions to work
 * as expected...which is the point of this test suite!
 */
class EvaluatorTest {

    static InputStream resource(String resourceName) {
        return EvaluatorTest.class.getClassLoader().getResourceAsStream(resourceName);
    }

    static Env loadResource(String resourceName) throws Throwable {
        return loadResource(resourceName, true);
    }

    static Env loadResource(String resourceName, boolean parserPrefersFloats) throws Throwable {
        Env env = Evaluator.initialEnv();
        try (InputStreamReader r = new InputStreamReader(resource(resourceName));
            BufferedReader resource = new BufferedReader(r)) {
            Scanner scanner = new Scanner(resource);
            scanner.preferParsingNumbersAsFloats = parserPrefersFloats;
            Reader reader = new Reader(scanner.scanTokens());
            while(!reader.isAtEnd()) {
                Evaluator.eval(reader.readForm(), env);
            }
            return env;
        }
    }

    @Test
    public void oneEqualsItself() {
        try {
            Env env = Evaluator.initialEnv();
            Scanner scanner = new Scanner("(= 1 1)");
            scanner.preferParsingNumbersAsFloats = false;
            Reader reader = new Reader(scanner.scanTokens());
            Expr result = Evaluator.eval(reader.readForm(), env);
            assertTrue(Literal.exprIsTrue(result));
        } catch (Throwable e) {
            assertTrue(false, "Throwable "+e.toString()+" thrown");
        }
    }

    @Nested
    class def1 {
        static Env env;
        @BeforeAll
        static void loadDef1() throws Throwable {
            Evaluator.debug = false;
            env = loadResource("def1.wol");
        }

        @Test
        void evalTest1() {
            Symbol foo = new Symbol("foo");
            assertEquals(env, env.find(foo));
        }

        @Test
        void evalTest2() {
            Float val = new Float(13);
            Symbol foo = new Symbol("foo");
            assertEquals(val, env.get(foo));
        }
    }

    @Nested
    class def2 {
        static Env env;
        @BeforeAll
        static void loadDef2() throws Throwable {
            Evaluator.debug = false;
            env = loadResource("def2.wol", false);
        }

        @Test
        void evalDef2Test1() {
            Symbol x = new Symbol("x1");
            Int val = new Int(3);
            assertEquals(val, env.get(x));
        }

        @Test
        void evalDef2Test2() {
            Symbol x = new Symbol("x2");
            Int val = new Int(11);
            assertEquals(val, env.get(x));
        }
        @Test
        void evalDef2Test3() {
            Symbol x = new Symbol("x3");
            Int val = new Int(8);
            assertEquals(val, env.get(x));
        }
        @Test
        void evalDef2Test4() {
            Symbol x = new Symbol("x4");
            Int val = new Int(2);
            assertEquals(val, env.get(x));
        }
        @Test
        void evalDef2Test5() {
            Symbol x = new Symbol("x5");
            Int val = new Int(1010L);
            assertEquals(val, (Int)env.get(x));
        }
        @Test
        void evalDef2Test6() {
            Symbol x = new Symbol("x6");
            Int val = new Int(-18L);
            assertEquals(val, (Int)env.get(x));
        }
        @Test
        void evalDef2Test7() {
            Symbol x = new Symbol("x7");
            Int val = new Int(-994L);
            assertEquals(val, (Int)env.get(x));
        }
        @Test
        void evalDef2Test8() {
            Symbol x = new Symbol("x8");
            Vector val = new Vector();
            val.conj(new Int(1));
            val.conj(new Int(2));
            val.conj(new Int(3));
            assertEquals(val, env.get(x));
        }
    }

    @Nested
    class def3 {
        static Env env;
        @BeforeAll
        static void loadDef3() throws Throwable {
            Evaluator.debug = false;
            env = loadResource("def3.wol", false);
        }
        @Test
        void evalDef3Test1() {
            Symbol x = new Symbol("x1");
            Int val = new Int(3);
            assertEquals(val, env.get(x));
        }
        @Test
        void evalDef3Test2() {
            Symbol x = new Symbol("x2");
            assertEquals(Literal.T, env.get(x));
        }
        @Test
        void evalDef3Test3() {
            Symbol x = new Symbol("x3");
            assertEquals(Literal.T, env.get(x));
        }
        @Test
        void evalDef3Test4() {
            Symbol x = new Symbol("x4");
            assertEquals(Literal.T, env.get(x));
        }
        @Test
        void evalDef3Test5() {
            Symbol x = new Symbol("x5");
            assertEquals(Literal.T, env.get(x));
        }
        @Test
        void evalDef3Test6() {
            Symbol x = new Symbol("x6");
            assertEquals(Literal.T, env.get(x));
        }
        @Test
        void evalDef3Test6_huh() {
            Symbol x = new Symbol("x6");
            assertTrue(env.get(x).isLiteral());
            assertTrue(Literal.exprIsTrue(env.get(x)));
        }
        @Test
        void evalDef3Test7() {
            Symbol x = new Symbol("x7");
            Int val = new Int(-1);
            assertEquals(val, env.get(x));
        }
    }

    @Nested
    class def4 {
        static Env env;
        @BeforeAll
        static void loadDef4() throws Throwable {
            env = loadResource("def4.wol", false);
        }
        @Test
        void evalDef4Test1CheckType() {
            Symbol x = new Symbol("x1");
            assertTrue(env.get(x).isMap());
        }
        @Test
        void evalDef4Test1CheckSize() {
            Symbol x = new Symbol("x1");
            assertEquals(1, ((Map)env.get(x)).size());
        }
        @Test
        void evalDef4Test1ContainsKey() {
            Symbol x = new Symbol("x1");
            Keyword k = new Keyword("abcd");
            assertTrue(((Map)env.get(x)).contains(k));
        }
        @Test
        void evalDef4Test1KeyIsBoundToVal() {
            Symbol x = new Symbol("x1");
            Int i = new Int(1234L);
            Keyword k = new Keyword("abcd");
            assertEquals(i, ((Map)env.get(x)).get(k));
        }
        @Test
        void evalDef4Test2() {
            Symbol x = new Symbol("x2");
            assertEquals(Literal.T, env.get(x));
        }
        @Test
        void evalDef4Test3() {
            Symbol x = new Symbol("x3");
            assertEquals(Literal.F, env.get(x));
        }
        @ParameterizedTest
        @ValueSource(strings = {"x4-1", "x4-2", "x4-3", "x4-4", "x4-5",
                                "x4-5", "x4-6", "x4-7", "x4-8"})
        void evalDef4Test4(String subcase) {
            Symbol x = new Symbol(subcase);
            assertEquals(Literal.T, env.get(x), "Failed "+subcase);
        }
        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8})
        void evalDef4Test5(int subcaseNumber) {
            String subcase = "x5-"+subcaseNumber;
            Symbol x = new Symbol(subcase);
            assertEquals(Literal.T, env.get(x), "Failed "+subcase);
        }
        @Test
        void evalDef4Test6() {
            Symbol s = new Symbol("s6");
            Symbol x = new Symbol("x6");
            Symbol ex = new Symbol("expected-x6");
            Printer p = new Printer(true);
            assertEquals(Literal.T, env.get(x));
        }
        @Test
        void evalDef4Test6what() {
            Symbol s = new Symbol("s6");
            Symbol x = new Symbol("x6");
            Symbol ex = new Symbol("expected-x6");
            Printer p = new Printer(true);
            assertEquals(env.get(ex), env.get(s));
        }

        @Test
        void evalDef4Test7() {
            Symbol s = new Symbol("s7");
            Symbol x = new Symbol("x7");
            Printer p = new Printer(true);
            assertEquals(env.get(x).toString(),
                         env.get(s).accept(p));
        }
    }
}