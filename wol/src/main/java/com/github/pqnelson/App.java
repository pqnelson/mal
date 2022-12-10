package com.github.pqnelson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
public final class App {
    private static final Printer PRINTER = new Printer(true);

    private App() { }

    private static String print(final Expr e) {
        return PRINTER.printStr(e);
    }

    private static String rep(final String line, final Env env)
            throws Throwable {
        ReadTable reader = new ReadTable(line);
        return print(Evaluator.eval(reader.read(), env));
    }

    public static void main(final String[] args) {
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
                System.err.println("Oh now, throwable thrown!");
                System.err.println("I am calling it quits!");
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
