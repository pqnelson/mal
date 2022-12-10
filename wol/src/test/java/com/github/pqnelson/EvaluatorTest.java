package com.github.pqnelson;

import org.apache.commons.text.StringEscapeUtils;

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
import com.github.pqnelson.expr.LispException;
import com.github.pqnelson.expr.LispIOException;
import com.github.pqnelson.expr.LispIllegalArgumentException;
import com.github.pqnelson.expr.LispError;
import com.github.pqnelson.expr.LispNoSuchMethodException;
import com.github.pqnelson.expr.LispThrowable;
import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.Map;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Str;
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
public class EvaluatorTest {

    static InputStream resource(String resourceName) {
        return EvaluatorTest.class.getClassLoader().getResourceAsStream(resourceName);
    }

    static Env loadResource(String resourceName) throws Throwable {
        return loadResource(resourceName, true);
    }

    static Env loadResource(String resourceName, boolean parserPrefersFloats)
            throws Throwable {

        Env env = Evaluator.initialEnv();

        try (InputStreamReader r = new InputStreamReader(resource(resourceName))) {
            ReadTable reader = new ReadTable(r);
            reader.preferParsingNumbersAsFloats = parserPrefersFloats;
            while(!reader.isFinished()) {
                Expr e = reader.read();
                if (null == e) continue;
                Evaluator.eval(e, env);
            }
            return env;
        }
    }

    @Test
    public void oneEqualsItself() {
        try {
            Env env = Evaluator.initialEnv();
            ReadTable reader = new ReadTable("(= 1 1)");
            reader.preferParsingNumbersAsFloats = false;

            Expr result = Evaluator.eval(reader.read(), env);
            assertTrue(Literal.exprIsTrue(result));
        } catch (Throwable e) {
            assertTrue(false, "Throwable "+e.toString()+" thrown");
        }
    }

    @Test
    public void quasiquoteOfEmptyListTest() {
        try {
            Expr expected = new Seq();
            Env env = Evaluator.initialEnv();
            ReadTable reader = new ReadTable("`()");
            reader.preferParsingNumbersAsFloats = false;

            Expr result = Evaluator.eval(reader.read(), env);
            assertEquals(expected, result);
        } catch (Throwable e) {
            assertTrue(false, "Throwable "+e.toString()+" thrown");
        }
    }

    @Nested
    public class def1Tests {
        static Env env;
        @BeforeAll
        static void loadDef1() throws Throwable {
            Evaluator.debug = false;
            env = loadResource("def1.wol");
        }

        @Test
        public void evalTest1() {
            Symbol foo = new Symbol("foo");
            assertEquals(env, env.find(foo));
        }

        @Test
        public void evalTest2() {
            Float val = new Float(13);
            Symbol foo = new Symbol("foo");
            assertEquals(val, env.get(foo));
        }

        @Test
        public void divTest() {
            Float expected = new Float(1.0/2.0);
            Symbol foo = new Symbol("x1");
            assertEquals(expected, env.get(foo));
        }

        @Test
        public void inversionTest() {
            Float expected = new Float(1.0/2.0);
            Symbol foo = new Symbol("x2");
            assertEquals(expected, env.get(foo));
        }

        @Test
        public void emptyInversionTest() {
            Symbol foo = new Symbol("x3");
            assertEquals(Literal.ONE, env.get(foo));
        }

        @Test
        public void ifNilEqualsIfFalseTest() {
            Symbol foo = new Symbol("t1");
            assertEquals(Literal.T, env.get(foo));
        }
    }

    @Nested
    public class def2Tests {
        static Env env;
        @BeforeAll
        static void loadDef2() throws Throwable {
            Evaluator.debug = false;
            env = loadResource("def2.wol", false);
        }

        @Test
        public void evalDef2Test1() {
            Symbol x = new Symbol("x1");
            Int val = new Int(3);
            assertEquals(val, env.get(x));
        }

