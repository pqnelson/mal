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
 * Create a list from arguments.
 */
function list() {
  return Array.prototype.slice.call(arguments, 0);
}

/**
 * Predicate testing if an object is a list.
 */
function is_list(obj) {
  return Array.isArray(obj);
}

/**
 * Checks if two objects are equal.
 *
 * @see "Equal Rights for Functional Objects, or, the More Things Change, The More They Are the Same" by Henry Baker
 * @param {*} lhs - The left-hand side of the equality test.
 * @param {*} rhs - The right-hand side of the equality test.
 * @returns True iff the two objects are "the same".
 */
 function egal(lhs, rhs) {
  if (is_list(lhs) && is_list(rhs)) {
    if (lhs.length !== rhs.length) return false;
    for (var i=0; i < lhs.length; i++) {
      if (!egal(lhs[i], rhs[i])) {
        return false;
      }
    }
    return true;
  }
  if (is_symbol(lhs) && is_symbol(rhs)) {
    return lhs.eq(rhs);
  }
  if (is_keyword(lhs) && is_keyword(rhs)) {
    return lhs.eq(rhs);
  }
  return lhs==rhs;
}

/**
 * Interpreted function (as opposed to 'compiled' or 'native' function).
 *
 * @param Eval - The Eval function from our REPL
 * @param Env - The Environment constructor.
 * @param ast - The S-expression for the function's body.
 * @param env - The environment when we constructed our function.
 * @param params - The formal parameters to the function.
 * @constructor
 */
function Fun(Eval, Env, ast, env, params) {
  var fn = function() {
    // This is insane
    // @see {@link https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Functions/arguments}
    return Eval(ast, new Env(env, params, arguments));
  }
  fn.__meta__ = null;
  fn.__ast__ = ast;
  fn.__compiled__ = false;
  fn.__gen_env__ = function(args) {
    console.log("params = ", pr_str(params, true));
    return new Env(env, params, args);
  };
  fn._ismacro_ = false;
  return fn;
}

function is_function(obj) {
  return ('function' === typeof(obj));
}

function is_macro(obj) {
  return (is_function(obj) && !!obj._ismacro_);
}

function is_fn(obj) {
  return (is_function(obj) && !obj._ismacro_);
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
function is_compiled_function(obj) {
  return is_fn(obj) && !!obj.__compiled__;
}

/**
 * Is the object a Lisp function?
 *
 * Lisp functions are interpreted by the evaluator, as opposed to being
 * compiled to a Javascript function.
 *
 * @see is_compiled_function
 * @param {*} obj - A possible Lisp function.
 * @returns True iff the object is a Lisp function.
 */
function is_interpreted_function(obj) {
  return is_fn(obj) && !obj.__compiled__;
}

/**
 * A poor man's atom.
 *
 * @param value - Optional initial value, defaults to NULL.
 * @constructor
 * @see {@link https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/Atom.java}
 */
function Atom(value) {
  this.value = value || null;
}

function is_atom(obj) {
  return (obj instanceof Atom);
}

Atom.prototype.deref = function() { return this.value; };
Atom.prototype.reset = function(new_value) {
  this.value = new_value;
  return new_value;
};
Atom.prototype.type = function() { return 'atom'; };



function is_true(obj) {
  return true === obj;
}

function is_false(obj) {
  return false === obj;
}

function is_string(obj) {
  return 'string' === typeof(obj);
}

function is_number(obj) {
  return 'number' === typeof(obj);
}

/**
 * Produce a string representation of the type for the object.
 */
function obj_type(obj) {
  if (is_symbol(obj)) { return 'symbol'; }
  else if (is_keyword(obj)) { return 'object'; }
  else if (is_list(obj)) { return 'list'; }
  else if (is_null(obj)) { return 'nil'; }
  else if (is_true(obj)) { return 'boolean'; }
  else if (is_false(obj)) { return 'boolean'; }
  else if (is_atom(obj)) { return 'atom'; }
  else {
    switch(typeof(obj)) {
    case 'number': return 'number';
    case 'function': return 'function';
    case 'string': return 'string';
    default:
      throw new Error("Unknown type '"+typeof(obj)+"'");
    }
  }
}
