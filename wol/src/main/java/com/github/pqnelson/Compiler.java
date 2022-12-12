package com.github.pqnelson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.pqnelson.js.AssignmentStatement;
import com.github.pqnelson.js.BlockStatement;
import com.github.pqnelson.js.IfStatement;
import com.github.pqnelson.js.Statement;

import com.github.pqnelson.js.JsExpr;
import com.github.pqnelson.js.Name;
import com.github.pqnelson.js.RefinementExpr;

import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.Fun;
import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.Map;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Symbol;
import com.github.pqnelson.expr.Vector;

/**
 * A Javascript compiler for Lisp S-expressions.
 */
public class Compiler {
    HashMap<Symbol, Expr> symbolTable;
    List<Statement> result;
    public boolean verbose = false;
    
    public Compiler(String source) {
        this.symbolTable = new HashMap<>();
        result = new ArrayList<>();
    }

    /**
     * Emit the Javascript statement for the Lisp-expression.
     */
    private Statement emit(Expr expr, Env env) {
        if (!expr.isList()) /* emitLiteral(expr, env); */ ;
        try {
            expr = Evaluator.macroexpand(expr, env);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        if (!expr.isList()) /* emitLiteral(expr, env); */ ;

        Seq ast = ((Seq) expr).slice(1);
        Expr rator = ((Seq) expr).rator();
        String s = rator.isSymbol() ? ((Symbol) rator).name() : "";
        switch (s) {
        case "def": return emitDef(rator, ast, env);
        case "let": return emitLet(rator, ast, env);
        case "do": return emitDo(rator, ast, env);
        case "if": return emitIf(rator, ast, env);
        case "fn*": return emitFn(rator, ast, env);
        default:
            return emitFunctionCall(rator, ast, env);
        }
    }

    Name emitName(Expr e) {
        assert (e.isSymbol());
        return new Name(((Symbol) e).name());
    }

    RefinementExpr emitLVal(Expr e) {
        assert (e.isSymbol());
        return new RefinementExpr(emitName(e));
    }

    JsExpr emitExpr(Expr e, Env env) {
        return null;
    }

    Statement emitDef(Expr rator, Seq ast, Env env) {
        assert (Symbol.DEF == rator);
        // (rator . ast) = (def name value)
        RefinementExpr name = emitLVal(ast.get(0));
        JsExpr body = emitExpr(ast.get(1), env);
        AssignmentStatement result = new AssignmentStatement(name, body);
        return result;
    }

    Statement emitLet(Expr rator, Seq ast, Env env) {
        assert (Symbol.LET_STAR == rator);
        // (rator . ast) = (let bindings body...)
        BlockStatement result = new BlockStatement();
        Vector bindings = (Vector) ast.get(0);
        Seq body = ast.slice(1);
        for (int i = 0; i < bindings.size(); i += 2) {
            Name name = emitName(bindings.get(i));
            JsExpr val = emitExpr(bindings.get(i+1), env);
            AssignmentStatement bind = new AssignmentStatement(name, val);
            result.append(bind);
        }
        for (Expr e : body) {
            result.append(emit(e, env));
        }
        return result;
    }

    Statement emitDo(Expr rator, Seq ast, Env env) {
        assert (Symbol.DO == rator);
        BlockStatement result = new BlockStatement();
        for (Expr e : ast) {
            result.append(emit(e, env));
        }
        return result;
    }

    Statement emitIf(Expr rator, Seq ast, Env env) {
        assert (Symbol.IF == rator);
        // (rator . ast) = (if test true-branch false??nil)
        JsExpr test = emitExpr(ast.get(0), env);
        Statement trueBranch = emit(ast.get(1), env);
        if (2 == ast.size()) {
            Statement falseBranch = emit(ast.get(2), env);
            return new IfStatement(test, trueBranch, falseBranch);
        } else {
            return new IfStatement(test, trueBranch);
        }
    }
    
    Statement emitFn(Expr rator, Seq ast, Env env) {
        assert (Symbol.FN_STAR == rator);
        return null;
    }

    Statement emitFunctionCall(Expr rator, Seq ast, Env env) {
        return null;
    }
    
}