        @Test
        public void evalDef2Test2() {
            Symbol x = new Symbol("x2");
            Int val = new Int(11);
            assertEquals(val, env.get(x));
        }
        @Test
        public void evalDef2Test3() {
            Symbol x = new Symbol("x3");
            Int val = new Int(8);
            assertEquals(val, env.get(x));
        }
        @Test
        public void evalDef2Test4() {
            Symbol x = new Symbol("x4");
            Int val = new Int(2);
            assertEquals(val, env.get(x));
        }
        @Test
        public void evalDef2Test5() {
            Symbol x = new Symbol("x5");
            Int val = new Int(1010L);
            assertEquals(val, (Int)env.get(x));
        }
        @Test
        public void evalDef2Test6() {
            Symbol x = new Symbol("x6");
            Int val = new Int(-18L);
            assertEquals(val, (Int)env.get(x));
        }
        @Test
        public void evalDef2Test7() {
            Symbol x = new Symbol("x7");
            Int val = new Int(-994L);
            assertEquals(val, (Int)env.get(x));
        }
        @Test
        public void evalDef2Test8() {
            Symbol x = new Symbol("x8");
            Vector val = new Vector();
            val.conj(new Int(1));
            val.conj(new Int(2));
            val.conj(new Int(3));
            assertEquals(val, env.get(x));
        }
    }

    @Nested
    public class def3Tests {
        static Env env;
        @BeforeAll
        static void loadDef3() throws Throwable {
            Evaluator.debug = false;
            env = loadResource("def3.wol", false);
        }
        @Test
        public void evalDef3Test1() {
            Symbol x = new Symbol("x1");
            Int val = new Int(3);
            assertEquals(val, env.get(x));
        }
        @Test
        public void evalDef3Test2() {
            Symbol x = new Symbol("x2");
            assertEquals(Literal.T, env.get(x));
        }
        @Test
        public void evalDef3Test3() {
            Symbol x = new Symbol("x3");
            assertEquals(Literal.T, env.get(x));
        }
        @Test
        public void evalDef3Test4() {
            Symbol x = new Symbol("x4");
            assertEquals(Literal.T, env.get(x));
        }
        @Test
        public void evalDef3Test5() {
            Symbol x = new Symbol("x5");
            assertEquals(Literal.T, env.get(x));
        }
        @Test
        public void evalDef3Test6() {
            Symbol x = new Symbol("x6");
            assertEquals(Literal.T, env.get(x));
        }
        @Test
        public void evalDef3Test6_huh() {
            Symbol x = new Symbol("x6");
            assertTrue(env.get(x).isLiteral());
            assertTrue(Literal.exprIsTrue(env.get(x)));
        }
        @Test
        public void evalDef3Test7() {
            Symbol x = new Symbol("x7");
            Int val = new Int(-1);
            assertEquals(val, env.get(x));
        }
    }

    @Nested
    public class def4Tests {
        static Env env;
        @BeforeAll
        static void loadDef4() throws Throwable {
            Evaluator.debug = false;
            env = loadResource("def4.wol", false);
        }
        @Test
        public void evalDef4Test1CheckType() {
            Symbol x = new Symbol("x1");
            assertTrue(env.get(x).isMap());
        }

