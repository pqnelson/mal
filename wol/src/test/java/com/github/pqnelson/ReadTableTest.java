package com.github.pqnelson;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ReadTableTest {
    @Test
    public void emptyStringTest() {
        ReadTable r = new ReadTable("    \t\n     ");
        assertEquals(null, r.read());
    }

    @Test
    public void nonemptyStringTest() {
        ReadTable r = new ReadTable("foo    spam\t\n     ");
        assertEquals("foo", r.read());
        assertEquals("spam", r.read());
    }

    @Test
    public void listTest() {
        ReadTable r = new ReadTable("(foo    spam\t\n     )");
        ArrayList<String> expected = new ArrayList<>();
        expected.add("foo");
        expected.add("spam");
        assertEquals(expected, r.read());
    }

    @Test
    public void nestedListTest() {
        ReadTable r = new ReadTable("(foo (eggs but) and spam)");
        ArrayList<Object> expected = new ArrayList<>();
        ArrayList<Object> tmp = new ArrayList<>();
        tmp.add("eggs");
        tmp.add("but");
        expected.add("foo");
        expected.add(tmp);
        expected.add("and");
        expected.add("spam");
        assertEquals(expected, r.read());
    }

    @Test
    public void nestedNestedListTest() {
        ReadTable r = new ReadTable("(foo (eggs (scrambed (stuff) suggests) but) and spam)");
        ArrayList<Object> expected = new ArrayList<>();
        ArrayList<Object> tmp = new ArrayList<>();
        ArrayList<Object> inner = new ArrayList<>();
        inner.add("stuff");
        tmp.add("scrambed");
        tmp.add(inner);
        tmp.add("suggests");
        inner = tmp;
        tmp = new ArrayList<>();
        tmp.add("eggs");
        tmp.add(inner);
        tmp.add("but");
        expected = new ArrayList<>();
        expected.add("foo");
        expected.add(tmp);
        expected.add("and");
        expected.add("spam");
        assertEquals(expected, r.read());
    }
}
