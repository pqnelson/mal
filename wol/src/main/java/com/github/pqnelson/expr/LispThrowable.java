package com.github.pqnelson.expr;

import com.github.pqnelson.Printer;

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
/*
public class LispException extends LispThrowable {
    Str message;
    public LispException(Expr value) {
        Printer printer = new Printer(true);
        this.message = new Str(value.accept(printer));
    }
    public LispException(String message) {
        this.value = new Str(message);
    }
    public LispException(Throwable cause) {
        super(cause);
    }
    public Expr getMessage() {
        return this.message;
    }
}

public class LispError extends LispThrowable {
    Str message;
    public LispError(Expr value) {
        Printer printer = new Printer(true);
        this.message = new Str(value.accept(printer));
    }
    public LispError(String message) {
        this.value = new Str(message);
    }
    public Expr getMessage() {
        return this.message;
    }
}

public class LispIOException extends LispException {
    public LispIOException(Throwable cause) {
        super(cause);
    }
}

public class LispIllegalArgumentException extends LispException {
}
*/