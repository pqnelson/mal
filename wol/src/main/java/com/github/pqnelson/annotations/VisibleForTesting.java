package com.github.pqnelson.annotations;

/**
 * This is an annotation to make a private method testable.
 *
 * Right now, this is only for documentation purposes.
 *
 * Guava inspired this, though they also used it for purely documentation purposes.
 *
 * @see {@link https://github.com/google/guava/blob/master/guava/src/com/google/common/annotations/VisibleForTesting.java}
 */
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface VisibleForTesting {
}