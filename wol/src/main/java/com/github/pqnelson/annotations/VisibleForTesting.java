package com.github.pqnelson.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is an annotation to make a private method testable.
 *
 * <p>Guava inspired this, though they used it for purely
 * documentation purposes.</p>
 *
 * <p>Right now, this is only for documentation purposes. It would be
 * awesome if I could hack annotation processing to make this {@code
 * private} when compiling for production and {@code public} when
 * compiling for testing.</p>
 *
 * @see <a href="https://github.com/google/guava/blob/master/guava/src/com/google/common/annotations/VisibleForTesting.java">VisibleForTesting.java</a>
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface VisibleForTesting {
}