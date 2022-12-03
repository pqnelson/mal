package com.github.pqnelson.expr;

import com.github.pqnelson.Printer;

public class LispIllegalArgumentException extends LispException {
    public LispIllegalArgumentException(String message) {
        super(message);
    }
}