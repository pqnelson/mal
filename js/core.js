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

function cons(a, b) { return [a].concat(b); }



var ns = {
  'nil?': is_null,
  '=': egal,
  'type': obj_type,
  'keyword?': is_keyword,
  'symbol?': is_symbol,
  'true?': is_true,
  'false?': is_false,
  'string?': is_string,
  'number?': is_number,
  'fn?': is_function,

  'pr-str': print_str,
  'str': str,
  'cons': cons,
  '>': function(a,b) { return a>b; },
  '>=': function(a,b) { return a>=b; },
  '<': function(a,b) { return a<b; },
  '<=': function(a,b) { return a<=b; },
  '+': function(a,b) { return a+b; },
  '-': function(a,b) { return a-b; },
  '*': function(a,b) { return a*b; },
  '/': function(a,b) { return a/b; }
};
