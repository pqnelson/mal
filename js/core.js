/**
 * Primitive functions we will provide.
 */

function print_str() {
  return Array.prototype.map.call(arguments, function(exp) {
    return pr_str(exp);
  }).join(" ");
}


function str() {
  return Array.prototype.map.call(arguments, function(exp) {
    return pr_str(exp);
  }).join("");
}

function slurp(filename) {
  var request = new XMLHttpRequest();
  request.open("GET", filename, false);
  request.send();
  if (200 === request.status) {
    return request.responseText;
  } else {
    throw new Error("Failed to slurp file '"+filename+"'");
  }
}

function cons(a, b) { return [a].concat(b); }

function concat_with_rest_parameters(coll, ...rest) {
  coll = coll || [];
  return coll.concat.apply(coll, rest);
}
/* I am told to use 'rest parameters' (...rest), but it isn't
   universally supported, so here's the version with arguments. */
function concat_with_arguments(coll) {
  coll = coll || [];
  return coll.concat.apply(coll, arguments.slice(1));
}

function first(coll) {
  if (list_QMARK_(coll)) {
    return (coll.length > 0 ? coll[0] : null);
  }
  throw new Error("first expects a list, received "+obj_type(coll));
}

function rest(coll) {
  if (list_QMARK_(coll)) {
    return (coll.length > 0 ? coll.slirce(1) : []);
  }
  throw new Error("rest expects a list, received "+obj_type(coll));
}

function nth(coll, idx) {
  if (idx < coll.length) { return coll[idx]; }
  else { throw new Error("nth: index "+idx+" out of range, max"+ " "+coll.length); }
}

function deref(atm) { return atm.deref(); };

function reset_BANG(atm, val) { return atm.reset(val); }

function swap_BANG(atm, f) {
  var args = [atm.deref()].concat(Array.prototype.slice.call(arguments, 2));
  var new_value = f.apply(f,args);
  atm.reset(new_value);
  return new_value;
}

function atom(val) {
  return new Atom(val);
}

/* Just a note about identical: NaN !== NaN, but I think it should be treated as
   identical to itself. That's why I use Object.is;
   @see {@link https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/is}
*/
var ns = {
  'nil?': nil_QMARK_,
  '=': egal,
  'identical?': Object.is,
  'type': obj_type,
  'keyword?': keyword_QMARK_,
  'symbol?': symbol_QMARK_,
  'true?': true_QMARK_,
  'false?': false_QMARK_,
  'string?': string_QMARK_,
  'number?': number_QMARK_,
  'fn?': function_QMARK_,
  'macro?': macro_QMARK_,

  'atom': atom,
  'atom?': atom_QMARK_,
  'deref': deref,
  'reset!': reset_BANG,
  'swap!': swap_BANG,

  'list?': list_QMARK_,
  'list': list,

  'pr-str': print_str,
  'str': str,
  'slurp': slurp,
  'read-string': read_str,
  'cons': cons,
  'concat': concat_with_arguments,
  '>': function(a,b) { return a>b; },
  '>=': function(a,b) { return a>=b; },
  '<': function(a,b) { return a<b; },
  '<=': function(a,b) { return a<=b; },
  '+': function(a,b) { return a+b; },
  '-': function(a,b) { return a-b; },
  '*': function(a,b) { return a*b; },
  '/': function(a,b) { return a/b; }
};
