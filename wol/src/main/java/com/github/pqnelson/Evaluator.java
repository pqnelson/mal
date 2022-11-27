package com.github.pqnelson;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.BigInt;
import com.github.pqnelson.expr.Float;
import com.github.pqnelson.expr.Fun;
import com.github.pqnelson.expr.ICountable;
import com.github.pqnelson.expr.IFn;
import com.github.pqnelson.expr.Int;
import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.Map;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Str;
import com.github.pqnelson.expr.Symbol;
import com.github.pqnelson.expr.Vector;
import com.github.pqnelson.TokenType;

public class Evaluator {

    static boolean ratorIs(Expr expr, String name) {
        if (expr.isList() && ((Seq)expr).size() > 0
                && ((Seq)expr).first().isSymbol()) {
            Symbol symb = (Symbol)(((Seq)expr).first());
            return symb.name().equals(name);
        }
        return false;
    }
    static boolean isUnquote(Expr expr) {
        return ratorIs(expr, "unquote");
    }

    private static final Symbol symbol(String name) {
        return new Symbol(new Token(TokenType.IDENTIFIER, name, null, 0));
    }

    static final Symbol cons = symbol("cons");
    static final Symbol concat = symbol("concat");
    static final Symbol quote = new Symbol(new Token(TokenType.QUOTE, "quote"));

    static Expr quote(Expr e) {
        Seq result = new Seq();
        result.conj(quote);
        result.conj(e);
        return result;
    }

    static void qqProcess(Seq acc, Expr e) {
        if (ratorIs(e, "unsplice")) {
            acc.prepend(((Seq)e).get(1));
            acc.prepend(concat);
        } else {
            acc.prepend(quasiquote(e));
            acc.prepend(cons);
        }
    }

    static Expr quasiquote(Expr ast) {
        if (isUnquote(ast)) {
            return ((Seq)ast).get(1);
        } else if (ast.isList()) {
            Seq acc = new Seq();
            for (Expr e : ((Seq)ast)) {
                qqProcess(acc, e);
            }
            return acc;
        } else if (ast.isSymbol()) {
            return quote(ast);
        } else {
            return ast;
        }
    }

    static boolean isMacroCall(Expr ast, Env env) {
        if (ast.isList() && ((Seq)ast).first().isSymbol() &&
                !((Symbol)((Seq)ast).first()).isSpecialForm()) {
            Expr e = env.get((Symbol)(((Seq)ast).first()));
            return e.isFunction() && ((Fun)e).isMacro();
        }
        return false;
    }

    static Expr macroexpand(Expr ast, Env env) throws Throwable {
        while(isMacroCall(ast, env)) {
            Symbol rator = (Symbol)((Seq)ast).rator();
            Fun macro = (Fun)env.get(rator);
            ast = macro.invoke(((Seq)ast).slice(1));
        }
        return ast;
    }
    public static boolean debug = false;

