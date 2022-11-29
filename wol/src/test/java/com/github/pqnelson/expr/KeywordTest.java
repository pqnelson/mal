package com.github.pqnelson.expr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.pqnelson.expr.Keyword;

public class KeywordTest
{
    @Test
    public void KeywordEqualsTest1() {
        Keyword lhs = new Keyword("foo");
        Keyword rhs = new Keyword("foo");
        assertEquals(lhs, rhs);
    }

    @Test
    public void KeywordEqualsReflexiveTest1() {
        Keyword lhs = new Keyword("foo");
        assertEquals(lhs, lhs);
    }
    @Test
    public void KeywordEqualsSymmetryTest1() {
        Keyword lhs = new Keyword("foo");
        Keyword rhs = new Keyword("foo");
        assertEquals(lhs, rhs);
        assertEquals(rhs, lhs);
    }

    @Test
    public void KeywordEqualsTransitiveTest1() {
        Keyword a = new Keyword("foo");
        Keyword b = new Keyword("foo");
        Keyword c = new Keyword("foo");
        assertEquals(a, b);
        assertEquals(b, c);
        assertEquals(a, c);
    }

    @Test
    public void typeTest() {
        Keyword s = new Keyword("");
        assertEquals("Keyword", s.type());
    }
}