package com.github.pqnelson;

import org.apache.commons.text.StringEscapeUtils;

import java.math.BigInteger;

import java.util.ArrayList;
import java.util.InputMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.pqnelson.expr.BigInt;
import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.Int;
import com.github.pqnelson.expr.Float;
import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Str;
import com.github.pqnelson.expr.Symbol;
import com.github.pqnelson.expr.Vector;

public class ReadTableTest {
    @Test
    public void emptyStringTest() {
        ReadTable r = new ReadTable("    \t\n     ");
        assertEquals(null, r.read());
    }

    @Nested
    public class ReadListTests {
        @Test
        public void nonemptyStringTest() {
            ReadTable r = new ReadTable("foo    spam\t\n     ");
            assertEquals(new Symbol("foo"), r.read());
            assertEquals(new Symbol("spam"), r.read());
        }

        @Test
        public void listTest() {
            ReadTable r = new ReadTable("(foo    spam\t\n     )");
            ArrayList<Expr> expected = new ArrayList<>();
            expected.add(new Symbol("foo"));
            expected.add(new Symbol("spam"));
            assertEquals(new Seq(expected), r.read());
        }

        @Test
        public void nestedListTest() {
            ReadTable r = new ReadTable("(foo (eggs but) and spam)");
            ArrayList<Expr> expected = new ArrayList<>();
            ArrayList<Expr> tmp = new ArrayList<>();
            tmp.add(new Symbol("eggs"));
            tmp.add(new Symbol("but"));
            expected.add(new Symbol("foo"));
            expected.add(new Seq(tmp));
            expected.add(new Symbol("and"));
            expected.add(new Symbol("spam"));
            assertEquals(new Seq(expected), r.read());
        }

        @Test
        public void nestedNestedListTest() {
            ReadTable r = new ReadTable("(foo (eggs (scrambed (stuff) suggests) but) and spam)");
            ArrayList<Expr> expected = new ArrayList<>();
            ArrayList<Expr> tmp = new ArrayList<>();
            ArrayList<Expr> inner = new ArrayList<>();
            inner.add(new Symbol("stuff"));
            tmp.add(new Symbol("scrambed"));
            tmp.add(new Seq(inner));
            tmp.add(new Symbol("suggests"));
            inner = tmp;
            tmp = new ArrayList<>();
            tmp.add(new Symbol("eggs"));
            tmp.add(new Seq(inner));
            tmp.add(new Symbol("but"));
            expected = new ArrayList<>();
            expected.add(new Symbol("foo"));
            expected.add(new Seq(tmp));
            expected.add(new Symbol("and"));
            expected.add(new Symbol("spam"));
            assertEquals(new Seq(expected), r.read());
        }
    }

    @Nested
    public class QuoteTests {
        @Test
        public void readQuoteTest() {
            ReadTable r = new ReadTable("'foo");
            Seq expected = new Seq();
            expected.conj(Symbol.QUOTE);
            expected.conj(new Symbol("foo"));
            assertEquals(expected, r.read());
        }

        @Test
        public void quotedListTest() {
            ReadTable r = new ReadTable("'(foo    spam\t\n     )");
            ArrayList<Expr> coll = new ArrayList<>();
            coll.add(new Symbol("foo"));
            coll.add(new Symbol("spam"));
            Seq expected = new Seq();
            expected.conj(Symbol.QUOTE);
            expected.conj(new Seq(coll));
            assertEquals(expected, r.read());
        }
    }

    @Nested
    public class ReadVector {
        @Test
        public void readVectorTest() {
            ReadTable r = new ReadTable("[foo    spam\t\n     ]");
            ArrayList<Expr> coll = new ArrayList<>();
            coll.add(new Symbol("foo"));
            coll.add(new Symbol("spam"));
            Vector expected = new Vector(coll);
            assertEquals(expected, r.read());
        }
    }

    @Test
    public void readKeyword() {
        ReadTable r = new ReadTable("    :spam\t\n     ");
        Expr expected = new Keyword("spam");
        assertEquals(expected, r.read());
    }

