package com.github.pqnelson;

/* Compare to Clojure Munge dictionary
https://github.com/clojure/clojure/blob/e6fce5a42ba78fadcde00186c0b0c3cd00f45435/src/jvm/clojure/lang/Compiler.java#L2846-L2871
 */
public enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    LEFT_BRACKET, RIGHT_BRACKET,

    QUOTE, BACKTICK, WITH_META, SPLICE, UNQUOTE,

    // Literals.
    IDENTIFIER, NUMBER, STRING, KEYWORD, CHAR,

    // special forms
    DEF, DEFMACRO, DO, FN_STAR, IF, LET_STAR,
    NIL, TRUE, FALSE, TRY, CATCH, MACROEXPAND, QUASIQUOTE_EXPAND,

    EOF
}