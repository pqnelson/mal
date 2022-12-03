package com.github.pqnelson.expr;

import com.github.pqnelson.Printer;

public class LispNoSuchMethodException extends LispException {
    public LispNoSuchMethodException(Throwable e) {
        super(e);
    }
}