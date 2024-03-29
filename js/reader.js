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
  /* re = /whitespace(splice|collections|string|comment|everything-else)/ */
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

const floatPattern = /^([-+]?[0-9]+(\.[0-9]*)?([eE][-+]?[0-9]+)?)(M)?$/g;
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
  } else if (token.match(floatPattern)) { // float
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
  } else if (":" === token[0]) {
    return keyword(token.slice(1, token.length));
  } else { // default: symbol
    /* TODO: consider handling namespaces here? Things like
       "namespace.subspace.subsubspace/identifier" */
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

function read_hash_map(reader) {
  var contents = read_list(reader, "{", "}");
  if (contents.length % 2 !== 0) {
    throw new Error("Hash maps require an even number of elements as key-value pairs");
  }
  var result = new HashMap();
  for (var i=0; i < contents.length; i += 2) {
    result.set(contents[i], contents[i+1]);
  }
  return result;
}

const _quote = new MalSymbol("quote");
const _quasiquote = new MalSymbol("quasiquote");
const _unquote = new MalSymbol("unquote");
const _splice_unquote = new MalSymbol("splice-unquote");
const _deref = new MalSymbol("deref");
const _with_meta = new MalSymbol("with-meta");

/**
 * Private reader main method for reading.
 * @private
 */
Reader.prototype.readForm = function() {
  var token = this.peek();
  switch (token) {
    case ';': return null;
    // Minimal reader macros
    case "'":
      this.next();
      return [_quote, this.readForm()];
    case "`":
      this.next();
      return [_quasiquote, this.readForm()];
    case "~":
      this.next();
      return [_unquote, this.readForm()];
    case "~@":
      this.next();
      return [_splice_unquote, this.readForm()];

    // metadata
    case "^":
      this.next();
      var meta = this.readForm();
      return [_with_meta, this.readForm(), meta];
    // deref
    case "@":
      this.next();
      return [_deref, this.readForm()];
    // Lists
    case ')': throw new Error("unexpected ')'");
    case '(': return read_list(this, "(", ")");

    // Treat vectors as lists
    case ']': throw new Error("unexpected ']'");
    case '[': return read_list(this, "[", "]");

    // hashmaps!
    case "}": throw new Error("unexpected '}'");
    case "{": return read_hash_map(this);

    default: return read_atom(this);
  }
};

/**
 * Public facing `read` function.
 *
 * If `str` is multiple forms, e.g. "(def! foo 42)\n(- foo 33)"
 * then it will just evaluate the first form. THIS IS NOT A BUG.
 * This is how the Lisp reader is expected to behave; e.g., Common Lisp
 * works this way.
 *
 * @param {string} str - the string we are reading.
 * @returns {*} The Lisp object represented by the string.
 */
function read_str(str) {
  var tokens = tokenize(str);
  var reader = new Reader(tokens);
  return reader.readForm();
}


register_suite(new TestSuite("Reader Tests", [
  test_case("- is a symbol, not a float", function () {
    var result = read_str("-");
    return (symbol_QMARK_(result));
  }),
  test_case("+ is a symbol, not a float", function () {
    var result = read_str("+");
    return (symbol_QMARK_(result));
  }),
  test_case("* is a symbol", function () {
    var result = read_str("*");
    return (symbol_QMARK_(result));
  }),
  test_case("/ is a symbol", function () {
    var result = read_str("/");
    return (symbol_QMARK_(result));
  }),
  test_case("'nil' reads to null", function () {
    var result = read_str("nil");
    return (null === result);
  }),
  test_case("'true' reads to true", function () {
    var result = read_str("true");
    return (true === result);
  }),
  test_case("'false' reads to false", function () {
    var result = read_str("false");
    return (false === result);
  }),
  test_case("'-1.25' is a float", function () {
    var result = read_str("-1.25");
    return (-1.25 === result);
  }),
  test_case("'.25' is a symbol", function () {
    var result = read_str(".25");
    return (symbol_QMARK_(result));
  }),
  test_case("'3e5' is a symbol", function () {
    var result = read_str("3e5");
    return (3e5 === result);
  }),
  test_case("':foo' is a keyword", function () {
    var result = read_str(":foo");
    return (keyword_QMARK_(result));
  }),
  test_case("'(:foo 1 2)' is a list", function () {
    var result = read_str("(:foo 1 2)");
    return (list_QMARK_(result));
  })
]));
