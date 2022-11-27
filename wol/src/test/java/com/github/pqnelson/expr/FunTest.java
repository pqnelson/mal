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

import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.Fun;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Vector;

public class FunTest
{
    @Test
    public void cloneTest1() {
        Seq body = new Seq();
        body.conj(Literal.T);
        Fun constantlyTrue = new Fun((args) -> Literal.T, Vector.EMPTY, body);
        Fun clown = new Fun(constantlyTrue);
        assertTrue(clown.equals(constantlyTrue));
    }
    @Test
    public void cloneTest2() {
        Seq body = new Seq();
        body.conj(Literal.T);
        Fun constantlyTrue = new Fun((args) -> Literal.T, Vector.EMPTY);
        Fun clown = new Fun(constantlyTrue.f, constantlyTrue.params, body);
        assertTrue(clown.equals(constantlyTrue));
    }
    @Test
    public void hasSameImplementationTest1() {
        Seq body = new Seq();
        body.conj(Literal.T);
        Fun constantlyTrue = new Fun((args) -> Literal.T, Vector.EMPTY);
        Fun clown = new Fun(constantlyTrue.f, constantlyTrue.params, body);
        assertTrue(clown.hasSameImplementation(constantlyTrue));
    }
}