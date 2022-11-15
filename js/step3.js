function READ(str) {
  return read_str(str);
}

function eval_ast(ast, env) {
  if(is_symbol(ast)) {
    return env.get(ast);
  } else if (is_list(ast)) {
    return ast.map(function(a) { return EVAL(a, env); });
  } else {
    return ast;
  }
}

function _eval(ast, env) {
  if (!is_list(ast)) {
    return eval_ast(ast, env);
  }
  if (ast.length === 0) {
    return ast;
  }
  /* apply! */
  console.log("ast = ", pr_str(ast, true));
  switch(ast[0].toString()) {
  case 'def!':
    var body = ast[2],
    identifier = ast[1],
    result = EVAL(body,env);
    return env.set(identifier, result);
  case 'let*':
    var let_env = new Env(env),
    let_bindings = ast[1],
    body = ast[2];
    for (var i=0; i < let_bindings.length; i+=2) {
      let_env.set(let_bindings[i], EVAL(let_bindings[i+1], let_env));
    }
    return EVAL(body, let_env);
  default:
    var e = eval_ast(ast, env);
    console.log("e = ", pr_str(e, true));
    var f = e[0];
    return f.apply(f, e.slice(1));
  }
}

function EVAL(ast, env) {
  var result = _eval(ast, env);
  return ("undefined" !== typeof result) ? result : null;
}

function PRINT(exp) {
  return pr_str(exp, true);
}

var init_env = new Env();
init_env.set(new MalSymbol('+'), function(a,b) { return a+b; });
init_env.set(new MalSymbol("-"), function(a,b) { return a-b; });
init_env.set(new MalSymbol('*'), function(a,b) { return a*b; });
init_env.set(new MalSymbol('/'), function(a,b) { return a/b; });

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
