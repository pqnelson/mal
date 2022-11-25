- We prefix fields with `this.` in constructors and setter methods.
- Prefer `StringBuffer` over `StringBuilder`
- Prefer `List` over `Vector`
- `equals()` and `hashCode()`: If you override one, then you must
  implement both.

# Testing Private Methods

Right now, I am using an annotation `@VisibleForTesting` to indicate a
method or field is supposed to be private, but it's visible to JUnit.
It's purely documentation, and doesn't actually change the modifiers to
the method or element.

The next best thing would be to write an [annotation processor](https://www.baeldung.com/java-annotation-processing-builder)
to check for a compiler flag (something like "-test" or whatever); if
present, then the processor will leave the fields as package-private. If
absent, they will be made bona-fide private.

- https://blog.jcore.com/2016/12/modify-java-8-final-behaviour-with-annotations/

# Style Guides
- https://github.com/twitter-archive/commons/blob/master/src/java/com/twitter/common/styleguide.md
- https://google.github.io/styleguide/javaguide.html
- http://cr.openjdk.java.net/~alundblad/styleguide/index-v6.html
- https://www.oracle.com/technetwork/java/codeconventions-150003.pdf


# Notes about Maven

If you want to check for the latest version of dependencies, run
`mvn versions:display-dependency-updates`.