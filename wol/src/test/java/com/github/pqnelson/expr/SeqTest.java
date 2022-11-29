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

public class SeqTest
{
    @Test
    public void concatTest1() {
        Seq lhs = new Seq();
        lhs.conj(new Str("1"));
        Seq expected = new Seq();
        expected.conj(new Str("1"));
        assertEquals(expected, lhs.concat(Literal.NIL));
    }

    @Test
    public void concatTest2() {
        Seq lhs = new Seq();
        Seq rhs = new Seq();
        lhs.conj(new Str("1"));
        Seq expected = new Seq();
        expected.conj(new Str("1"));
        assertEquals(expected, lhs.concat(rhs));
    }

    @Test
    public void concatTest3() {
        Seq lhs = new Seq();
        Seq rhs = new Seq();
        rhs.conj(new Str("1"));
        Seq expected = new Seq();
        expected.conj(new Str("1"));
        assertEquals(expected, lhs.concat(rhs));
    }

    @Test
    public void concatTest4() {
        Seq lhs = new Seq();
        lhs.conj(new Str("0"));
        Seq rhs = new Seq();
        rhs.conj(new Str("1"));
        Seq expected = new Seq();
        expected.conj(new Str("0"));
        expected.conj(new Str("1"));
        assertEquals(expected, lhs.concat(rhs));
    }
}