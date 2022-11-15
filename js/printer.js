/**
 * Print an arbitrary lisp object as a string.
 *
 * @param {*} obj - A lisp object.
 * @param {boolean} prettyprint - Pretty print the strings.
 * @return String representation of the object.
 */
function pr_str(obj, prettyprint) {
  switch (typeof(obj)) {
  case 'string':
    if (prettyprint) {
      return '"'+obj+'"';
    } else {
      return obj;
    }
  case 'function':
    return obj.name;
  default:
    if (null === obj) {
      return "nil";
    } else if (is_list(obj)) {
      var result = obj.map(function(e) { return pr_str(e, prettyprint); });
      return "("+result.join(' ')+")";
    } else {
      return obj.toString();
    }
  }
}

register_suite(new TestSuite("Printer Tests", [
  test_case("pr_str for a function", function () {
    return ("pr_str" === pr_str(pr_str));
  }),
  test_case("pr_str for a string", function () {
    const s = "Lisp is a great language";
    return ("Lisp is a great language" === pr_str(s));
  }),
  test_case("pr_str for a string", function () {
    const s = "Lisp is a great language";
    return ('"Lisp is a great language"' === pr_str(s, true));
  }),
  test_case("pr_str for a symbol", function () {
    const symb = new MalSymbol("Foobar");
    return ("Foobar" === pr_str(symb));
  }),
  test_case("pr_str for a 'list'", function () {
    const symb = new MalSymbol("Foobar");
    var result = pr_str([symb,42,"Lisp is a great Language"], true);
    return ('(Foobar 42 "Lisp is a great Language")' === result);
  }),
  test_case("pr_str for nil", function () {
    return ("nil" === pr_str(null));
  })
]));
