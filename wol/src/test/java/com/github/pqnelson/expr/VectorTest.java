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
}