    @Nested
    public class ReadLiteralTests {
        @Test
        public void readDefTest() {
            ReadTable r = new ReadTable("def");
            Expr expected = Symbol.DEF;
            assertEquals(expected, r.read());
        }
        @Test
        public void readDefmacroTest() {
            ReadTable r = new ReadTable("defmacro");
            Expr expected = Symbol.DEFMACRO;
            assertEquals(expected, r.read());
        }
        @Test
        public void readDoTest() {
            ReadTable r = new ReadTable("do");
            Expr expected = Symbol.DO;
            assertEquals(expected, r.read());
        }
        @Test
        public void readFalseTest() {
            ReadTable r = new ReadTable("false");
            Expr expected = Literal.F;
            assertEquals(expected, r.read());
        }
        @Test
        public void readFnTest() {
            ReadTable r = new ReadTable("fn*");
            Expr expected = Symbol.FN_STAR;
            assertEquals(expected, r.read());
        }
        @Test
        public void readIfTest() {
            ReadTable r = new ReadTable("if");
            Expr expected = Symbol.IF;
            assertEquals(expected, r.read());
        }
        @Test
        public void readTrueTest() {
            ReadTable r = new ReadTable("true");
            Expr expected = Literal.T;
            assertEquals(expected, r.read());
        }
        @Test
        public void readNilTest() {
            ReadTable r = new ReadTable("nil");
            Expr expected = Literal.NIL;
            assertEquals(expected, r.read());
        }
        @Test
        public void readTryTest() {
            ReadTable r = new ReadTable("try");
            Expr expected = Symbol.TRY;
            assertEquals(expected, r.read());
        }
        @Test
        public void readCatchTest() {
            ReadTable r = new ReadTable("catch");
            Expr expected = Symbol.CATCH;
            assertEquals(expected, r.read());
        }
    }

    @Nested
    public class CharTests {
        @Test
        public void readNewlineTest() {
            ReadTable r = new ReadTable("\\newline");
            Expr expected = Literal.Char('\n');
            assertEquals(expected, r.read());
        }
        @Test
        public void readSpaceTest() {
            ReadTable r = new ReadTable("\\space");
            Expr expected = Literal.Char(' ');
            assertEquals(expected, r.read());
        }
        @Test
        public void readTabTest() {
            ReadTable r = new ReadTable("\\tab");
            Expr expected = Literal.Char('\t');
            assertEquals(expected, r.read());
        }
        @Test
        public void readBackspaceTest() {
            ReadTable r = new ReadTable("\\backspace");
            Expr expected = Literal.Char('\b');
            assertEquals(expected, r.read());
        }
        @Test
        public void readFormfeedTest() {
            ReadTable r = new ReadTable("\\formfeed");
            Expr expected = Literal.Char('\f');
            assertEquals(expected, r.read());
        }
        @Test
        public void readReturnTest() {
            ReadTable r = new ReadTable("\\return");
            Expr expected = Literal.Char('\r');
            assertEquals(expected, r.read());
        }
        @ParameterizedTest
        @ValueSource(strings = {"!", "\"", "#", "$", "%", "&", "'", "(", ")",
                                "*", "+", ",", "-", ".", "/", "0", "1", "2",
                                "3", "4", "5", "6", "7", "8", "9", ":", ";",
                                "<", "=", ">", "?", "@", "[", "\\", "]", "^",
                                "_", "`", "{", "|", "}", "a", "b", "c", "d",
                                "e", "f", "g", "h", "i", "j", "k", "l", "m",
                                "n", "o", "p", "q", "r", "s", "t", "u", "v",
                                "w", "x", "y", "z", "A", "B", "C", "D", "E",
                                "F", "G", "H", "I", "J", "K", "L", "M", "N",
                                "O", "P", "Q", "R", "S", "T", "U", "V", "W",
                                "X", "Y", "Z"})
        public void singleStringCharTest(String token) {
            ReadTable r = new ReadTable("\\"+token);
            Expr expected = Literal.Char(token.charAt(0));
            assertEquals(expected, r.read());
        }
        @ParameterizedTest
        @ValueSource(strings = {"!", "\"", "#", "$", "%", "&", "'", "(", ")",
                                "*", "+", ",", "-", ".", "/", "0", "1", "2",
                                "3", "4", "5", "6", "7", "8", "9", ":", ";",
                                "<", "=", ">", "?", "@", "[", "\\", "]", "^",
                                "_", "`", "{", "|", "}", "a", "b", "c", "d",
                                "e", "f", "g", "h", "i", "j", "k", "l", "m",
                                "n", "o", "p", "q", "r", "s", "t", "u", "v",
                                "w", "x", "y", "z", "A", "B", "C", "D", "E",
                                "F", "G", "H", "I", "J", "K", "L", "M", "N",
                                "O", "P", "Q", "R", "S", "T", "U", "V", "W",
                                "X", "Y", "Z"})
        public void unicodeCharTest(String token) {
            ReadTable r = new ReadTable("\\u00"+Integer.toHexString(Character.codePointAt(token,0)));
            Expr expected = Literal.Char(token.charAt(0));
            assertEquals(expected, r.read());
        }

