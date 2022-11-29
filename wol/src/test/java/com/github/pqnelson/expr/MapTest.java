package com.github.pqnelson.expr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Map;

public class MapTest
{
    @Nested
    class equalsTests {
        @Test
        public void nullValueShouldNotMatchAbsentKey1() {
            Map lhs = new Map();
            Keyword k = new Keyword("spam");
            lhs.assoc(k, null);
            Map rhs = new Map();
            assertNotEquals(lhs, rhs);
        }
        @Test
        public void nullValueShouldNotMatchAbsentKey2() {
            Map rhs = new Map();
            Keyword k = new Keyword("spam");
            rhs.assoc(k, null);
            Map lhs = new Map();
            assertNotEquals(lhs, rhs);
        }
        @Test
        public void emptyMapsShouldBeEqualTest() {
            Map lhs = new Map();
            Map rhs = new Map();
            assertTrue (lhs != rhs);
            assertEquals(lhs, rhs);
        }
    }

    @Test
    public void emptyMapsAreEmpty() {
        Map m = new Map();
        assertTrue (m.isEmpty());
    }

    @Test
    public void nonemptyMapsAreNotEmpty() {
        Map m = new Map();
        Keyword k = new Keyword("spam");
        Keyword v = new Keyword("eggs");
        m.assoc(k, v);
        assertFalse (m.isEmpty());
    }

    @Test
    public void keysTest1() {
        Map m = new Map();
        assertTrue (m.keys().isEmpty());
    }

    @Test
    public void keysTest2() {
        Map m = new Map();
        Keyword k = new Keyword("spam");
        Keyword v = new Keyword("eggs");
        m.assoc(k, v);
        Seq keys = new Seq();
        keys.conj(k);
        assertEquals (m.keys(), keys);
    }

    @Test
    public void keysTest3() {
        Map m = new Map();
        Keyword k = new Keyword("spam");
        Keyword v = new Keyword("eggs");
        m.assoc(k, v);
        Seq keys = new Seq();
        keys.conj(k);
        k = new Keyword("keyword2");
        v = new Keyword("another-val");
        m.assoc(k, v);
        keys.conj(k);
        assertEquals (m.keys().size(), keys.size());
        assertEquals (m.keys().sort(), keys.sort());
    }

    @Test
    public void typeTest() {
        Map s = new Map();
        assertEquals("Map", s.type());
    }
}