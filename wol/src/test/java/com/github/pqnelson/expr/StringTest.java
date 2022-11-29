package com.github.pqnelson.expr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.Str;

public class StringTest
{
    @Nested
    class substringTests {
        @Test
        public void substringIdentityTest() {
            Str s = new Str("foobar");
            Str sub = s.substring(0, "foobar".length());
            assertEquals(s, sub);
        }
        @Test
        public void substringTest1() {
            Str s = new Str("foobar");
            Str expected = new Str("foo");
            Str sub = s.substring(0, 3);
            assertEquals(expected, sub);
        }
        @Test
        public void substringTest2() {
            Str s = new Str("foobar");
            Str expected = new Str("ob");
            Str sub = s.substring(2, 4);
            assertEquals(expected, sub);
        }
    }
    @Nested
    class seqTests {
        @Test
        public void emptyStringSeqsToNil() {
            Str s = new Str("");
            assertEquals(Literal.NIL, s.seq());
        }
        @Test
        public void seqTest1() {
            Str s = new Str("foobar");
            Seq expected = new Seq();
            String letters[] = {"f", "o", "o", "b", "a", "r"};
            for (String letter : letters) {
                expected.conj(new Str(letter));
            }
            assertEquals(expected, s.seq());
        }
    }

    @Test
    public void typeTest() {
        Str s = new Str("");
        assertEquals("Str", s.type());
    }
}