        @Test
        public void surrogateFail1Test() {
            ReadTable r = new ReadTable("\\uD800");
            assertThrows(RuntimeException.class,
                         () -> r.read());
        }

        @Test
        public void surrogateFail2Test() {
            ReadTable r = new ReadTable("\\uDFFF");
            assertThrows(RuntimeException.class,
                         () -> r.read());
        }
    }

    @Nested
    public class ReadNumberTests {
        @Test
        public void readZeroTest() {
            final String lexeme = "0";
            ReadTable r = new ReadTable(lexeme);
            Expr expected = new Float(0.0);
            assertEquals(expected, r.read());
        }
        @Test
        public void readZeroAsIntTest() {
            final String lexeme = "0";
            ReadTable r = new ReadTable(lexeme);
            r.preferParsingNumbersAsFloats = false;
            Expr expected = new Int(0L);
            assertEquals(expected, r.read());
        }
        @Test
        public void readZeroWithSpaceTest() {
            final String lexeme = " 0 ";
            ReadTable r = new ReadTable(lexeme);
            Expr expected = new Float(0.0);
            assertEquals(expected, r.read());
        }
        @Test
        public void readOneTest() {
            final String lexeme = "1";
            ReadTable r = new ReadTable(lexeme);
            Expr expected = new Float(1.0);
            assertEquals(expected, r.read());
        }
        @Nested
        class floatTests {
            @Test
            public void readPiTest() {
                final String lexeme = "3.14159265359";
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Float(Double.parseDouble(lexeme));
                assertEquals(expected, r.read());
            }

            @Test
            public void scanFloatNumber1Test() {
                String lexeme = "0.36787944117"; // exp(-1)
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Float(Double.parseDouble(lexeme));
                assertEquals(expected, r.read());
            }

            @Test
            public void scanFloatNumber2Test() {
                String lexeme = "-0.36787944117"; // -exp(-1)
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Float(Double.parseDouble(lexeme));
                assertEquals(expected, r.read());
            }

            @Test
            public void scanFloatNumber3Test() {
                String lexeme = "123e45";
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Float(Double.parseDouble(lexeme));
                assertEquals(expected, r.read());
            }

            @Test
            public void scanFloatNumber4Test() {
                String lexeme = "123e-45";
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Float(Double.parseDouble(lexeme));
                assertEquals(expected, r.read());
            }

            @Test
            public void scanFloatNumber5Test() {
                String lexeme = "-123e-45";
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Float(Double.parseDouble(lexeme));
                assertEquals(expected, r.read());
            }

            @Test
            public void scanFloatNumber6Test() {
                String lexeme = "-123e45";
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Float(Double.parseDouble(lexeme));
                assertEquals(expected, r.read());
            }

            @Test
            public void scanFloatNumber7Test() {
                String lexeme = "0.123";
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Float(Double.parseDouble(lexeme));
                assertEquals(expected, r.read());
            }

            @Test
            public void scanFloatNumber8Test() {
                String lexeme = "123.456";
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Float(Double.parseDouble(lexeme));
                assertEquals(expected, r.read());
            }

            @Test
            public void scanFloatNumber9Test() {
                String lexeme = "-123.456";
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Float(Double.parseDouble(lexeme));
                assertEquals(expected, r.read());
            }

            @Test
            public void scanThreeE5AsNumberTest() {
                String lexeme = "3e5";
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Float(Double.parseDouble(lexeme));
                assertEquals(expected, r.read());
            }

