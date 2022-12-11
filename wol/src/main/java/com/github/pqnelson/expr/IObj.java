package com.github.pqnelson.expr;

public interface IObj<E> extends IMeta, Cloneable {
    /**
     * Create a copy of {@code this} with new metadata supplied by the map,
     * unless the new metadata is equal to the existing metadata...then
     * {@code this} should be returned.
     */
    E withMeta(Map meta);
}
