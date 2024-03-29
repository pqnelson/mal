package com.github.pqnelson;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.Fun;
import com.github.pqnelson.expr.IFn;
import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.Map;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Str;
import com.github.pqnelson.expr.Symbol;
import com.github.pqnelson.expr.Vector;

public class Evaluator {

    private Evaluator() { }

    static boolean ratorIs(final Expr expr, final Symbol s) {
        return  (expr.isList() && ((Seq)expr).size() > 0
                 && ((Seq)expr).first().equals(s));
    }

    private static Symbol symbol(final String name) {
        return new Symbol(name);
    }

    static final Symbol cons = symbol("cons");
    static final Symbol concat = symbol("concat");

    static Expr quote(final Expr e) {
        Seq result = new Seq();
        result.conj(Symbol.QUOTE);
        result.conj(e);
        return result;
    }

    static Seq qqProcess(final Seq acc, final Expr e) {
        Seq result = new Seq();
        if (ratorIs(e, Symbol.SPLICE)) {
            result.conj(concat);
            result.conj(((Seq) e).get(1));
        } else {
            result.conj(cons);
            result.conj(quasiquote(e));
        }
        result.conj(acc);
        return result;
    }

    /**
     * Convoluted quasiquote process.
     *
     * <p>I could spend a lifetime exploring this domain space, but
     * basically what happens is that {@code `(1 2 3)} is interpreted as
     * {@code (cons 1 (cons 2 (cons 3 (list))))}. Compare this to Clojure, whic
     * would interpret it as {@code (concat (list 1) (list 2) (list 3))}.
     * Probably that is much cleaner:
     * {@code ast.map(l => (list quasiquote(l))).cons(concat)}.</p>
     */
    static Expr quasiquote(final Expr ast) {
        if (ast.isLiteral()) {
            return ast;
        } else if (ast.isSymbol() || ast.isMap()) {
        // BUG: a map should expand each key-value pair.
            return quote(ast);
        } else if (ratorIs(ast, Symbol.UNQUOTE)) {
            return ((Seq) ast).get(1);
        } else if (ast.isList() || ast.isVector()) {
            if ((ast.isList() && ((Seq) ast).isEmpty())
                || (ast.isVector() && ((Vector) ast).isEmpty())) {
                return quote(ast);
            }
            Seq acc = new Seq();
            acc.conj(new Symbol("list"));
            final Seq _ast = (Seq) ast;
            for (int i = _ast.size() - 1; i >= 0; i--) {
                Expr e = _ast.get(i);
                acc = qqProcess(acc, e);
            }
            if (ast.isVector()) {
                acc.prepend(new Symbol("vec"));
            }
            return acc;
        } else {
            // default case:
            return ast;
        }
    }

    static boolean isMacroCall(final Expr _ast, final Env env) {
        if (!_ast.isList()) {
            return false;
        }
        final Seq ast = (Seq) _ast;
        if (null == ast.first() || !(ast.first().isSymbol())) {
            return false;
        }
        final Symbol head = ((Symbol) ast.first());
        if (!head.isSpecialForm()) {
            final Expr e = env.get(head);
            if (!e.isFunction()) {
                return false;
            }
            return ((Fun) e).isMacro();
        }
        return false;
    }

    static Expr macroexpand(Expr ast, final Env env) throws Throwable {
        while(isMacroCall(ast, env)) {
            final Symbol rator = (Symbol) ((Seq) ast).rator();
            final Fun macro = (Fun) env.get(rator);
            ast = macro.invoke(((Seq) ast).slice(1));
        }
        return ast;
    }
    public static boolean debug = false;

    static Expr evalLiteral(final Expr ast, final Env env) throws Throwable {
        if (ast.isSymbol()) {
            if (debug) {
                System.out.println("evalLiteral() getting value of symbol '"
                                   + ast.toString()
                                   + "' from environment");
            }
            return env.get((Symbol) ast);
        } else if (ast.isList()) {
            Seq result = new Seq();
            for (Expr e : (Seq) ast) {
                result.conj(eval(e, env));
            }
            return result;
        } else if (ast.isVector()) {
            Vector result = new Vector();
            for (Expr e : (Vector) ast) {
                result.conj(eval(e, env));
            }
            return result;
        } else {
            return ast;
        }
    }