            @ParameterizedTest
            @ValueSource(strings = {"0.36787944117", "-0.36787944117",
                                    "0.123", "123.456", "-0.123", "-123.456",
                                    "123e45", "123e-45", "-123e45", "-123e-45"})
            public void readLexemeAsFloatTest(String lexeme) {
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Float(Double.parseDouble(lexeme));
                assertEquals(expected, r.read());
            }
        }

        @Nested
        class octalTests {
            @Test
            public void scanOctalNumber1Test() {
                String lexeme = "015";
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Int(Long.parseLong(lexeme, 8));
                assertEquals(expected, r.read());
            }

            @Test
            public void scanOctalNumber2Test() {
                String lexeme = "0001";
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Int(Long.parseLong(lexeme, 8));
                assertEquals(expected, r.read());
            }
            @Test
            public void scanOctalNumber3Test() {
                String lexeme = "0o7777777777n";
                ReadTable r = new ReadTable(lexeme);
                BigInteger v
                    = new BigInteger(lexeme.substring(2, lexeme.length()-1), 8);
                Expr expected = new BigInt(v);
                assertEquals(expected, r.read());
            }
            @Test
            public void scanOctalNumber4Test() {
                String lexeme = "0o7654x";
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Symbol(lexeme);
                assertEquals(expected, r.read());
            }

            @Test
            public void scanOctalNumber5Test() {
                String lexeme = "015n";
                ReadTable r = new ReadTable(lexeme);
                BigInteger v = new BigInteger(lexeme.substring(1, 3), 8);
                Expr expected = new BigInt(v);
                assertEquals(expected, r.read());
            }

            @ParameterizedTest
            @ValueSource(strings = {"015", "-015", "+015", "001", "-001", "+001",
                                    "0o777", "-0o777", "+0o777",
                                    "0O7654", "-0O7654", "+0O7654",
                                    "0o54321", "-0o54321", "+0o54321",
                                    "000321", "-000321", "+000321"})
            public void readLexemeAsOctalTest(String lexeme) {
                ReadTable r = new ReadTable(lexeme);
                Expr expected
                    = new Int(Long.decode(lexeme.toLowerCase()
                                                .replace("o", "")));
                assertEquals(expected, r.read());
            }
        }

        @Nested
        class hexadecimalTests {
            @Test
            public void readDeadBeefTest() {
                ReadTable r = new ReadTable("0xDEADBEEF");
                Expr expected = new Int(Long.decode("0xDEADBEEF"));
                assertEquals(expected, r.read());
            }
            @Test
            public void scanHexadecimalNumber1Test() {
                String lexeme = "0x1123";
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Int(Long.parseLong(lexeme.substring(2), 16));
                assertEquals(expected, r.read());
            }

            @Test
            public void scanHexadecimalNumber2Test() {
                String lexeme = "0x00111";
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Int(Long.parseLong(lexeme.substring(2), 16));
                assertEquals(expected, r.read());
            }

            // failures
            @Test
            public void scanHexadecimalNumber3Test() {
                String lexeme = "0x123456789ABCDEFn";
                ReadTable r = new ReadTable(lexeme);
                BigInteger v = new BigInteger("123456789ABCDEF", 16);
                Expr expected = new BigInt(v);
                assertEquals(expected, r.read());
            }

            @Test
            public void scanHexadecimalNumber4Test() {
                String lexeme = "-0x123456789ABCDEFn";
                ReadTable r = new ReadTable(lexeme);
                BigInteger v = new BigInteger("123456789ABCDEF", 16);
                Expr expected = new BigInt(v.negate());
                assertEquals(expected, r.read());
            }

            @ParameterizedTest
            @ValueSource(strings = {"0x15", "-0x15", "+0x15",
                                    "0xb", "0Xb", "0xB", "0XB",
                                    "-0xb", "-0Xb", "-0xB", "-0XB",
                                    "+0xb", "+0Xb", "+0xB", "+0XB",
                                    "0X10", "-0X10", "+0X10",
                                    "-0x123", "0xfeaf", "-0Xfeaf",
                                    "-0XFEAF", "0XFeAf", "+0Xfeaf"})
            public void readLexemeAsHexadecimalTest(String lexeme) {
                ReadTable r = new ReadTable(lexeme);
                Expr expected
                    = new Int(Long.decode(lexeme));
                assertEquals(expected, r.read());
            }
        }

