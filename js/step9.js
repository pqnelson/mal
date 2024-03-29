/* A decent unit test would be:

(defmacro! when
  (fn* (test & body) (list 'if test (cons 'do body))))

(def! myatom (atom 3))

(def! inc (fn* [x] (+ 1 x)))

(when true
  (swap! myatom inc)
  (swap! myatom inc)
  (swap! myatom inc))
*/

function READ(str) {
  return read_str(str);
}

function eval_ast(ast, env) {
  if(symbol_QMARK_(ast)) {
    return env.get(ast);
  } else if (list_QMARK_(ast)) {
    return ast.map(function(a) { return EVAL(a, env); });
  } else {
    return ast;
  }
}

const _concat = new MalSymbol("concat");
const _cons = new MalSymbol("cons");

// This unimaginative name is taken from Appendix C of Guy Steele's
// "Common Lisp the Language".
function qq_process(acc, e) {
  if (list_QMARK_(e) && e.length && symbol_QMARK_(e[0]) && e[0].getName() === "splice") {
    return [_concat, e[1], acc];
  } else {
    return [_cons, quasiquote(e), acc];
  }
}

/**
 * Quasiquote processing.
 *
 * Also called "backtick" among Lispers.
 *
 * @see Appendix C of "Common Lisp The Language" for an alternate
 * algorithm.
 */
function quasiquote(ast) {
  if (list_QMARK_(ast) && 0 < ast.length && egal(ast[0], _unquote)) {
    return ast[1];
  } else if (list_QMARK_(ast)) {
    return ast.reduceRight(qq_process, []);
  } else if (symbol_QMARK_(ast)) {
    return [_quote, ast];
  } else {
    return ast;
  }
}

function is_macro_call(ast, env) {
  if (list_QMARK_(ast) && symbol_QMARK_(ast[0])) {
    try {
      var mac = env.get(ast[0]);
      return macro_QMARK_(mac);
    } catch {
      return false;
    }
  }
  return false;
}

function macroexpand(ast, env) {
  while (is_macro_call(ast, env)) {
    var macro = env.get(ast[0]);
    ast = macro.apply(macro, ast.slice(1));
  }
  return ast;
}

var is_log_verbose = false;

function _eval(ast, env) {
  while(true) {
    if (!list_QMARK_(ast)) {
      return eval_ast(ast, env);
    }
    if (ast.length === 0) {
      return ast;
    }
    /* apply! */
    /* begin macroexpansion */
    ast = macroexpand(ast, env);
    if (!list_QMARK_(ast)) {
      return eval_ast(ast, env);
    }
    /* end macroexpansion */
    if (is_log_verbose) {
      console.log("ast = ", pr_str(ast, true));
    }
    switch(ast[0].toString()) {
      case 'def!': {
        var body = ast[2],
            identifier = ast[1],
            result = EVAL(body,env);
        if (is_log_verbose) {
          console.log("defining '"+identifier.toString()+"' = ",
                      pr_str(result,true));
        }
        return env.set(identifier, result);
      }
      case 'let*': {
        var let_env = new Env(env),
        let_bindings = ast[1],
            body = ast[2];
        for (var i=0; i < let_bindings.length; i+=2) {
          let_env.set(let_bindings[i], EVAL(let_bindings[i+1], let_env));
        }
        ast = body;
        env = let_env;
        break;
      }
      case "do": {
        for (var i=1; i < ast.length-1; i++) {
          EVAL(ast[i], env);
        }
        ast = ast[ast.length-1];
        break;
      }
      case "if": {
        var test = EVAL(ast[1], env);
        if (null === test || false == test) {
          ast = ("undefined" !== typeof ast[3] ? ast[3] : null);
        } else {
          ast = ast[2];
        }
        break;
      }
      case "fn*": // (fn* [args] body...)
        return Fun(EVAL, Env, ast[2], env, ast[1]);
      case "macroexpand":
        return macroexpand(ast[1], env);
      case "quote":
        return ast[1];
      case "quasiquoteexpand":
        return quasiquote(ast[1]);
      case "quasiquote":
        ast = quasiquote(ast[1]);
        break;
      case "defmacro!": {
        var macro = EVAL(ast[2], env);
        macro._ismacro_ = true;
        return env.set(ast[1], macro);
      }
      case "try*":
        /* ast[0] = try*
           ast[1] = expr
           ast[2] = (catch e body) */
        try {
          return EVAL(ast[1], env);
        } catch (e) {
          if (ast[2] && !!ast[2][0] && "catch*" === ast[2][0].getName()) {
            let catch_body = ast[2][2],
                exception_id = ast[2][1];
            if (e instanceof Error) {
              return EVAL(catch_body, new Env(env, [exception_id], [e.message]));
            } else {
              return EVAL(catch_body, new Env(env, [exception_id], [e]));
            }
          } else {
            throw e;
          }
        }
      default: {
        var e = eval_ast(ast, env);
        if (is_log_verbose) {
          console.log("e = ", pr_str(e, true));
        }
        var f = e[0];
        /* if (is interpreted function) */
        if (f.__ast__) {
          ast = f.__ast__;
          env = f.__gen_env__(e.slice(1));
        } else {
          // "native function"
          return f.apply(f, e.slice(1));
        }
      }
    }
  }
}

function EVAL(ast, env) {
  var result = _eval(ast, env);
  return ("undefined" !== typeof result) ? result : null;
}

function PRINT(exp) {
  return pr_str(exp, true);
}

function new_init_env() {

  var init_env = new Env();

  for (const [key, value] of Object.entries(ns)) {
    init_env.set(new MalSymbol(key), value);
  }

  init_env.set(new MalSymbol('eval'), function(ast) {
    return EVAL(ast, init_env);
  });
  return init_env;
}

var init_env = new_init_env();

function rep(str) {
  return PRINT(EVAL(READ(str), init_env));
}

/* The primordial core.clj */
rep("(def! load-file (fn* (f) (eval (read-string (str \"(do \" (slurp f) \"\nnil)\")))))");


function repl() {
  while(true) {
    var line = readline.readline("mal> ");
    if (null === line) { break; }
    if (line) { printer.println(rep(line)); }
  }
}

/* HACK: wrap the input in an implicit 'do' */
function wrapDo(str) {
  return "(do "+str+")";
}

function malCompile(inputElement, outputContainer) {
  outputContainer.innerHTML = rep(wrapDo(inputElement.value));
}
