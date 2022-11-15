// import {register_suite,TestSuite,TestCase} from "./test-framework.js";
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

/*
 * Think about whether we should use weak maps (or maps), and weak sets (or sets)?
 * @see {@link https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/WeakSet}
 * @see {@link https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/WeakMap}
 * @see {@link https://developer.mozilla.org/en-US/docs/Web/JavaScript/Memory_Management#data_structures_aiding_memory_management}
 */


/**
 * Creates a Lisp symbol.
 *
 * Note that ES6 introduces a new builtin class, also called "Symbol".
 * Perhaps there is a way to leverage this?
 *
 * @constructor
 * @param {string} name - The identifier for the symbol.
 * @see {@link https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Symbol}
 */
function MalSymbol(name) {
  this.name = name;
}

function is_symbol(obj) {
  return (obj instanceof MalSymbol);
}

MalSymbol.prototype.getName = function() { return this.name; };

MalSymbol.prototype.isAtom = function() { return true; };

MalSymbol.prototype.type = function() { return "symbol"; };

/**
 * Test for equality with another object.
 *
 * Returns false unless comparing to another MalSymbol with an identical name.
 *
 * @param {object} rhs - The "right hand side" to the equality test.
 * @return {boolean} - The result of comparing types and name [identifier].
 */
MalSymbol.prototype.eq = function(rhs) {
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
MalSymbol.prototype.toString = function() {
  return this.name;
};


register_suite(new TestSuite("MalSymbol Tests", [
  test_case("equality fails on numbers", function () {
    const symbol = new MalSymbol("foobar");
    return !(symbol.eq(42));
  }),
  test_case("equality works on the same symbol", function () {
    const symbol = new MalSymbol("foobar");
    return (symbol.eq(symbol));
  }),
  test_case("equality works on the different symbol objects with the same name", function () {
    const symbol = new MalSymbol("foobar");
    const symb = new MalSymbol("foobar");
    return (symbol.eq(symb));
  }),
  test_case("symbols are atoms", function() {
    const symbol = new MalSymbol("foobar");
    return symbol.isAtom();
  }),
  test_case("symbols are symbols", function() {
    const symbol = new MalSymbol("foobar");
    return is_symbol(symbol);
  }),
  test_case("numbers are not symbols", function() {
    return !is_symbol(42);
  })
]));

/**
 * Predicate testing if an object is null or not.
 *
 * @param {*} obj - The object we test for nullity.
 * @returns True if and only if obj is null.
 */
function is_null(obj) {
  return null === obj;
}

/**
 * Keywords are unique symbols prefixed by a colon.
 *
 * We can encode them as strings prefixed by unicode '\u029e'
 * [inverted 'k'] but using the Flyweight design pattern, we can use '=='
 * comparison to check if they refer to the same object.
 */

var FlyWeightFactory = (function () {
  var table = {};

  function Keyword(name) {
    this.name = name;
  }
  Keyword.prototype.toString = function() { return ":"+(this.name); };
  Keyword.prototype.type = function() { return 'keyword'; };
  Keyword.prototype.eq = function(rhs) { return this==rhs; };
  Keyword.prototype.isAtom = function(rhs) { return true; };

  return {
    get: function (name) {
      if (!table[name]) {
        table[name] =
          new Keyword(name);
      }
      return table[name];
    },

    getCount: function () {
      var count = 0;
      for (var f in table) count++;
      return count;
    },

    is_kw: function(obj) {
      return (obj instanceof Keyword);
    }
  };
})();

/**
 * Constructor for a new keyword.
 *
 * Guarantees the keyword is at most unique.
 *
 * @param {string} name - The name of the keyword.
 * @returns New Keyword instance.
 */
function keyword(name) {
  return FlyWeightFactory.get(name);
}

function is_keyword(obj) {
  return FlyWeightFactory.is_kw(obj);
}

/**
 * Predicate testing if an object is a list.
 */
function is_list(obj) {
  return Array.isArray(obj);
}