        @Test
        public void evalDef4Test1CheckSize() {
            Symbol x = new Symbol("x1");
            assertEquals(1, ((Map)env.get(x)).size());
        }
        @Test
        public void evalDef4Test1ContainsKey() {
            Symbol x = new Symbol("x1");
            Keyword k = new Keyword("abcd");
            assertTrue(((Map)env.get(x)).contains(k));
        }
        @Test
        public void evalDef4Test1KeyIsBoundToVal() {
            Symbol x = new Symbol("x1");
            Int i = new Int(1234L);
            Keyword k = new Keyword("abcd");
            assertEquals(i, ((Map)env.get(x)).get(k));
        }
        @Test
        public void evalDef4Test2() {
            Symbol x = new Symbol("x2");
            assertEquals(Literal.T, env.get(x));
        }
        @Test
        public void evalDef4Test3() {
            Symbol x = new Symbol("x3");
            assertEquals(Literal.F, env.get(x));
        }
        @ParameterizedTest
        @ValueSource(strings = {"x4-1", "x4-2", "x4-3", "x4-4", "x4-5",
                                "x4-5", "x4-6", "x4-7", "x4-8"})
        public void evalDef4Test4(String subcase) {
            Symbol x = new Symbol(subcase);
            assertEquals(Literal.T, env.get(x), "Failed "+subcase);
        }
        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8})
        public void evalDef4Test5(int subcaseNumber) {
            String subcase = "x5-"+subcaseNumber;
            Symbol x = new Symbol(subcase);
            assertEquals(Literal.T, env.get(x), "Failed "+subcase);
        }
        @Test
        public void evalDef4Test6() {
            Symbol s = new Symbol("s6");
            Symbol x = new Symbol("x6");
            Symbol ex = new Symbol("expected-x6");
            Printer p = new Printer(true);
            assertEquals(Literal.T, env.get(x));
        }
        @Test
        public void evalDef4Test6what() {
            Symbol s = new Symbol("s6");
            Symbol x = new Symbol("x6");
            Symbol ex = new Symbol("expected-x6");
            Printer p = new Printer(true);
            assertEquals(env.get(ex), env.get(s));
        }

        @Test
        public void evalDef4Test7() {
            Symbol s = new Symbol("s7");
            Symbol x = new Symbol("x7");
            Printer p = new Printer(true);
            assertEquals(env.get(x).toString(),
                         env.get(s).accept(p));
        }

        @Test
        public void evalDef4Test8() {
            Symbol s = new Symbol("s8");
            Symbol x = new Symbol("x8");
            Printer p = new Printer(true);
            assertEquals(env.get(x), env.get(s));
        }
        @Test
        public void evalDef4Test9() {
            Symbol s = new Symbol("test9");
            Symbol x = new Symbol("x9");
            assertEquals(Literal.T, env.get(s));
        }
    }
    @Nested
    class FnTest {
        static Env env;
        @BeforeAll
        static void loadFns() throws Throwable {
            env = loadResource("fn.wol", false);
            Evaluator.debug = false;
        }
        @Test
        public void fun1Test() {
            Symbol s = new Symbol("test1");
            assertEquals(Literal.T, env.get(s));
        }
        @Test
        public void fun2Test() {
            Symbol s = new Symbol("test2");
            Expr expected = new Int(0);
            assertEquals(expected, env.get(s));
        }
        @Test
        public void fun3Test() {
            Symbol s = new Symbol("test3");
            assertEquals(Literal.T, env.get(s));
        }
        @Test
        public void fun4Test() {
            Symbol s = new Symbol("test4");
            Expr expected = new Int(24);
            assertEquals(expected, env.get(s));
        }
        @Test
        public void let1Test() {
            Symbol s = new Symbol("let1");
            assertEquals(new Int(3), env.get(s));
        }
        @Test
        public void let2Test() {
            Symbol s = new Symbol("let2");
            assertEquals(new Int(9), env.get(s));
        }
        @Test
        public void if1Test() {
            Symbol s = new Symbol("if1");
            Symbol x = new Symbol("x1");
            Expr expected = new Keyword("fun");
            assertEquals(expected, env.get(s));
            assertEquals(new Int(0), env.get(x));
        }
        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5})
        public void ltTest(int i) {
            Symbol s = new Symbol("lt"+i);
            assertEquals(Literal.T, env.get(s), "Failed case "+i);
        }
        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4})
        public void gtTest(int i) {
            Symbol s = new Symbol("gt"+i);
            assertEquals(Literal.T, env.get(s));
        }
        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5})
        public void leqTest(int i) {
            Symbol s = new Symbol("leq"+i);
            assertEquals(Literal.T, env.get(s), "Failed case "+i);
        }
        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4})
        public void geqTest(int i) {
            Symbol s = new Symbol("geq"+i);
            assertEquals(Literal.T, env.get(s));
        }
        @ParameterizedTest
        @ValueSource(strings = {"lt-fail", "leq-fail", "gt-fail", "geq-fail",
            "lt-fail2", "leq-fail2", "gt-fail2", "geq-fail2"})
        public void failComparisonTests(String name) {
            Symbol s = new Symbol(name);
            assertEquals(Literal.F, env.get(s), "Fail: "+name);
        }

        @Nested
        public class ExpectedComparisonFailures {

            @Test
            public void geqFailsWithMultipleArgsTest() {
                try {
                    Env env = Evaluator.initialEnv();
                    ReadTable reader = new ReadTable("(>= 4 3 2 1 :i 0)");
                    reader.preferParsingNumbersAsFloats = false;

                    assertThrows(LispException.class,
                                 () -> Evaluator.eval(reader.read(), env));
                } catch (Throwable e) {
                    assertTrue(false, "Throwable "+e.toString()+" thrown");
                }
            }
            @Test
            public void geqFailsWithMultipleArgs2Test() {
                try {
                    Env env = Evaluator.initialEnv();
                    ReadTable reader = new ReadTable("(>= :err 4 3 2 1 :i 0)");
                    reader.preferParsingNumbersAsFloats = false;

                    assertThrows(LispException.class,
                                 () -> Evaluator.eval(reader.read(), env));
                } catch (Throwable e) {
                    assertTrue(false, "Throwable "+e.toString()+" thrown");
                }
            }
            @Test
            public void geqFailsWithTwoArgsTest() {
                try {
                    Env env = Evaluator.initialEnv();
                    ReadTable reader = new ReadTable("(>= 1 :i)");
                    reader.preferParsingNumbersAsFloats = false;

                    assertThrows(LispException.class,
                                 () -> Evaluator.eval(reader.read(), env));
                } catch (Throwable e) {
                    assertTrue(false, "Throwable "+e.toString()+" thrown");
                }
            }
            @Test
            public void geqFailsWithTwoArgs2Test() {
                try {
                    Env env = Evaluator.initialEnv();
                    ReadTable reader = new ReadTable("(>= :i 1)");
                    reader.preferParsingNumbersAsFloats = false;

                    assertThrows(LispException.class,
                                 () -> Evaluator.eval(reader.read(), env));
                } catch (Throwable e) {
                    assertTrue(false, "Throwable "+e.toString()+" thrown");
                }
            }
            @Test
            public void gtFailsWithMultipleArgsTest() {
                try {
                    Env env = Evaluator.initialEnv();
                    ReadTable reader = new ReadTable("(> 4 3 2 1 :i 0)");
                    reader.preferParsingNumbersAsFloats = false;

                    assertThrows(LispException.class,
                                 () -> Evaluator.eval(reader.read(), env));
                } catch (Throwable e) {
                    assertTrue(false, "Throwable "+e.toString()+" thrown");
                }
            }
            @Test
            public void gtFailsWithMultipleArgs2Test() {
                try {
                    Env env = Evaluator.initialEnv();
                    ReadTable reader = new ReadTable("(> :spam 4 3 2 1 :i 0)");
                    reader.preferParsingNumbersAsFloats = false;

                    assertThrows(LispException.class,
                                 () -> Evaluator.eval(reader.read(), env));
                } catch (Throwable e) {
                    assertTrue(false, "Throwable "+e.toString()+" thrown");
                }
            }
            @Test
            public void gtFailsWithTwoArgsTest() {
                try {
                    Env env = Evaluator.initialEnv();
                    ReadTable reader = new ReadTable("(> 1 :i)");
                    reader.preferParsingNumbersAsFloats = false;

                    assertThrows(LispException.class,
                                 () -> Evaluator.eval(reader.read(), env));
                } catch (Throwable e) {
                    assertTrue(false, "Throwable "+e.toString()+" thrown");
                }
            }
            @Test
            public void gtFailsWithTwoArgs2Test() {
                try {
                    Env env = Evaluator.initialEnv();
                    ReadTable reader = new ReadTable("(> :i 1)");
                    reader.preferParsingNumbersAsFloats = false;

                    assertThrows(LispException.class,
                                 () -> Evaluator.eval(reader.read(), env));
                } catch (Throwable e) {
                    assertTrue(false, "Throwable "+e.toString()+" thrown");
                }
            }
            @Test
            public void ltFailsWithMultipleArgsTest() {
                try {
                    Env env = Evaluator.initialEnv();
                    ReadTable reader = new ReadTable("(< 1 2 3 :i 4)");
                    reader.preferParsingNumbersAsFloats = false;

                    assertThrows(LispException.class,
                                 () -> Evaluator.eval(reader.read(), env));
                } catch (Throwable e) {
                    assertTrue(false, "Throwable "+e.toString()+" thrown");
                }
            }

            @Test
            public void ltFailsWithMultipleArgs2Test() {
                try {
                    Env env = Evaluator.initialEnv();
                    ReadTable reader = new ReadTable("(< :spam1 2 3 :i 4)");
                    reader.preferParsingNumbersAsFloats = false;

                    assertThrows(LispException.class,
                                 () -> Evaluator.eval(reader.read(), env));
                } catch (Throwable e) {
                    assertTrue(false, "Throwable "+e.toString()+" thrown");
                }
            }
            @Test
            public void ltFailsWithTwoArgsTest() {
                try {
                    Env env = Evaluator.initialEnv();
                    ReadTable reader = new ReadTable("(< :i 1)");
                    reader.preferParsingNumbersAsFloats = false;

                    assertThrows(LispException.class,
                                 () -> Evaluator.eval(reader.read(), env));
                } catch (Throwable e) {
                    assertTrue(false, "Throwable "+e.toString()+" thrown");
                }
            }
            @Test
            public void ltFailsWithTwoArgs2Test() {
                try {
                    Env env = Evaluator.initialEnv();
                    ReadTable reader = new ReadTable("(< 1 :i)");
                    reader.preferParsingNumbersAsFloats = false;

                    assertThrows(LispException.class,
                                 () -> Evaluator.eval(reader.read(), env));
                } catch (Throwable e) {
                    assertTrue(false, "Throwable "+e.toString()+" thrown");
                }
            }
            @Test
            public void leqFailsWithMultipleArgsTest() {
                try {
                    Env env = Evaluator.initialEnv();
                    ReadTable reader = new ReadTable("(<= 1 2 3 :i 4)");
                    reader.preferParsingNumbersAsFloats = false;

                    assertThrows(LispException.class,
                                 () -> Evaluator.eval(reader.read(), env));
                } catch (Throwable e) {
                    assertTrue(false, "Throwable "+e.toString()+" thrown");
                }
            }
            @Test
            public void leqFailsWithMultipleArgs2Test() {
                try {
                    Env env = Evaluator.initialEnv();
                    ReadTable reader = new ReadTable("(<= :foo1 2 3 :i 4)");
                    reader.preferParsingNumbersAsFloats = false;
                    assertThrows(LispException.class,
                                 () -> Evaluator.eval(reader.read(), env));
                } catch (Throwable e) {
                    assertTrue(false, "Throwable "+e.toString()+" thrown");
                }
            }
            @Test
            public void leqFailsWithTwoArgsTest() {
                try {
                    Env env = Evaluator.initialEnv();
                    ReadTable reader = new ReadTable("(<= 1 :i)");
                    reader.preferParsingNumbersAsFloats = false;
                    assertThrows(LispException.class,
                                 () -> Evaluator.eval(reader.read(), env));
                } catch (Throwable e) {
                    assertTrue(false, "Throwable "+e.toString()+" thrown");
                }
            }
            @Test
            public void leqFailsWithTwoArgs2Test() {
                try {
                    Env env = Evaluator.initialEnv();
                    ReadTable reader = new ReadTable("(<= :i 1)");
                    reader.preferParsingNumbersAsFloats = false;

                    assertThrows(LispException.class,
                                 () -> Evaluator.eval(reader.read(), env));
                } catch (Throwable e) {
                    assertTrue(false, "Throwable "+e.toString()+" thrown");
                }
            }
        }

    }
    @Nested
    class QuasiquoteTest {
        static Env env;
        @BeforeAll
        static void loadQuasiquote() throws Throwable {
            env = loadResource("quasiquote.wol", false);
        }

        @Test
        public void simpleQuasiquoteTest1() {
            Expr expected = Literal.NIL;
            Symbol x = new Symbol("simple1");
            assertEquals(expected, env.get(x));
        }
        @Test
        public void simpleQuasiquoteTest2() {
            Expr expected = new Int(7);
            Symbol x = new Symbol("simple2");
            assertEquals(expected, env.get(x));
        }
        @Test
        public void simpleQuasiquoteTest3() {
            Expr expected = new Symbol("a");
            Symbol x = new Symbol("simple3");
            assertEquals(expected, env.get(x));
        }
        @Test
        public void simpleQuasiquoteTest4() {
            Expr expected = Literal.T;
            Symbol x = new Symbol("simple4");
            assertEquals(expected, env.get(x));
        }

        @Test
        public void seqQuasiquoteTest1() {
            Seq expected = new Seq();
            Symbol x = new Symbol("list1");
            assertEquals(expected, env.get(x));
        }

        @Test
        public void seqQuasiquoteTest2() {
            Seq expected = new Seq();
            expected.conj(new Int(1));
            expected.conj(new Int(2));
            expected.conj(new Int(3));
            Symbol x = new Symbol("list2");
            assertEquals(expected, env.get(x));
        }

        @Test
        public void seqQuasiquoteTest3() {
            Seq expected = new Seq();
            expected.conj(new Symbol("a"));
            Symbol x = new Symbol("list3");
            assertEquals(expected, env.get(x));
        }

        @Test
        public void seqQuasiquoteTest4() {
            Seq expected = new Seq();
            expected.conj(new Int(1));
            expected.conj(new Int(2));
            Seq tmp = new Seq();
            tmp.conj(new Int(3));
            tmp.conj(new Int(4));
            expected.conj(tmp);
            Symbol x = new Symbol("list4");
            assertEquals(expected, env.get(x));
        }
    }
    @Nested
    class UnquoteTest {
        static Env env;
        @BeforeAll
        static void loadQuasiquote() throws Throwable {
            Evaluator.debug = false;
            env = loadResource("quasiquote.wol", false);
            Evaluator.debug = false;
        }
        @Test
        public void unquoteTest1() {
            Expr expected = new Int(31);
            Symbol x = new Symbol("unquote1");
            assertEquals(expected, env.get(x));
        }

        @Test
        public void unquoteTest2() {
            Expr expected = new Int(57);
            Symbol x = new Symbol("unquote2");
            assertEquals(expected, env.get(x));
        }

        @Test
        public void unquoteTest3() {
            Seq expected = new Seq();
            expected.conj(new Int(1));
            expected.conj(new Int(2));
            Symbol x = new Symbol("unquote3");
            assertEquals(expected, env.get(x));
        }
        @Test
        public void unquoteTest4() {
            Expr expected = Literal.T;
            Symbol x = new Symbol("unquote4");
            assertEquals(expected, env.get(x));
        }
        @Test
        public void unquoteTest5() {
            Seq expected = new Seq();
            expected.conj(new Int(1));
            expected.conj(new Int(1));
            expected.conj(new Str("b"));
            expected.conj(new Keyword("c"));
            expected.conj(new Int(3));
            Symbol x = new Symbol("unquote5");
            assertEquals(expected, env.get(x));
        }
        @Test
        public void whenTest1() {
            Seq expected = new Seq();
            expected.conj(Symbol.IF);
            expected.conj(Literal.T);
            Seq tmp = new Seq();
            tmp.conj(Symbol.DO);
            Seq arg = new Seq();
            arg.conj(new Symbol("+"));
            arg.conj(new Int(1));
            arg.conj(new Int(2));
            tmp.conj(arg);
            arg = new Seq();
            arg.conj(new Symbol("*"));
            arg.conj(new Int(3));
            arg.conj(new Int(4));
            tmp.conj(arg);
            arg = new Seq();
            arg.conj(new Symbol("/"));
            arg.conj(new Int(8));
            arg.conj(new Int(2));
            tmp.conj(arg);
            expected.conj(tmp);
            expected.conj(Literal.NIL);
            Symbol x = new Symbol("when1");
            assertEquals(expected, env.get(x));
        }
        @Test
        public void whenTest2() {
            Expr expected = Literal.T;
            Symbol x = new Symbol("when2");
            assertEquals(expected, env.get(x));
        }
        @ParameterizedTest
        @ValueSource(ints = {1, 2})
        public void macroexpandsTests(int name) {
            Symbol s = new Symbol("quasiquoteexpand-test"+name);
            assertEquals(Literal.T, env.get(s), "Fail: "+s.name());
        }
    }
    @Nested
    class MacrosTest {
        static Env env;
        @BeforeAll
        static void loadMacros() throws Throwable {
            env = loadResource("macros.wol", false);
        }

        @Test
        public void macroExpandsToConstantTest() {
            Expr expected = new Int(2);
            Symbol t1 = new Symbol("t1");
            assertEquals(expected, env.get(t1));
        }
        @ParameterizedTest
        @ValueSource(strings = {"t2", "t3"})
        public void macroexpandsTests(String name) {
            Symbol s = new Symbol(name);
            assertEquals(Literal.T, env.get(s), "Fail: "+name);
        }
    }
    @Nested
    class TryTest {
        static Env env;
        @BeforeAll
        static void loadMacros() throws Throwable {
            env = loadResource("try.wol", false);
        }

        @Test
        public void tryDoesNothingWhenNoExceptionIsThrownTest() {
            Expr expected = Literal.T;
            Symbol t1 = new Symbol("t1");
            assertEquals(expected, env.get(t1));
        }
        @ParameterizedTest
        @ValueSource(strings = {"t2", "t3", "t4"})
        public void failComparisonTests(String name) {
            Symbol s = new Symbol(name);
            assertEquals(Literal.T, env.get(s), "Fail: "+name);
        }
    }
}