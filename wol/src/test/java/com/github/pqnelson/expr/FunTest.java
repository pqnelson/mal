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
    public void arities1Test() {
        IFn dummy = null;
        Vector params = new Vector();
        params.conj(new Symbol("x"));
        Fun f = new Fun(dummy,  params);
        assertEquals(new Int(1), f.arities().get(0));
    }
    @Test
    public void arities2Test() {
        IFn dummy = null;
        Vector params = new Vector();
        params.conj(new Symbol("x"));
        params.conj(new Symbol("y"));
        Fun f = new Fun(dummy,  params);
        assertEquals(new Int(2), f.arities().get(0));
    }
    @Test
    public void arities3Test() {
        IFn dummy = null;
        Vector params = new Vector();
        params.conj(new Symbol("x"));
        params.conj(new Symbol("&"));
        params.conj(new Symbol("y"));
        Fun f = new Fun(dummy,  params);
        assertEquals(new Int(1), f.arities().get(0));
    }
    @Test
    public void arities4Test() {
        IFn dummy = null;
        Vector params = new Vector();
        params.conj(new Symbol("x"));
        params.conj(new Symbol("y"));
        params.conj(new Symbol("z"));
        Fun f = new Fun(dummy,  params);
        assertEquals(new Int(3), f.arities().get(0));
    }
    @Test
    public void cloneTest1() {
        Seq body = new Seq();
        body.conj(Literal.T);
        Vector params = new Vector();
        Fun constantlyTrue = new Fun((args) -> Literal.T, params, body);
        Fun clown = new Fun(constantlyTrue);
        assertTrue(clown.equals(constantlyTrue));
    }
    /*
    @Test
    public void cloneTest2() {
        Seq body = new Seq();
        body.conj(Literal.T);
        Vector params = new Vector();
        Fun constantlyTrue = new Fun((args) -> Literal.T, params);
        Fun clown = new Fun(constantlyTrue.f, constantlyTrue.params, body);
        assertTrue(clown.equals(constantlyTrue));
    }
    @Test
    public void hasSameImplementationTest1() {
        Seq body = new Seq();
        body.conj(Literal.T);
        Vector params = new Vector();
        Fun constantlyTrue = new Fun((args) -> Literal.T, params);
        Fun clown = new Fun(constantlyTrue.f, constantlyTrue.params, body);
        assertTrue(clown.hasSameImplementation(constantlyTrue));
    }
    */

    @Test
    public void toStringTest1() {
        String expected = "(fn* [] (+ 1 2))";
        Int x = new Int(1);
        Int y = new Int(2);
        Vector params = new Vector();
        Seq body = new Seq();
        body.conj(new Symbol("+"));
        body.conj(x);
        body.conj(y);
        Fun f = new Fun(null, params, body);
        assertEquals(expected, f.toString());
    }

    @Test
    public void toStringTest2() {
        String expected = "(fn* [x y] (+ x y))";
        Symbol x = new Symbol("x");
        Symbol y = new Symbol("y");
        Vector params = new Vector();
        params.conj(x);
        params.conj(y);
        Seq body = new Seq();
        body.conj(new Symbol("+"));
        body.conj(x);
        body.conj(y);
        Fun f = new Fun(null, params, body);
        assertEquals(expected, f.toString());
    }
}
