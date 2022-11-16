function READ(str) {
  return read_str(str);
}

function eval_ast(ast, env) {
  if(symbol_QMARK_(ast)) {
    if (ast in env) {
      return env[ast];
    } else {
      throw new Error("'"+ast.toString()+"' not found");
    }
  } else if (list_QMARK_(ast)) {
    return ast.map(function(a) { return EVAL(a, env); });
  } else {
    return ast;
  }
}

function _eval(ast, env) {
  if (!list_QMARK_(ast)) {
    return eval_ast(ast, env);
  }
  if (ast.length === 0) {
    return ast;
  }
  /* apply! */
  console.log("ast = ", pr_str(ast, true));
  var e = eval_ast(ast, env);
  console.log("e = ", pr_str(e, true));
  var f = e[0];
  return f.apply(f, e.slice(1));
}

function EVAL(ast, env) {
  var result = _eval(ast, env);
  return ("undefined" !== typeof result) ? result : null;
}

function PRINT(exp) {
  return pr_str(exp, true);
}

var init_env = {};
init_env['+'] = function(a,b) { return a+b; };
init_env["-"] = function(a,b) { return a-b; };
init_env['*'] = function(a,b) { return a*b; };
init_env['/'] = function(a,b) { return a/b; };

function rep(str) {
  return PRINT(EVAL(READ(str), init_env));
}

function repl() {
  while(true) {
    var line = readline.readline("mal> ");
    if (null === line) { break; }
    if (line) { printer.println(rep(line)); }
  }
}

function malCompile(inputElement, outputContainer) {
  outputContainer.innerHTML = rep(inputElement.value);
}