    /**
     * Evaluate an expression relative to a given binding environment.
     *
     * This is almost certainly what you are looking for.
     */
    public static Expr eval(Expr expr, Env env) throws Throwable {
        while (true) {
            if (debug) {
                System.out.println("eval given expr = " + expr);
            }
            if (!expr.isList()) {
                return evalLiteral(expr, env);
            }
            /* begin macroexpansion */
            expr = macroexpand(expr, env);
            if (!expr.isList()) {
                return evalLiteral(expr, env);
            }
            /* end macroexpansion */
            Seq ast = ((Seq) expr).slice(1);
            Expr rator = ((Seq) expr).rator();
            String s = rator.isSymbol() ? ((Symbol) rator).name() : "";
            switch (s) {
            case "def": {
                assert (2 == ast.size())
                    : "def has " + ast.size() + " operands instead of 2";
                /* the reader assembles (def ^:foo bar spam) as
                   (def (with-meta bar :foo) spam), where
                   (with-meta ...) is a secret macro. */
                final Symbol name = (Symbol) macroexpand(ast.first(), env);
                final Expr value = eval(ast.get(1), env);
                env.set(name, value);
                return value;
            }
            case "let*": {
                final Vector bindings = (Vector) ast.first();
                final Seq body = ast.slice(1);
                Symbol key;
                Expr val;
                Env letEnv = new Env(env);
                for (int i = 0; i < bindings.size(); i += 2) {
                    key = (Symbol) bindings.get(i);
                    val = bindings.get(i + 1);
                    letEnv.set(key, eval(val, letEnv));
                }
                for (int i = 2; i < ast.size() - 1; i++) {
                    eval(ast.get(i), letEnv);
                }
                expr = ast.last();
                env = letEnv;
                break;
            }
            case "do": {
                evalLiteral(ast.butLast(), env);
                expr = ast.last();
                break;
            }
            case "if": {
                // ast = (test true-branch false-branch?)
                Expr test = eval(ast.get(0), env);
                if (test.isLiteral() && ((Literal) test).isFalsy()) {
                    expr = ast.get(2, Literal.NIL);
                } else {
                    expr = ast.get(1);
                }
                break;
            }
            case "fn*": {
                // ast ::= ([params] body) OR (name [params] body)
                final Symbol name = (ast.first().isSymbol()
                                     ? (Symbol) ast.first()
                                     : null);
                final Vector params = (Vector) (null == name
                                                ? ast.first()
                                                : ast.get(1));
                Expr body;
                final int nameOffset = (null == name ? 0 : 1);
                final int bodyIndex = nameOffset + 1;
                if (1 + bodyIndex == ast.size()) {
                    body = ast.get(bodyIndex);
                } else {
                    body = (Seq) ast.slice(bodyIndex);
                    ((Seq) body).prepend(Symbol.DO);
                }
                Env current = env;
                Fun fn;
                if (null != name) { // recursive functions use their own name
                    fn = new Fun (name);
                    current.set(name, fn);
                    IFn f = (args) -> eval(body, new Env(current, params, args));
                    fn.addMethod(f, params, body);
                } else { // no name? no recursion!
                    IFn f = (args) -> eval(body, new Env(current, params, args));
                    fn = new Fun (f, params, body, name);
                }
                return fn;
            }
            case "macroexpand":       return macroexpand(ast.first(), env);
            case "quote":             return ast.first();
            case "quasiquote-expand": return quasiquote(ast.first());
            case "quasiquote":
                expr = quasiquote(ast.first());
                break;
            case "defmacro": {
                // ast = (macro-name [params] body)
                assert (ast.get(0).isSymbol()) : "macro name is not a symbol";
                assert (ast.get(1).isVector()) : "Args are not a vector";
                assert (ast.size() >= 2) : "macro has no body";
                PPrinter p = new PPrinter();
                Symbol name = (Symbol) ast.first();
                Seq littleMac = ast.slice(1); // = ([params] body)
                littleMac.prepend(Symbol.FN_STAR);
                Fun macro = (Fun) eval(littleMac, env);
                // macro = eval (fn* [params] body)
                macro.setMacro();
                env.set(name, macro);
                return macro;
            }
            case "try": {
                // ast = (body... (catch e catch-body...)+)
                /* CAVEAT: it will process the body, which is the sublist of
                   expressions UNTIL the first {@code catch} is encountered.
                   This is a "bug". I really want to support a richer
                   exception handling framework, but this is the current
                   state of things.
                */
                Predicate<Expr> isCatchClause = (ex) ->
                    ex.isList() && Symbol.CATCH == ((Seq) ex).first();
                try {
                    Seq body = ast.takeWhile(isCatchClause.negate());
                    body.prepend(Symbol.DO);
                    return eval(body, env);
                } catch (Throwable e) {
                    // cases when the exception isn't handled
                    if ((null == ast.get(1))
                        || (!ast.get(1).isList())) {
                        throw e;
                    }

                    Seq catchClauses = ast.filter(isCatchClause);
                    if (catchClauses.isEmpty()) {
                        throw e;
                    }
                    // OK, so, ast handles the exception properly
                    // catchClause ~ (catch e catch-body...)
                    Seq catchClause = (Seq) (catchClauses.first());
                    String stacktrace = Arrays.stream(e.getStackTrace())
                        .map(line -> line.toString())
                        .collect(Collectors.joining("\n"));
                    Map meta = new Map();
                    meta.assoc(new Keyword("stacktrace"), new Str(stacktrace));
                    Symbol exceptionId = (Symbol) catchClause.get(1);
                    exceptionId = (Symbol) (exceptionId.withMeta(meta));
                    // bind the exception identifier in a new Env
                    Env eenv = new Env(env);
                    eenv.set(exceptionId, new Str(e.getMessage()));
                    env = eenv;
                    // set the expr to `(do catch-body...)`
                    Seq catchBody = catchClause.slice(2);
                    catchBody.prepend(Symbol.DO);
                    expr = catchBody;
                }
                break;
            }
            default: {
                Seq args = (Seq) evalLiteral(ast, env);
                Fun f = (Fun) (eval(rator, env));
                if (!f.isInterpreted(args.size())) { // "compiled" function
                    return f.invoke(args);
                } else { // interpreted function
                    expr = f.getBody(args.size());
                    env = f.genEnv(env, args);
                }
            }
            }
        }
    }


