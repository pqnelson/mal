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
import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.Fun;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Symbol;
import com.github.pqnelson.expr.Vector;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

class EvaluatorTest {

    static InputStream resource(String resourceName) {
        return EvaluatorTest.class.getClassLoader().getResourceAsStream(resourceName);
    }

    static Env loadResource(String resourceName) {
        Env env = new Env();
        Scanner scanner = new Scanner(resource(resourceName));
        Reader reader = new Reader(scanner.scanTokens());
        while(!reader.isAtEnd()) {
            Evaluator.eval(reader.readForm(), env);
        }
        return env;
    }

    @Nested
    class def1 {
        static Env env;
        @BeforeAll
        static void loadDef1() {
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
}