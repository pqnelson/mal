/**
 * @file Encodes the primitive types for MAL.
 *
 * So far, only the Symbol class has been implemented, but I expect to
 * implement a few others (keywords, hashmaps, functions(?)).
 *
 * Right now, I am wondering if there is a way to hack things to use a
 * Javascript function instead of a `Function` class.
 *
 * @author Alex Nelson <pqnelson@gmail.com>
 */

/**
 * Creates a Lisp symbol.
 * @constructor
 * @param {string} name - The identifier for the symbol.
 */
function Symbol(name) {
  this.name = name;
}

function is_symbol(obj) {
  return (obj instanceof Symbol);
}


Symbol.prototype.isAtom = function() { return true; };

Symbol.prototype.type = function() { return "symbol"; };

/**
 * Test for equality with another object.
 *
 * Returns false unless comparing to another Symbol with an identical name.
 *
 * @param {object} rhs - The "right hand side" to the equality test.
 * @return {boolean} - The result of comparing types and name [identifier].
 */
Symbol.prototype.eq = function(rhs) {
  if (is_symbol(rhs)) {
    return (this.name === rhs.name);
  } else {
    return false;
  }
};

/**
 * Produce a string representation of this symbol.
 *
 * @return {string} - The identifier as a string.
 */
Symbol.prototype.toString = function() {
  return this.name;
}