    public static Env initialEnv() {
        Env env = new Env();
        env.set(new Symbol("+"), new Fun(Core::add, 0, true, new Symbol("+")));
        env.set(new Symbol("-"),
                new Fun(Core::subtract, 1, true, new Symbol("-")));
        env.set(new Symbol("*"), new Fun(Core::multiply, 0, true, new Symbol("*")));
        env.set(new Symbol("/"), new Fun(Core::divide, 0, true, new Symbol("/")));
        env.set(new Symbol("count"), new Fun(Core::count, 1, false, new Symbol("count")));
        env.set(new Symbol("list"),
                new Fun((Seq args) -> args, 0, true, new Symbol("list")));
        env.set(new Symbol("list?"),
                new Fun(Core.list_QMARK_, 1, false, new Symbol("list?")));
        env.set(new Symbol("empty?"),
                new Fun(Core::empty_QMARK_, 1, false, new Symbol("empty?")));
        env.set(new Symbol("seq"), new Fun(Core::seq, 1, false, new Symbol("seq")));
        env.set(new Symbol("first"), new Fun(Core::first, 1, false, new Symbol("first")));
        env.set(new Symbol("nth"), new Fun(Core::nth, 2, false, new Symbol("nth")));
        env.set(new Symbol("rest"), new Fun(Core::rest, 1, false, new Symbol("rest")));
        env.set(concat, new Fun(Core::concat, 0, true, concat));
        env.set(cons, new Fun(Core::cons, 2, false, cons));
        env.set(new Symbol("="), new Fun(Core::equality, 0, true, new Symbol("=")));
        env.set(new Symbol("<"), new Fun(Core::LT, 0, true, new Symbol("<")));
        env.set(new Symbol("<="), new Fun(Core::LEQ, 0, true, new Symbol("<=")));
        env.set(new Symbol(">"), new Fun(Core::GT, 0, true, new Symbol(">")));
        env.set(new Symbol(">="), new Fun(Core::GEQ, 0, true, new Symbol(">=")));
        env.set(new Symbol("nil?"),
                new Fun(Core.nil_QMARK_, 1, false, new Symbol("nil?")));
        env.set(new Symbol("true?"),
                new Fun(Core.true_QMARK_, 1, false, new Symbol("true?")));
        env.set(new Symbol("false?"),
                new Fun(Core.false_QMARK_, 1, false, new Symbol("false?")));
        env.set(new Symbol("symbol?"),
                new Fun(Core.symbol_QMARK_, 1, false, new Symbol("symbol?")));
        env.set(new Symbol("symbol"),
                new Fun(Core::symbol, 1, false, new Symbol("symbol")));
        env.set(new Symbol("keyword?"),
                new Fun(Core.keyword_QMARK_, 1, false, new Symbol("keyword?")));
        env.set(new Symbol("keyword"),
                new Fun(Core::keyword, 1, false, new Symbol("keyword")));
        env.set(new Symbol("vector?"),
                new Fun(Core.vector_QMARK_, 1, false, new Symbol("vector?")));
        env.set(new Symbol("vector"),
                new Fun(Core::vector, 0, true, new Symbol("vector")));
        env.set(new Symbol("map?"),
                new Fun(Core.map_QMARK_, 1, false, new Symbol("map?")));
        env.set(new Symbol("string?"),
                new Fun(Core.string_QMARK_, 1, false, new Symbol("string?")));
        env.set(new Symbol("fn?"),
                new Fun(Core.fn_QMARK_, 1, false, new Symbol("fn?")));
        env.set(new Symbol("println"),
                new Fun(Core::println, 0, true, new Symbol("println")));
        env.set(new Symbol("pr-str"),
                new Fun(Core::pr_str, 0, true, new Symbol("pr-str")));
        env.set(new Symbol("prn"), new Fun(Core::prn, 0, true, new Symbol("prn")));
        env.set(new Symbol("str"), new Fun(Core::str, 0, true, new Symbol("str")));
        env.set(new Symbol("hash-map"),
                new Fun(Core::hash_map, 0, true, new Symbol("hash-map")));
        env.set(new Symbol("get"), new Fun(Core::get, 2, false, new Symbol("get")));
        env.set(new Symbol("assoc"),
                new Fun(Core::assoc, 3, true, new Symbol("assoc")));
        env.set(new Symbol("assoc!"),
                new Fun(Core::assoc_BANG_, 3, true, new Symbol("assoc!")));
        env.set(new Symbol("dissoc"),
                new Fun(Core::dissoc, 2, true, new Symbol("dissoc")));
        env.set(new Symbol("dissoc!"),
                new Fun(Core::dissoc_BANG_, 2, true, new Symbol("dissoc!")));
        env.set(new Symbol("contains?"),
                new Fun(Core::contains_QMARK_, 2, false, new Symbol("contains?")));
        env.set(new Symbol("keys"),
                new Fun(Core::keys, 1, false, new Symbol("keys")));
        env.set(new Symbol("vals"),
                new Fun(Core::vals, 1, false, new Symbol("vals")));
        env.set(new Symbol("read-string"),
                new Fun(Core::read_string, 1, false, new Symbol("read-string")));
        env.set(new Symbol("slurp"),
                new Fun(Core::slurp, 1, false, new Symbol("slurp")));
        env.set(new Symbol("list"),
                new Fun(Core::list, 0, true, new Symbol("list")));
        env.set(new Symbol("throw"),
                new Fun(Core::_throw, 0, true, new Symbol("throw")));

        return env;
    }
}
