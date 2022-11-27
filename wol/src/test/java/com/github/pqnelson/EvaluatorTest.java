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

    @Nested
    class def1 {
        static Env env;
        @BeforeAll
        static void loadDef1() throws Throwable {
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
    }
}