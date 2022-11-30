package com.github.pqnelson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.github.pqnelson.expr.Expr;

/**
 * The basic interpreter.
 *
 * To run this, simply use Maven to execute:
 * <blockquote>{@code
 * $ mvn compile exec:java -Dexec.mainClass="com.github.pqnelson.App"
 * }</blockquote>
 *
 */
public class App
{
    private static final Printer printer = new Printer();
    private static String print(Expr e) {
        return e.accept(printer);
    }

    private static String rep(String line, Env env) throws Throwable {
        return print(Evaluator.eval(Reader.readString(line), env));
    }

    public static void main( String[] args )
    {
        printer.isReadable = true;
        Env env = Evaluator.initialEnv();
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader buf = new BufferedReader(input);
        boolean isFinished = false;
        while (!isFinished) {
            String line;
            System.out.print("> ");
            try {
                line = buf.readLine();
                if (null == line || line.equals("")) {
                    isFinished = true;
                } else {
                    System.out.println(rep(line, env));
                }
            } catch (Throwable e) {
                System.err.println("Oh now, throwable thrown! I am calling it quits!");
                System.err.println(e.getMessage());
                e.printStackTrace(System.err);
                isFinished = true;
            }
        }
        try {
            buf.close();
        } catch (IOException e) {
        }
    }
}