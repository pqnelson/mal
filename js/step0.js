function READ(str) {
  return str;
}

function EVAL(ast, env) {
  return ast;
}

function PRINT(exp) {
  return exp;
}

function rep(str) {
  return PRINT(EVAL(READ(str), {}));
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
