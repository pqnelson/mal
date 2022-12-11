package com.github.pqnelson.js;

public interface PrimitiveValue {

    public default boolean isFalsy() {
        return false;
    }

    public default boolean isTruthy() {
        return !this.isFalsy();
    }
}