package com.github.pqnelson.expr;

public class LispThrowable extends Exception {
    public LispThrowable() {
        super();
    }
    public LispThrowable(String msg) {
        super(msg);
    }
    public LispThrowable(Throwable cause) {
        super(cause);
    }
}
