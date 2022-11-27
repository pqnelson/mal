package com.github.pqnelson;

import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.BigInt;
import com.github.pqnelson.expr.Float;
import com.github.pqnelson.expr.Fun;
import com.github.pqnelson.expr.ICountable;
import com.github.pqnelson.expr.Int;
import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.Map;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Str;
import com.github.pqnelson.expr.Symbol;
import com.github.pqnelson.expr.Vector;
import com.github.pqnelson.TokenType;

public class Core {
    private static void checkArity(int n, Seq args, String functionName)
            throws NoSuchMethodException {
        if (n != args.size()) {
            throw new NoSuchMethodException("Arity of function "+functionName+" is "+n+" but passed "+args.size()+" arguments");
        }
    }

    public static Expr count(Seq args) throws NoSuchMethodException {
        checkArity(1, args, "count");
        Expr e = args.first();
        ICountable coll = (ICountable)e;
        return new Int(coll.size());
    }

    public static Expr list_QMARK_(Seq args) throws NoSuchMethodException {
        checkArity(1, args, "list?");
        if (args.first().isList()) return Literal.T;
        return Literal.F;
    }

    public static Expr empty_QMARK_(Seq args) throws NoSuchMethodException {
        checkArity(1, args, "empty?");
        if ((args.first().isList() && ((Seq)(args.first())).isEmpty())
            || (args.first().isVector() && ((Vector)(args.first())).isEmpty())
            || (args.first().isMap() && ((Map)(args.first())).isEmpty())) {
            return Literal.T;
        }
        return Literal.F;
    }
}