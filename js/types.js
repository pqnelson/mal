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
 * A number of public-facing predicates are suffixed with "_QMARK_",
 * following Clojure's munging naming-conventions.
 *
 * I may need to sit down, and thnk about cloning more carefully.
 *
 * @see {@link https://exploringjs.com/deep-js/ch_copying-objects-and-arrays.html}
 * @see {@link https://exploringjs.com/deep-js/ch_copying-class-instances.html}
 * @see {@link https://github.com/clojure/clojure/blob/e6fce5a42ba78fadcde00186c0b0c3cd00f45435/src/jvm/clojure/lang/Compiler.java#L2846-L2871}
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
 * Unlike Common Lisp, this is not a Flyweight. (We *do* treat Keywords
 * as a Flyweight design pattern.)
 *
 * Note that ES6 introduces a new builtin class, also called "Symbol".
 * Do not be confused: ES6 "Symbols" are more like enum entries, not
 * identifiers we can bind values to.
 *
 * @constructor
 * @param {string} name - The identifier for the symbol.
 * @see {@link https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Symbol}
 */
function MalSymbol(name) {
  this.name = name;
  this.__meta__ = null;
}

function symbol_QMARK_(obj) {
  return (obj instanceof MalSymbol);
}

MalSymbol.prototype.getName = function() { return this.name; };

MalSymbol.prototype.type = function() { return "symbol"; };

MalSymbol.prototype.clone = function() {
  return new MalSymbol(this.getName());
};

/**
 * Test for equality with another object.
 *
 * Returns false unless comparing to another MalSymbol with an identical name.
 *
 * @param {object} rhs - The "right hand side" to the equality test.
 * @return {boolean} - The result of comparing types and name [identifier].
 */
MalSymbol.prototype.eq = function(rhs) {
  if (symbol_QMARK_(rhs)) {
    return (this.getName() === rhs.getName());
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
  return this.getName();
};

/**
 * Predicate testing if an object is null or not.
 *
 * @param {*} obj - The object we test for nullity.
 * @returns True if and only if obj is null.
 */
function nil_QMARK_(obj) {
  return null === obj;
}


/**
 * Abstract class for callable objects.
 *
 * @see {@link https://github.com/sleexyz/callable}
 */
function Callable(f) {
  const wrapped = x => f(x);
  Object.setPrototypeOf(wrapped, this.constructor.prototype);
  return wrapped;
}

Callable.prototype = Object.create(Function.prototype);
Callable.prototype.constructor = Callable;

class HashMap extends Callable {
  static _setUpPrototype() {
    for (const methodName of ['has', 'keys', 'set', 'values', 'entries',
                              'delete','forEach', 'clear']) {
      HashMap.prototype[methodName] = function (...args) {
        return this.#table[methodName](...args);
      };
    }
  }

  #table;
  constructor() {
    super(k => this.get(k));
    this.#table = new Map();
    this.__meta__ = null;
  }

  type () { return "HashMap"; }

  get(k, defaultValue=null) {
    const val = this.#table.get(k);
    return (undefined === val ? defaultValue : val);
  }

  /**
   * Produce a string representation of the HashMap, suitable for reading.
   *
   * @param {boolean} prettyPrintKVs - Pretty print flag used when calling pr_str
   * @returns {string} String representation of all key-value pairs in braces.
   */
  toString(prettyPrintKVs=true) {
    if (0 === this.size) { return "{}"; }
    var entries = [];
    for (const [k,v] of this.#table) {
      entries.push(pr_str(k, prettyPrintKVs)+" "+pr_str(v, prettyPrintKVs));
    }
    return "{" + entries.reduce((acc, entry) => (""===acc ? entry : acc+", "+entry), "")+"}";
  }

  /**
   * Test for equality.
   *
   * @param {*} rhs - The right hand side of equality testing.
   * @returns {boolean} True iff equal.
   */
  eq(rhs) {
    if (typeof(this) !== typeof(rhs)) return false;
    if (this === rhs) return true;
    if (this.size !== rhs.size) return false;

    for (const [k,v] of this) {
      if (!egal(v, rhs.get(k))) {
        return false;
      }
    }
    return true;
  }

  size() { return this.#table.size; }
  isEmpty() { return 0 === this.#table.size; }
}
HashMap._setUpPrototype();

function map_QMARK_(obj) {
  return (obj instanceof HashMap);
}
var debugging = false;
/**
 * Keywords are unique symbols prefixed by a colon.
 *
 * We can encode them as strings prefixed by unicode '\u029e'
 * [inverted 'k'] but using the Flyweight design pattern, we can use '=='
 * comparison to check if they refer to the same object.
 */
var KeywordFactory = (function () {
  // Using a JSON object can lead to memory leaks on IE10, which isn't
  // (or shouldn't be) a concern anymore, but I'm paranoid.
  var table = new Map(); // Weak maps won't work, neither strings nor symbols are objects

  class Keyword extends Callable {
    #name = null;
    constructor(name) {
      // TODO: make this more robust
      super((coll,defaultValue=null) => coll.get(this, defaultValue));
      this.#name = name;
    }
    toString() { return ":"+this.#name; }
    type() { return "keyword"; }
    eq(rhs) { return this === rhs; }
    clone() { return this; }
  }

  return {
    get: function (name) {
      var k = Symbol.for(name);
      if (table.has(k)) {
        return table.get(k);
      }
      var result = new Keyword(name);
      table.set(k, result);
      return result;
    },

    is_kw: function(obj) {
      return (obj instanceof Keyword);
    },

    // debugging code, also useful for testing
    clear: () => table.clear(),

    getCount: function () {
      var count = 0;
      for (const [k,v] of table.entries()) {
        count++;
      }
      return count;
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
  return KeywordFactory.get(name);
}

function keyword_QMARK_(obj) {
  return KeywordFactory.is_kw(obj);
}

/**
 * Create a list from arguments.
 */
function list() {
  return Array.prototype.slice.call(arguments, 0);
}

/**
 * Fastest way to clone an array.
 *
 * Right now (16 Nov 2022), `list.slice()` is barely faster than
 * `list.slice(0)` on Firefox 106.0.5 (64-bit). Array.from is among
 * the slowest.
 *
 * @see {@link https://stackoverflow.com/a/21514254}
 * @returns {Array.<*>} Shallow clone of the object.
 */
Array.prototype.clone = function() {
  return this.slice(0);
};

/**
 * Predicate testing if an object is a list.
 */
function list_QMARK_(obj) {
  return Array.isArray(obj);
}

/**
 * Checks if two objects are equal.
 *
 * Defaults to "eq" method, if available. If you are writing a custom
 * class, then implement an "eq" method; it will allow lisp to defer
 * to it upon testing for equality.
 *
 * @see "Equal Rights for Functional Objects, or, the More Things Change, The More They Are the Same" by Henry Baker
 * @param {*} lhs - The left-hand side of the equality test.
 * @param {*} rhs - The right-hand side of the equality test.
 * @returns True iff the two objects are "the same".
 */
function egal(lhs, rhs) {
  if (list_QMARK_(lhs) && list_QMARK_(rhs)) {
    if (lhs.length !== rhs.length) return false;
    for (var i=0; i < lhs.length; i++) {
      if (!egal(lhs[i], rhs[i])) {
        return false;
      }
    }
    return true;
  }
  if (typeof(lhs) === typeof(rhs) && !!lhs.eq) {
    return lhs.eq(rhs);
  }
  return lhs===rhs;
}

/**
 * Interpreted function (as opposed to 'compiled' or 'native' function).
 *
 * Explicitly calls back to the Lisp evaluator, as opposed to compiling
 * down to a Javascript function.
 *
 * @param {Function} Eval - The Eval function from our REPL
 * @param {Function} Env - The Environment constructor.
 * @param {*} ast - The S-expression for the function's body.
 * @param {Env} env - The environment when we constructed our function.
 * @param {MalSymbol[]} params - The formal parameters to the function.
 * @constructor
 */
function Fun(Eval, Env, ast, env, params) {
  var fn = function() {
    // This is insane
    // @see {@link https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Functions/arguments}
    return Eval(ast, new Env(env, params, arguments));
  };
  fn.__meta__ = null;
  fn.__ast__ = ast;
  fn.__interpreted__ = true;
  fn.__gen_env__ = function(args) {
    console.log("params = ", pr_str(params, true));
    return new Env(env, params, args);
  };
  fn._ismacro_ = false;
  return fn;
}

/**
 * Clone a function.
 *
 * @see {@link https://stackoverflow.com/a/11230005}
 */
Function.prototype.clone = function() {
  // Avoid long chains of needless callbacks
  var that = this;
  if(this.__isClone) {
    that = this.__clonedFrom;
  }
  // Send in the clowns...I mean, clones...
  var clown = function () { return that.apply(this, arguments); };
  for(key in this) {
    clown[key] = this[key];
  }

  clown.__isClone = true;
  clown.__clonedFrom = that;
  return clown;
};

function function_QMARK_(obj) {
  return (Function === obj?.constructor);
}

function macro_QMARK_(obj) {
  return (function_QMARK_(obj) && !!obj._ismacro_);
}

function fn_QMARK_(obj) {
  return (function_QMARK_(obj) && !obj._ismacro_);
}

/**
 * Is the object a Javascript function?
 *
 * "Compiled" function, in analogy to Common Lisp's terminology. This is
 * in contrast to being a Lisp interpreted function, which is executed
 * only by the evaluator.
 *
 * @see {@link https://www.cs.cmu.edu/Groups/AI/html/cltl/clm/node226.html#SECTION002912000000000000000}
 * @param {*} obj - A possible function object.
 * @returns True iff the object is a Javascript function.
 */
function compiled_function_QMARK_(obj) {
  return fn_QMARK_(obj) && !obj.__interpreted__;
}

/**
 * Is the object a Lisp function?
 *
 * Lisp functions are interpreted by the evaluator, as opposed to being
 * compiled to a Javascript function.
 *
 * @see compiled_function_QMARK_
 * @param {*} obj - A possible Lisp function.
 * @returns True iff the object is a Lisp function.
 */
function interpreted_function_QMARK_(obj) {
  return fn_QMARK_(obj) && !!obj.__interpreted__;
}

/**
 * A poor man's atom.
 *
 * This is the only way to create a class with a private field using
 * ES5, that I could think of, at least.
 *
 * @param {*=} initial_value - Optional initial value, defaults to NULL.
 * @see {@link https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/Atom.java}
 * @see {@link https://css-tricks.com/implementing-private-variables-in-javascript/}
 * @constructor
 */
function Atom(initial_value) {
  var value = initial_value || null;

  this.deref = () => value;

  this.reset = (new_value) => {
    value = new_value;
  };
}

Atom.prototype.type = function() { return 'atom'; };

function atom_QMARK_(obj) {
  return (obj instanceof Atom);
}

/**
 * Two atoms are equal iff they are identical (refer to the same location in memory).
 */
Atom.prototype.eq = function(rhs) {
  return atom_QMARK_(rhs) && (this === rhs);
};


function true_QMARK_(obj) {
  return true === obj;
}

function false_QMARK_(obj) {
  return false === obj;
}

function string_QMARK_(obj) {
  return String === obj.constructor;
}

function number_QMARK_(obj) {
  return 'number' === typeof(obj);
}


/**
 * Produce a string representation of the type for the object.
 *
 * If given an object with a method "type", it will call that as a last
 * resort.
 *
 * @param {*} obj - A user-supplied object.
 * @returns {string} A string representation of the type of the object.
 */
function obj_type(obj) {
  if (obj?.type) { return obj.type(); }
  else if (list_QMARK_(obj)) { return 'list'; }
  else if (nil_QMARK_(obj)) { return 'nil'; }
  else if (true_QMARK_(obj)) { return 'boolean'; }
  else if (false_QMARK_(obj)) { return 'boolean'; }
  else {
    switch(obj.constructor.name) {
      case 'number': return 'number';
      case 'function': return 'function';
      case 'string': return 'string';
      default:
        return Object.prototype.toString.call(obj).replace(/^\[object (.+)\]$/,"$1").toLowerCase();
    }
  }
}



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
  test_case("symbols are symbols", function() {
    const symbol = new MalSymbol("foobar");
    return symbol_QMARK_(symbol);
  }),
  test_case("numbers are not symbols", function() {
    return !symbol_QMARK_(42);
  })
]));


register_suite(new TestSuite("HashMap Tests", [
  test_case("map is not a function", () => {
    const m = new HashMap();
    return !function_QMARK_(m);
  }),
  test_case("new maps are empty", () => {
    const m = new HashMap();
    return m.isEmpty();
  }),
  test_case("assoc'd maps are not empty", function () {
    var m = new HashMap();
    m.set("foo", "bar");
    return !m.isEmpty();
  }),
  test_case("assoc'd maps contain the new key", () => {
    var m = new HashMap();
    m.set("foo", "bar");
    return m.has("foo");
  }),
  test_case("(map? (HashMap.)) is true", () => {
    const m = new HashMap();
    return map_QMARK_(m);
  }),
  test_case("toString for empty map is {}", () => {
    const m = new HashMap();
    return ("{}" === m.toString());
  }),
  test_case("HashMap toString() works as expected", () => {
    let m = new HashMap();
    m.set("foo", "bar");
    return ('{"foo" "bar"}' === m.toString());
  }),
  test_case("HashMap toString(false) works as expected", () => {
    let m = new HashMap();
    m.set("foo", "bar");
    return ("{foo bar}" === m.toString(false));
  }),
  test_case("HashMap::get() returns value when key is present", () => {
    let m = new HashMap();
    let k = keyword("foo");
    let v = keyword("bar");
    m.set(k, v);
    return (v === m.get(k));
  }),
  test_case("HashMap::get(k) returns null when key is absent", () => {
    let m = new HashMap();
    let k = keyword("foo");
    let v = keyword("bar");
    m.set(k, v);
    return (null === m.get("foo"));
  }),
  test_case("HashMap::get(k, default) returns default when key is absent", () => {
    let m = new HashMap();
    let k = keyword("foo");
    let v = keyword("bar");
    m.set(k, v);
    return ("spam" === m.get("foo", "spam"));
  }),
  test_case("A hashmap is not a function", () => {
    const m = new HashMap();
    return !function_QMARK_(m);
  }),
  test_case("A hashmap is callable, syntactically 'like' a function", () => {
    const m = new HashMap();
    let k = keyword("foo");
    let v = keyword("bar");
    m.set(k, v);
    return (v === m(k));
  }),
  test_case("A key is a rator when a hashmap is its rand", () => {
    const m = new HashMap();
    let k = keyword("foo");
    let v = keyword("bar");
    m.set(k, v);
    return (v === k(m));
  })
]));