    static Expr evalLiteral(Expr ast, Env env) throws Throwable {
        if (ast.isSymbol()) {
            if (debug) { System.out.println("evalLiteral() getting value of symbol '"+
                                            ast.toString()+"' from environment"); }
            return env.get((Symbol)ast);
        } else if (ast.isList()) {
            Seq result = new Seq();
            for (Expr e : (Seq)ast) {
                result.conj(eval(e, env));
            }
            return result;
        } else if (ast.isVector()) {
            Vector result = new Vector();
            for (Expr e : (Vector)ast) {
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
            if (debug) { System.out.println("eval given expr = "+expr.toString()); }
            if (!expr.isList()) {
                return evalLiteral(expr, env);
            }
            /* begin macroexpansion */
            expr = macroexpand(expr, env);
            if (!expr.isList()) {
                return evalLiteral(expr, env);
            }
            /* end macroexpansion */
            Seq ast = ((Seq)expr).slice(1);
            Expr rator = ((Seq)expr).rator();
            String s = rator.isSymbol() ? ((Symbol)rator).name() : "";
            switch(s) {
            case "def": {
                assert (2 == ast.size()) : "def has "+ast.size()+" operands instead of 2";
                /* the reader assembles (def ^:foo bar spam) as
                   (def (with-meta bar :foo) spam), where
                   (with-meta ...) is a secret macro. */
                Symbol name = (Symbol)macroexpand(ast.first(), env);
                Expr value = eval(ast.get(1), env);
                env.set(name, value);
                return value;
            }
            case "let*": {
                Vector bindings = (Vector)ast.first();
                Seq body = ast.slice(1);
                Symbol key;
                Expr val;
                Env letEnv = new Env(env);
                for (int i=0; i < bindings.size(); i+=2) {
                    key = (Symbol)bindings.get(i);
                    val = bindings.get(i+1);
                    letEnv.set(key, eval(val, letEnv));
                }
                for (int i=2; i < ast.size()-1; i++) {
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
                if (test.isLiteral() && ((Literal)test).isFalsy()) {
                    if (ast.size() > 1) {
                        expr = ast.get(2);
                    } else {
                        return null;
                    }
                } else {
                    expr = ast.get(1);
                }
                break;
            }
            case "fn*": {
                // ast ::= (name [params] body) OR ([params] body)
                Symbol name = (ast.first().isSymbol() ? (Symbol)ast.first() : null);
                final Vector params = (Vector)(null == name ? ast.first() : ast.get(1));
                final Seq body = (Seq)(null == name ? ast.get(1) : ast.get(2));
                final Env current = env;
                IFn f = (args) -> eval(body, new Env(current, params, args));
                return new Fun (f, params, body, name);
            }
            case "macroexpand":
                return macroexpand(ast.first(), env);
            case "quote":
                return ast.first();
            case "quasiquote-expand":
                return quasiquote(ast.first());
            case "quasiquote":
                expr = quasiquote(ast.first());
                break;
            case "defmacro":
                // ast = (macro-name [params] body)
                Symbol name = (Symbol)ast.first();
                Seq littleMac = ast.slice(1); // = ([params] body)
                littleMac.prepend(new Symbol(new Token(TokenType.FN_STAR, "fn*")));
                Expr macro = eval(littleMac, env); // = eval (fn* [params] body)
                env.set(name, macro);
                return macro;
            case "try": {
                // ast = (body (catch e catch-body...))
                try {
                    return eval(ast.first(), env);
                } catch (Throwable e) {
                    // cases when the exception isn't handled
                    if (null == ast.get(1)) throw e;
                    if (!ast.get(1).isList()) throw e;
                    Seq catchClause = (Seq)ast.get(1);
                    if (!catchClause.first().isSymbol() ||
                            !((Symbol)catchClause.first()).name().equals("catch"))
                        throw e;

                    // OK, so, ast handles the exception properly
                    Expr catchBody = catchClause.get(2);
                    Symbol exceptionId = (Symbol)catchClause.get(1);
                    String stacktrace = Arrays.stream(e.getStackTrace())
                        .map(line -> line.toString())
                        .collect(Collectors.joining("\n"));
                    Env eenv = new Env(env);
                    eenv.set(exceptionId, new Str(stacktrace));
                    expr = catchBody;
                    env = eenv;
                    break;
                }
            }
            default: {
                Seq args = (Seq)evalLiteral(ast, env);
                Fun f = (Fun)(evalLiteral(rator, env));
                if (!f.isInterpreted()) { // "compiled" function
                    if (debug) System.out.println("Executing a compiled function");
                    if (debug) System.out.println("args = "+args.toString());
                    Expr result = f.invoke(args);
                    if (debug) System.out.println("Returning "+result.toString());
                    return result;
                } else { // interpreted function
                    if (debug) System.out.println("Executing an interpreted function: "+ast.toString());
                    expr = ast;
                    env = f.genEnv(env, args);
                }
            }
            }
        }
    }


    public static Env initialEnv() {
        Env env = new Env();
        env.set(new Symbol("+"), new Fun(Core::add));
        env.set(new Symbol("-"), new Fun(Core::subtract));
        env.set(new Symbol("*"), new Fun(Core::multiply));
        env.set(new Symbol("/"), new Fun(Core::divide));
        env.set(new Symbol("count"), new Fun(Core::count));
        env.set(new Symbol("list"), new Fun((Seq args) -> args));
        env.set(new Symbol("list?"), new Fun(Core.list_QMARK_));
        env.set(new Symbol("empty?"), new Fun(Core::empty_QMARK_));
        env.set(new Symbol("seq"), new Fun(Core::seq));
        env.set(new Symbol("first"), new Fun(Core::first));
        env.set(new Symbol("nth"), new Fun(Core::nth));
        env.set(new Symbol("rest"), new Fun(Core::rest));
        env.set(concat, new Fun(Core::concat));
        env.set(cons, new Fun(Core::cons));
        env.set(new Symbol("="), new Fun(Core::equality));
        env.set(new Symbol("nil?"), new Fun(Core.nil_QMARK_));
        env.set(new Symbol("true?"), new Fun(Core.true_QMARK_));
        env.set(new Symbol("false?"), new Fun(Core.false_QMARK_));
        env.set(new Symbol("symbol?"), new Fun(Core.symbol_QMARK_));
        env.set(new Symbol("symbol"), new Fun(Core::symbol));
        env.set(new Symbol("keyword?"), new Fun(Core.keyword_QMARK_));
        env.set(new Symbol("keyword"), new Fun(Core::keyword));
        env.set(new Symbol("vector?"), new Fun(Core.vector_QMARK_));
        env.set(new Symbol("vector"), new Fun(Core::vector));
        env.set(new Symbol("map?"), new Fun(Core.map_QMARK_));
        env.set(new Symbol("string?"), new Fun(Core.string_QMARK_));
        env.set(new Symbol("fn?"), new Fun(Core.fn_QMARK_));
        env.set(new Symbol("println"), new Fun(Core::println));
        env.set(new Symbol("str"), new Fun(Core::str));
        env.set(new Symbol("hash-map"), new Fun(Core::hash_map));
        env.set(new Symbol("get"), new Fun(Core::get));
        env.set(new Symbol("assoc"), new Fun(Core::assoc));
        env.set(new Symbol("assoc!"), new Fun(Core::assoc_BANG_));
        env.set(new Symbol("dissoc"), new Fun(Core::dissoc));
        env.set(new Symbol("dissoc!"), new Fun(Core::dissoc_BANG_));
        env.set(new Symbol("contains?"), new Fun(Core::contains_QMARK_));
        env.set(new Symbol("keys"), new Fun(Core::keys));
        env.set(new Symbol("vals"), new Fun(Core::vals));

        return env;
    }
}