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
import com.github.pqnelson.expr.Vector;

public class VectorTest
{
    @Test
    public void toStringTest1() {
        Vector v = new Vector();
        assertEquals("[]", v.toString());
    }

    @Test
    public void contains1Test() {
        Vector v = new Vector();
        v.conj(new Symbol("x"));
        v.conj(new Symbol("y"));
        v.conj(new Symbol("z"));
        assertFalse(v.contains(new Symbol("&")));
    }

    @Test
    public void contains2Test() {
        Vector v = new Vector();
        v.conj(new Symbol("x"));
        v.conj(new Symbol("&"));
        v.conj(new Symbol("rest"));
        assertTrue(v.contains(new Symbol("&")));
    }
}
