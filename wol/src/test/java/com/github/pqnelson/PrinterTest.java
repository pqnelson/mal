package com.github.pqnelson;

import org.apache.commons.text.StringEscapeUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.BigInt;
import com.github.pqnelson.expr.Float;
import com.github.pqnelson.expr.Fun;
import com.github.pqnelson.expr.Int;
import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Str;
import com.github.pqnelson.expr.Symbol;
import com.github.pqnelson.expr.Vector;

public class PrinterTest {
    Printer printer = new Printer();

    @Nested
    class PrintNumberTests {
        @Test
        public void printFloatingPointNumberTest1() {
            String lexeme = "3.14159";
            ReadTable reader = new ReadTable(lexeme);
            Expr e = reader.read();
            assertEquals(lexeme, printer.print(e));
        }
        @Test
        public void printFloatingPointNumberTest2() {
            String lexeme = "1.618033988749";
            ReadTable reader = new ReadTable(lexeme);
            Expr e = reader.read();
            assertEquals(lexeme, printer.print(e));
        }
        @Test
        public void printFloatingPointNumberTest3() {
            String lexeme = "-2.236067977499";
            ReadTable reader = new ReadTable(lexeme);
            Expr e = reader.read();
            assertEquals(lexeme, printer.print(e));
        }
        @Test
        public void printFloatingPointNumberTest4() {
            String lexeme = "1.732050807568";
            ReadTable reader = new ReadTable(lexeme);
            Expr e = reader.read();
            assertEquals(lexeme, printer.print(e));
        }
        @Test
        public void printHexadecimalNumberTest1() {
            String lexeme = "0xDEADBEEF";
            ReadTable reader = new ReadTable(lexeme);
            Expr e = reader.read();
            assertEquals(String.valueOf(0xDEADBEEFL), printer.print(e));
        }
        @Test
        public void printHexadecimalNumberTest2() {
            String lexeme = "0xF01DE401";
            ReadTable reader = new ReadTable(lexeme);
            Expr e = reader.read();
            assertEquals(String.valueOf(0xF01DE401L), printer.print(e));
        }
        @Test
        public void printHexadecimalNumberTest3() {
            String lexeme = "0xDEADBEEFn";
            ReadTable reader = new ReadTable(lexeme);
            Expr e = reader.read();
            assertEquals(String.valueOf(0xDEADBEEFL), printer.print(e));
        }
        @Test
        public void printHexadecimalNumberTest4() {
            String lexeme = "0xF01DE401n";
            ReadTable reader = new ReadTable(lexeme);
            Expr e = reader.read();
            assertEquals(String.valueOf(0xF01DE401L), printer.print(e));
        }
        @Test
        public void printOctalNumberTest1() {
            String lexeme = "0112";
            ReadTable reader = new ReadTable(lexeme);
            Expr e = reader.read();
            assertEquals(String.valueOf(0112), printer.print(e));
        }
        @Test
        public void printOctalNumberTest2() {
            String lexeme = "0o311037552"; // ~ pi
            ReadTable reader = new ReadTable(lexeme);
            Expr e = reader.read();
            assertEquals(String.valueOf(0311037552L), printer.print(e));
        }
        @Test
        public void printOctalNumberTest3() {
            String lexeme = "0O217067363"; // ~ sqrt(5)
            ReadTable reader = new ReadTable(lexeme);
            Expr e = reader.read();
            assertEquals(String.valueOf(0217067363L), printer.print(e));
        }
        @Test
        public void printOctalNumberTest4() {
            String lexeme = "0112n";
            ReadTable reader = new ReadTable(lexeme);
            Expr e = reader.read();
            assertEquals(String.valueOf(0112L), printer.print(e));
        }
        @Test
        public void printOctalNumberTest5() {
            String lexeme = "0o311037552n"; // ~ pi
            ReadTable reader = new ReadTable(lexeme);
            Expr e = reader.read();
            assertEquals(String.valueOf(0311037552L), printer.print(e));
        }
        @Test
        public void printOctalNumberTest6() {
            String lexeme = "0O217067363n"; // ~ sqrt(5)
            ReadTable reader = new ReadTable(lexeme);
            Expr e = reader.read();
            assertEquals(String.valueOf(0217067363L), printer.print(e));
        }
    }

    @Nested
    class PrintStringTests {
        @Test
        public void printStringTest() {
            String s = "I am a\t \"happy\" string";
            String lexeme = "\""+StringEscapeUtils.escapeJava(s)+"\"";
            String result = (StringEscapeUtils.escapeJava(lexeme)).substring(1);
            result = result.substring(0, result.length()-1);
            Expr expected = new Str(s);
            ReadTable reader = new ReadTable(lexeme);
            Expr e = reader.read();
            assertEquals(expected, e);
        }
        @Test
        public void printStringTest2() {
            String lexeme = "\"I am a\t \\\"happy\\\" string\"";
            String result = "I am a\\t \\\"happy\\\" string";
            ReadTable reader = new ReadTable(lexeme);
            Expr e = reader.read();
            assertEquals(result, printer.print(e, true));
        }
        @Test
        public void printStringTest3() {
            String lexeme = "\"I am a\t \\\"happy\\\" string\"";
            ReadTable reader = new ReadTable(lexeme);
            Expr e = reader.read();
            assertEquals(e.toString(), printer.print(e));
        }
    }
    @Nested
    class PrintVectorTests {
        @Test
        public void printVectorTest1() {
            String lexeme = "[1, 2, 3]";
            ReadTable reader = new ReadTable(lexeme);
            Expr e = reader.read();
            assertEquals("[1.0 2.0 3.0]", printer.print(e));
        }
    }
}
