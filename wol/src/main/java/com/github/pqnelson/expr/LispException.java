package com.github.pqnelson.expr;

import com.github.pqnelson.Printer;

public class LispException extends LispThrowable {
    String message;
    public LispException(Expr value) {
        Printer printer = new Printer(true);
        this.message = value.accept(printer);
    }
    public LispException(String message) {
        this.message = message;
    }
    public LispException(Throwable cause) {
        super(cause);
    }
    @Override
    public String getMessage() {
        return this.message;
    }
}