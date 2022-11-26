package com.github.pqnelson;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.BigInt;
import com.github.pqnelson.expr.Float;
import com.github.pqnelson.expr.Fun;
import com.github.pqnelson.expr.Int;
import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Str;
import com.github.pqnelson.expr.Symbol;
import com.github.pqnelson.expr.Vector;
import com.github.pqnelson.TokenType;

public class Evaluator {

    boolean ratorIs(Expr expr, String name) {
        if (expr.isList() && ((Seq)expr).size() > 0
            && ((Seq)expr).first().isSymbol()) {
            Symbol symb = (Symbol)(((Seq)expr).first());
            return symb.name().equals(name);
        }
        return false;
    }
    boolean isUnquote(Expr expr) {
        return ratorIs(expr, "unquote");
    }

    private final Symbol symbol(String name) {
        return new Symbol(new Token(TokenType.IDENTIFIER, name, null, 0));
    }

    final Symbol cons = symbol("cons");
    final Symbol concat = symbol("concat");
    final Symbol quote = new Symbol(new Token(TokenType.QUOTE, "quote"));
    Expr quote(Expr e) {
        Seq result = new Seq();
        result.conj(quote);
        result.conj(e);
        return result;
    }

    void qqProcess(Seq acc, Expr e) {
        if (ratorIs(e, "unsplice")) {
            acc.prepend(((Seq)e).get(1));
            acc.prepend(concat);
        } else {
            acc.prepend(quasiquote(e));
            acc.prepend(cons);
        }
    }

    Expr quasiquote(Expr ast) {
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

    boolean isMacroCall(Expr ast, Env env) {
        if (ast.isList() && ((Seq)ast).first().isSymbol()) {
            Expr e = env.get((Symbol)(((Seq)ast).first()));
            return e.isFunction() && ((Fun)e).isMacro();
        }
        return false;
    }

    Expr macroexpand(Expr ast, Env env) {
        while(isMacroCall(ast,env)) {
            Symbol rator = (Symbol)((Seq)ast).rator();
            Fun macro = (Fun)env.get(rator);
            ast = macro.invoke(((Seq)ast).slice(1));
        }
        return ast;
    }

    Expr evalLiteral(Expr ast, Env env) {
        if (ast.isSymbol()) {
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


    Expr eval(Expr expr, Env env) {
        while (true) {
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
                Symbol name = (Symbol)ast.first();
                Expr body = ast.get(1);
                Expr value = eval(body, env);
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
                final Vector params = (Vector)ast.first();
                final Expr body = ast.get(1);
                final Env current = env;
                return new Fun () {
                    public Expr invoke(Seq args) {
                        return eval(body, new Env(current, params, args));
                    }
                };
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
                        !((Symbol)catchClause.first()).name().equals("catch")) throw e;

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
                    return f.invoke(args);
                } else { // interpreted function
                    expr = ast;
                    env = f.genEnv(env, args);
                }
            }
            }
        }
    }
}