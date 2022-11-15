

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

Env.prototype.set = function(key, value) {
  if (!is_symbol(key)) {
    throw new Error("env.set key must be given a symbol");
  }
  this.bindings[key.getName()] = value;
  return value;
};

Env.prototype.find = function(key) {
  if (!is_symbol(key)) {
    throw new Error("env.find key must be given a symbol, given "+obj_type(key));
  }
  if (key.getName() in this.bindings) { return this; }
  else if (this.outer) { return this.outer.find(key); }
  else { return null; }
};

Env.prototype.get = function(key) {
  if (!is_symbol(key)) {
    throw new Error("env.get key must be given a symbol, given "+obj_type(key));
  }
  var env = this.find(key);
  if (!env) { throw new Error('"'+key.toString()+'" not found in environment'); }
  return env.bindings[key.getName()];
};