        @Nested
        public class FauxOctalTests {
            @Test
            public void tryScanningOctal1Test() {
                final String lexeme = "0123987";
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Int(Long.parseLong(lexeme));
                assertEquals(expected, r.read());
            }

            @Test
            public void tryScanningInt2Test() {
                final String lexeme = "1234e5";
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Float(Double.parseDouble(lexeme));
                assertEquals(expected, r.read());
            }

            @Test
            public void fauxOctalTest() {
                final String lexeme = "05678";
                ReadTable r = new ReadTable(lexeme);
                Expr expected = new Int(Long.parseLong(lexeme));
                assertEquals(expected, r.read());
            }
        }
    }

    @Nested
    class StringTokenizationTests {
        @Test
        public void string1Test() {
            String lexeme = "\"This is a happy string\"";
            ReadTable r = new ReadTable(lexeme);
            Expr expected = new Str(lexeme.substring(1, lexeme.length()-1));
            assertEquals(expected, r.read());
        }

        @Test
        public void unterminatedStringTest() {
            String lexeme = "\"This is an unhappy string";
            ReadTable r = new ReadTable(lexeme);
            Expr expected = new Str(lexeme.substring(1, lexeme.length()-1));
            InputMismatchException e = assertThrows(InputMismatchException.class,
                                                    () -> r.read());
            assertEquals("Line [1,1]: Unterminated string", e.getMessage());
        }

        @Test
        public void string2Test() {
            String naive = "{:abc \"val1\" :def \"val2\"}";
            String expectedToken = StringEscapeUtils.escapeJava(naive);
            String lexeme = "\""+expectedToken+"\"";
            ReadTable r = new ReadTable(lexeme);
            Expr expected = new Str(naive);
            assertEquals(expected, r.read());
        }
        @Test
        public void string3Test() {
            String naive = "{:abc \"val1\" :def \"val2\"}";
            String expectedToken = StringEscapeUtils.escapeJava(naive);
            String lexeme = "\n\""+expectedToken+"\"";
            ReadTable r = new ReadTable(lexeme);
            Expr expected = new Str(naive);
            assertEquals(expected, r.read());
        }
    }

    @Nested
    public class ArithmeticListTest {
        @Test
        public void zeroMinusOneTest() {
            final String lexeme = "(- 0 1)";
            ReadTable r = new ReadTable(lexeme);
            Seq expected = new Seq();
            expected.conj(new Symbol("-"));
            expected.conj(new Float(0.0));
            expected.conj(new Float(1.0));
            assertEquals(expected, r.read());
        }

        @Test
        public void zeroMinusOneCar1Test() {
            final String lexeme = "(- 0 1)";
            ReadTable r = new ReadTable(lexeme);
            Seq expected = new Seq();
            expected.conj(new Symbol("-"));
            expected.conj(new Float(0.0));
            expected.conj(new Float(1.0));
            assertEquals(expected.get(0), ((Seq) r.read()).get(0));
        }

        @Test
        public void zeroMinusOneCar2Test() {
            final String lexeme = "(- 0 1)";
            ReadTable r = new ReadTable(lexeme);
            Seq expected = new Seq();
            expected.conj(new Symbol("-"));
            expected.conj(new Float(0.0));
            expected.conj(new Float(1.0));
            assertEquals(expected.get(1), ((Seq) r.read()).get(1));
        }

        @Test
        public void zeroMinusOneCar3Test() {
            final String lexeme = "(- 0 1)";
            ReadTable r = new ReadTable(lexeme);
            Seq expected = new Seq();
            expected.conj(new Symbol("-"));
            expected.conj(new Int(0));
            expected.conj(new Float(1.0));
            assertEquals(expected.get(2), ((Seq) r.read()).get(2));
        }
    }

