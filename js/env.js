

function Env(outer) {
  this.outer = outer || null;
  this.bindings = {};
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
    throw new Error("env.find key must be given a symbol");
  }
  if (key.getName() in this.bindings) { return this; }
  else if (this.outer) { return this.outer.find(key); }
  else { return null; }
};

Env.prototype.get = function(key) {
  if (!is_symbol(key)) {
    throw new Error("env.get key must be given a symbol");
  }
  var env = this.find(key);
  if (!env) { throw new Error('"'+key.toString()+'" not found in environment'); }
  return env.bindings[key.getName()];
};
