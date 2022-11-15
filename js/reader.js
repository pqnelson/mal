/**
 * Lisp reader class.
 *
 * A number of functions here are marked as "private" in the documentation.
 * We're on the honor code, because I'm too lazy to enforce this at the moment.
 *
 * @author Alex Nelson <pqnelson@gmail.com>
 */


/**
 * Tokenize a string, producing an array of [string] tokens.
 *
 * @param {string} str - the string we will tokenize
 * @return {Array.string} an array of tokens as strings.
 * @private
 */
function tokenize(str) {
  var re = /[\s,]*(~@|[\[\]{}()'`~^@]|"(?:\\.|[^\\"])*"?|;.*|[^\s\[\]{}('"`,;)]*)/g;

  var results = [];
  while ((match = re.exec(str)[1]) != '') {
    if (match[0] === ';') { continue; }
    results.push(match);
  }
  return results;
}

/**
 * Constructs a new reader from an array of tokens.
 *
 * @constructor
 * @param {Array.<string>} tokens - an array of string tokens.
 */
function Reader(tokens) {
  this.tokens = tokens;
  this.position = 0;
}

Reader.prototype.peek = function() { return this.tokens[this.position]; };
Reader.prototype.next = function() { return this.tokens[this.position++]; };

/**
 * Function to read the atom and determine what it is.
 *
 * @param reader - the reader whose head is the token we're reading.
 * @throws error for unbalanced strings
 * @private
 */
function read_atom(reader) {
  var token = reader.next();
  if (token.match(/^-?[0-9]+$/)) { // integer
    return parseInt(token, 10);
  } else if (token.match(/^(-?[0-9]+\.?[0-9]*[eE]?[0-9]*|-?[0-9]*\.?[0-9]+[eE]?[0-9]*)$/)) { // float
    return parseFloat(token);
  } else if (token.match(/^"(?:\\.|[^\\"])*"$/)) { // string
    return token.slice(1, token.length - 1);
  } else if ('"' === token[0]) {
    throw new Error("expected '\"', got EOF");
  } else if ("nil" === token) {
    return null;
  } else if ("true" === token) {
    return true;
  } else if ("false" === token) {
    return false;
  } else { // default: symbol
    // We can lookup a Javascript variable by
    // `window[token]`.
    // @see {@link https://stackoverflow.com/q/1920867}
    return new MalSymbol(token);
  }
}

/** Create a list. We treat vectors and lists as arrays.
 *
 * @param reader contains the list of elements.
 * @param {string} start - the string for the starting delimiter
 * @param {string} end - the string for the matching ending delimiter
 * @returns an array of objects.
 * @private
 */
function read_list(reader, start, end) {
  start = start || '(';
  end = end || ')';
  var ast = [];
  var token = reader.next();
  if (token !== start) {
    throw new Error("expected '"+start+"', found '"+token+"'");
  }
  while ((token = reader.peek()) !== end) {
    if (!token) {
      throw new Error("expected '"+end+"', found EOF");
    }
    ast.push(reader.readForm());
  }
  reader.next();
  return ast;
}

/**
 * Private reader main method for reading.
 * @private
 */
Reader.prototype.readForm = function() {
  var token = this.peek();
  switch (token) {
  case ';': return null;
    
  case "'":
    this.next();
    return [new MalSymbol("quote"), this.readForm()];

  case ')': throw new Error("unexpected ')'");
  case '(': return read_list(this, "(", ")");

  case ']': throw new Error("unexpected ']'");
  case '[': return read_list(this, "[", "]");

  default: return read_atom(this);
  }
};

/**
 * Public facing `read` function.
 *
 * A bug: if `str` is multiple forms, e.g. "(def! foo 42)\n(- foo 33)"
 * then it will just evaluate the first form.
 *
 * @param {string} str - the string we are reading.
 */
function read_str(str) {
  var tokens = tokenize(str);
  var reader = new Reader(tokens);
  return reader.readForm();
}


register_suite(new TestSuite("Reader Tests", [
  test_case("- is a symbol, not a float", function () {
    var result = read_str("-");
    return (is_symbol(result));
  }),
  test_case("+ is a symbol, not a float", function () {
    var result = read_str("+");
    return (is_symbol(result));
  }),
  test_case("* is a symbol", function () {
    var result = read_str("*");
    return (is_symbol(result));
  }),
  test_case("/ is a symbol", function () {
    var result = read_str("/");
    return (is_symbol(result));
  })
]));
