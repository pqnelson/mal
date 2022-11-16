/**
 * An environment of bindings.
 *
 * @param {Env=} outer - The "parent" environment to the newly created Env.
 * @param {MalSymbol[]} keys - An array of initial keys.
 * @param {Array.<*>} values - An initial array of values bound to the keys.
 * @constructor
 */
function Env(outer, keys, values) {
  this.outer = outer || null;
  this.bindings = {};

  // If creating a new environment for a function closure.
  if (keys && values) {
    const variadic_separator = "&";
    for (var i=0; i < keys.length; i++) {
      if (keys[i].getName() === variadic_separator) {
        this.bindings[keys[i+1].getName()] = Array.prototype.slice.call(values,i);
        break;
      } else {
        this.bindings[keys[i].getName()] = values[i];
      }
    }
  }
  return this;
}

/**
 * Bind a symbol to a specific value.
 *
 * @param {MalSymbol} key - The symbol we are binding to a new value.
 * @param {*} value - The new value for the symbol.
 * @returns value;
 * @throws error if the key is not a MalSymbol.
 */
Env.prototype.set = function(key, value) {
  if (!symbol_QMARK_(key)) {
    throw new Error("env.set key must be given a symbol");
  }
  this.bindings[key.getName()] = value;
  return value;
};

/**
 * Find the environment containing the given symbol.
 *
 * @param {MalSymbol} key - The symbol we are trying to look for.
 * @returns {Env|null} The first environment containing the symbol, or null if none found.
 * @throws error if the key is not a MalSymbol.
 */
Env.prototype.find = function(key) {
  if (!symbol_QMARK_(key)) {
    throw new Error("env.find key must be given a symbol, given "+obj_type(key));
  }
  if (key.getName() in this.bindings) { return this; }
  else if (this.outer) { return this.outer.find(key); }
  else { return null; }
};

/**
 * Obtain the value bound to the symbol.
 *
 * @param {MalSymbol} key - The "needle" we're looking for.
 * @returns The value bound to the symbol, throws an error upon failure.
 * @throws error if the key is not a MalSymbol.
 */
Env.prototype.get = function(key) {
  if (!symbol_QMARK_(key)) {
    throw new Error("env.get key must be given a symbol, given "+obj_type(key));
  }
  var env = this.find(key);
  if (!env) { throw new Error('"'+key.toString()+'" not found in environment'); }
  return env.bindings[key.getName()];
};
