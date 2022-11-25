package com.github.pqnelson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


public class PrinterTest {
    Printer printer = new Printer();

    @Nested
    class PrintNumberTests {
        @Test
        public void printFloatingPointNumberTest1() {
            String lexeme = "3.14159";
            Expr e = Reader.readString(lexeme);
            assertEquals(lexeme, printer.print(e));
        }
        @Test
        public void printFloatingPointNumberTest2() {
            String lexeme = "1.618033988749";
            Expr e = Reader.readString(lexeme);
            assertEquals(lexeme, printer.print(e));
        }
        @Test
        public void printFloatingPointNumberTest3() {
            String lexeme = "-2.236067977499";
            Expr e = Reader.readString(lexeme);
            assertEquals(lexeme, printer.print(e));
        }
        @Test
        public void printFloatingPointNumberTest4() {
            String lexeme = "1.732050807568";
            Expr e = Reader.readString(lexeme);
            assertEquals(lexeme, printer.print(e));
        }
        @Test
        public void printHexadecimalNumberTest1() {
            String lexeme = "0xDEADBEEF";
            Expr e = Reader.readString(lexeme);
            assertEquals(lexeme, printer.print(e));
        }
        @Test
        public void printHexadecimalNumberTest2() {
            String lexeme = "0xF01DE401";
            Expr e = Reader.readString(lexeme);
            assertEquals(lexeme, printer.print(e));
        }
        @Test
        public void printHexadecimalNumberTest3() {
            String lexeme = "0xDEADBEEFn";
            Expr e = Reader.readString(lexeme);
            assertEquals(lexeme, printer.print(e));
        }
        @Test
        public void printHexadecimalNumberTest4() {
            String lexeme = "0xF01DE401n";
            Expr e = Reader.readString(lexeme);
            assertEquals(lexeme, printer.print(e));
        }
        @Test
        public void printOctalNumberTest1() {
            String lexeme = "0112";
            Expr e = Reader.readString(lexeme);
            assertEquals(lexeme, printer.print(e));
        }
        @Test
        public void printOctalNumberTest2() {
            String lexeme = "0o311037552"; // ~ pi
            Expr e = Reader.readString(lexeme);
            assertEquals(lexeme, printer.print(e));
        }
        @Test
        public void printOctalNumberTest3() {
            String lexeme = "0O217067363"; // ~ sqrt(5)
            Expr e = Reader.readString(lexeme);
            assertEquals(lexeme, printer.print(e));
        }
        @Test
        public void printOctalNumberTest4() {
            String lexeme = "0112n";
            Expr e = Reader.readString(lexeme);
            assertEquals(lexeme, printer.print(e));
        }
        @Test
        public void printOctalNumberTest5() {
            String lexeme = "0o311037552n"; // ~ pi
            Expr e = Reader.readString(lexeme);
            assertEquals(lexeme, printer.print(e));
        }
        @Test
        public void printOctalNumberTest6() {
            String lexeme = "0O217067363n"; // ~ sqrt(5)
            Expr e = Reader.readString(lexeme);
            assertEquals(lexeme, printer.print(e));
        }
    }

    @Nested
    class PrintStringTests {
        @Test
        public void printStringTest() {
            String lexeme = "\"I am a happy string\"";
            Expr e = Reader.readString(lexeme);
            assertEquals(lexeme.substring(1, lexeme.length()-1), printer.print(e));
        }
    }
    @Nested
    class PrintVectorTests {
        @Test
        public void printVectorTest1() {
            String lexeme = "[1 2 3]";
            Expr e = Reader.readString(lexeme);
            assertEquals(lexeme, printer.print(e));
        }
        @Test
        public void printVectorTest2() {
            String lexeme = "[1, 2, 3]";
            Expr e = Reader.readString(lexeme);
            assertEquals("[1 2 3]", printer.print(e));
        }
    }
}
