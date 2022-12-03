package com.github.pqnelson.expr;

import com.github.pqnelson.Printer;

public class LispError extends LispThrowable {
    String message;
    public LispError(Expr value) {
        Printer printer = new Printer(true);
        this.message = value.accept(printer);
    }
    public LispError(String message) {
        super(message);
        this.message = message;
    }
    @Override
    public String getMessage() {
        return this.message;
    }
}