    @Nested
    public class SeqTests {
        @Test
        public void readFnTest() {
            final String lexeme = "(fn* [] stuff)";
            ReadTable r = new ReadTable(lexeme);
            Seq expected = new Seq();
            expected.conj(Symbol.FN_STAR);
            expected.conj(new Vector());
            expected.conj(new Symbol("stuff"));
            assertEquals(expected, r.read());
        }
        @Test
        public void readIfTest() {
            final String lexeme = "(if test true-branch false-branch)";
            ReadTable r = new ReadTable(lexeme);
            Seq expected = new Seq();
            expected.conj(Symbol.IF);
            expected.conj(new Symbol("test"));
            expected.conj(new Symbol("true-branch"));
            expected.conj(new Symbol("false-branch"));
            assertEquals(expected, r.read());
        }
        @Test
        public void readLetStarTest() {
            final String lexeme = "(let* [x val] body)";
            ReadTable r = new ReadTable(lexeme);
            Seq expected = new Seq();
            expected.conj(Symbol.LET_STAR);
            Vector bindings = new Vector();
            bindings.conj(new Symbol("x"));
            bindings.conj(new Symbol("val"));
            expected.conj(bindings);
            expected.conj(new Symbol("body"));
            assertEquals(expected, r.read());
        }
        @Test
        public void readDoTest() {
            final String lexeme = "(do more stuff)";
            ReadTable r = new ReadTable(lexeme);
            Seq expected = new Seq();
            expected.conj(Symbol.DO);
            expected.conj(new Symbol("more"));
            expected.conj(new Symbol("stuff"));
            assertEquals(expected, r.read());
        }
        @Test
        public void booleanLiteralTest() {
            final String lexeme = "(true false)";
            ReadTable r = new ReadTable(lexeme);
            Seq expected = new Seq();
            expected.conj(Literal.T);
            expected.conj(Literal.F);
            assertEquals(expected, r.read());
        }
    }

    @Test
    public void commentTest() {
        final String lexeme = "(do more ;; comment here you know\n stuff)";
        ReadTable r = new ReadTable(lexeme);
        Seq expected = new Seq();
        expected.conj(Symbol.DO);
        expected.conj(new Symbol("more"));
        expected.conj(new Symbol("stuff"));
        assertEquals(expected, r.read());
    }

    @Nested
    public class ReadQuotesTests {
        @Test
        public void readQuotedListTest() {
            final String lexeme = "'(do more stuff)";
            ReadTable r = new ReadTable(lexeme);
            Seq inner = new Seq();
            inner.conj(Symbol.DO);
            inner.conj(new Symbol("more"));
            inner.conj(new Symbol("stuff"));
            Seq expected = new Seq();
            expected.conj(Symbol.QUOTE);
            expected.conj(inner);
            assertEquals(expected, r.read());
        }
        @Test
        public void readQuasiquotedListTest() {
            final String lexeme = "`(do more stuff)";
            ReadTable r = new ReadTable(lexeme);
            Seq inner = new Seq();
            inner.conj(Symbol.DO);
            inner.conj(new Symbol("more"));
            inner.conj(new Symbol("stuff"));
            Seq expected = new Seq();
            expected.conj(Symbol.QUASIQUOTE);
            expected.conj(inner);
            assertEquals(expected, r.read());
        }
        @Test
        public void readQuasiquotedListWithUnquoteTest() {
            final String lexeme = "`(do ~more stuff)";
            ReadTable r = new ReadTable(lexeme);
            Seq inner = new Seq();
            inner.conj(Symbol.DO);
            Seq unquoted = new Seq();
            unquoted.conj(Symbol.UNQUOTE);
            unquoted.conj(new Symbol("more"));
            inner.conj(unquoted);
            inner.conj(new Symbol("stuff"));
            Seq expected = new Seq();
            expected.conj(Symbol.QUASIQUOTE);
            expected.conj(inner);
            assertEquals(expected, r.read());
        }
        @Test
        public void readQuasiquotedListWithSpliceTest() {
            final String lexeme = "`(do ~@more stuff)";
            ReadTable r = new ReadTable(lexeme);
            Seq inner = new Seq();
            inner.conj(Symbol.DO);
            Seq unquoted = new Seq();
            unquoted.conj(Symbol.SPLICE);
            unquoted.conj(new Symbol("more"));
            inner.conj(unquoted);
            inner.conj(new Symbol("stuff"));
            Seq expected = new Seq();
            expected.conj(Symbol.QUASIQUOTE);
            expected.conj(inner);
            assertEquals(expected, r.read());
        }
        @Test
        public void backtickEmptyListeTest() {
            final String lexeme = "`()";
            ReadTable r = new ReadTable(lexeme);
            Seq inner = new Seq();
            Seq expected = new Seq();
            expected.conj(Symbol.QUASIQUOTE);
            expected.conj(inner);
            assertEquals(expected, r.read());
        }
    }
}
