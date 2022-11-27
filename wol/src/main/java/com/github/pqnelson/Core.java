package com.github.pqnelson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

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
import com.github.pqnelson.annotations.VisibleForTesting;

public class Core {
    @VisibleForTesting
    static Function<Boolean, Expr> boxBool
        = (Boolean b) -> (b.equals(Boolean.TRUE) ? Literal.T : Literal.F);

    private static void checkArity(int n, Seq args, String functionName)
            throws NoSuchMethodException {
        if (n != args.size()) {
            throw new NoSuchMethodException("Arity of function "+functionName+" is "+n+" but passed "+args.size()+" arguments");
        }
    }
    private static String aritiesToStr(Set<Integer> arities) {
        StringBuffer buf = new StringBuffer("{");
        for (int i : arities) {
            buf.append(Integer.toString(i));
            buf.append(", ");
        }
        buf.delete(buf.length()-2, buf.length()-1);
        buf.append("}");
        return buf.toString();
    }
    private static void checkArities(Set<Integer> arities, Seq args, String functionName)
            throws NoSuchMethodException {
        if (!arities.contains(args.size())) {
            throw new NoSuchMethodException("Arities of function "+functionName+" are "+aritiesToStr(arities)+" but passed "+args.size()+" arguments");
        }
    }

    private static IFn predicateFactory(String predicateName,
                                        Predicate<Expr> pred) {
        return (Seq args) -> {
            checkArity(1, args, predicateName+"_QMARK_");
            return (pred.test(args.first()) ? Literal.T : Literal.F);
        };
    }

    /**
     * Now we have publicly exported functions.
     */
    public static Expr add(Seq args) throws NoSuchMethodException {
        com.github.pqnelson.expr.Number sum = new Int(0L);
        for(Expr e : args) {
            sum = sum.add((com.github.pqnelson.expr.Number)e);
        }
        return sum;
    }

    public static Expr subtract(Seq args) throws NoSuchMethodException {
        if (0 == args.size()) return Literal.ZERO;
        com.github.pqnelson.expr.Number sum = (com.github.pqnelson.expr.Number)args.first();
        for(Expr e : args.slice(1)) {
            sum = sum.subtract((com.github.pqnelson.expr.Number)e);
        }
        return sum;
    }

    public static Expr multiply(Seq args) throws NoSuchMethodException {
        if (0 == args.size()) return new Int(1L);
        com.github.pqnelson.expr.Number product = new Int(1L);
        for(Expr e : args) {
            product = product.multiply((com.github.pqnelson.expr.Number)e);
        }
        return product;
    }
    public static Expr divide(Seq args) throws NoSuchMethodException {
        if (0 == args.size()) return Literal.ONE;
        if (1 == args.size()) {
            return (new Int(1L)).divide((com.github.pqnelson.expr.Number)(args.first()));
        }
        com.github.pqnelson.expr.Number quotient = (com.github.pqnelson.expr.Number)args.first();
        for(Expr e : args.slice(1)) {
            quotient = quotient.divide((com.github.pqnelson.expr.Number)e);
        }
        return quotient;
    }

    public static Expr count(Seq args) throws NoSuchMethodException {
        checkArity(1, args, "count");
        Expr e = args.first();
        ICountable coll = (ICountable)e;
        return new Int(coll.size());
    }

    public static IFn list_QMARK_ = predicateFactory("list", Expr::isList);

    public static Expr empty_QMARK_(Seq args) throws NoSuchMethodException {
        checkArity(1, args, "empty?");
        if ((args.first().isList() && ((Seq)(args.first())).isEmpty())
            || (args.first().isVector() && ((Vector)(args.first())).isEmpty())
            || (args.first().isMap() && ((Map)(args.first())).isEmpty())) {
            return Literal.T;
        }
        return Literal.F;
    }

    private static Expr _seqArg(Expr arg) throws NoSuchMethodException {
        if (arg.isList()) return ((Seq)arg).seq();
        if (arg.isNil()) return arg;
        if (arg.isVector()) return ((Vector)arg).seq();
        if (arg.isMap()) return ((Map)arg).seq();
        if (arg.isString()) return ((Str)arg).seq();
        throw new NoSuchMethodException("seq called on unknown type "+arg.type());
    }

    public static Expr seq(Seq args) throws NoSuchMethodException {
        checkArity(1, args, "seq");
        return _seqArg(args.first());
    }

    public static Expr first(Seq args) throws NoSuchMethodException {
        checkArity(1, args, "first");
        Expr arg = _seqArg(args.first());
        if (arg.isList()) return ((Seq)arg).first();
        if (arg.isNil()) return arg;
        throw new NoSuchMethodException("first called on unknown type "+arg.type());
    }

    public static Expr rest(Seq args) throws NoSuchMethodException {
        checkArity(1, args, "rest");
        Expr arg = _seqArg(args.first());
        if (arg.isList()) return ((Seq)arg).slice(1);
        if (arg.isNil()) return arg;
        throw new NoSuchMethodException("rest called on unknown type "+arg.type());
    }

    public static Expr nth(Seq args) throws NoSuchMethodException {
        checkArities(Set.of(2,3), args, "nth");
        if (!args.get(1).isInt()) {
            throw new NoSuchMethodException("nth called with non-int index, received "+args.get(1).type());
        }
        int i = ((Int)args.get(1)).value().intValue();
        Expr arg = args.first();
        Expr defaultValue = args.get(2, Literal.NIL);
        if (arg.isMap() || arg.isString()) arg = _seqArg(arg);
        if (arg.isNil()) return defaultValue;
        if (arg.isList()) return ((Seq)arg).get(i, defaultValue);
        if (arg.isVector()) return ((Vector)arg).get(((Int)args.get(1)), defaultValue);
        throw new NoSuchMethodException("nth called on unknown type "+arg.type());
    }

    public static Expr concat(Seq args) throws NoSuchMethodException {
        Expr arg = args.first();
        if (args.isEmpty()) return Literal.NIL;
        Expr seqArg = _seqArg(arg);
        if (seqArg.isNil()) seqArg = new Seq();
        Seq result = (Seq)seqArg;
        for (Expr e : args.slice(1)) {
            result = result.concat(_seqArg(e));
        }
        return result;
    }

    public static Expr cons(Seq args) throws NoSuchMethodException {
        checkArity(2, args, "cons");
        Expr e = _seqArg(args.get(1));
        if (e.isNil()) e = new Seq();
        Seq coll = (Seq)e;
        return coll.cons(args.first());
    }

    public static Expr equality(Seq args) throws NoSuchMethodException {
        switch (args.size()) {
        case 0: return Literal.T;
        case 1: return Literal.T;
        case 2: return (args.get(0).equals(args.get(1)) ? Literal.T : Literal.F);
        default:
            for (int i = 0; i < args.size() - 1; i++) {
                if (!args.get(i).equals(args.get(i+1))) {
                    return Literal.F;
                }
            }
            return Literal.T;
        }
    }
    public static IFn nil_QMARK_ = predicateFactory("nil", Expr::isNil);
    public static IFn true_QMARK_ = predicateFactory("true", Literal::exprIsTrue);
    public static IFn false_QMARK_ = predicateFactory("false", Literal::exprIsFalse);
    public static IFn symbol_QMARK_ = predicateFactory("symbol", Expr::isSymbol);
    public static Expr symbol(Seq args) throws NoSuchMethodException {
        checkArity(1, args, "symbol");
        Expr e = args.first();
        if (e.isSymbol()) return e;
        if (e.isKeyword()) return ((Keyword)e).symbol();
        if (e.isString()) return new Symbol(((Str)e).value());
        throw new IllegalArgumentException("no conversion of "+e.type()+" to symbol");
    }
    public static IFn keyword_QMARK_ = predicateFactory("keyword", Expr::isKeyword);
    public static Expr keyword(Seq args) throws NoSuchMethodException {
        checkArity(1, args, "keyword");
        Expr e = args.first();
        if (e.isSymbol()) return new Keyword(((Symbol)e).name());
        if (e.isKeyword()) return e;
        if (e.isString()) return new Keyword(((Str)e).value());
        throw new IllegalArgumentException("no conversion of "+e.type()+" to keyword");
    }
    public static Expr vector(Seq args) throws NoSuchMethodException {
        return args.vec();
    }
    public static IFn vector_QMARK_ = predicateFactory("vector", Expr::isVector);

    public static IFn map_QMARK_ = predicateFactory("map", Expr::isMap);
    public static IFn string_QMARK_ = predicateFactory("string", Expr::isString);
    public static IFn fn_QMARK_ = predicateFactory("fn", Expr::isFunction);
    public static Expr println(Seq args) throws NoSuchMethodException {
        StringBuffer buf = new StringBuffer();
        Printer p = new Printer();
        for (Expr e : args) {
            buf.append(e.accept(p));
            buf.append(" ");
        }
        buf.deleteCharAt(buf.length()-1);
        System.out.println(buf.toString());
        return Literal.NIL;
    }
    public static Expr pr_str(Seq args) throws NoSuchMethodException {
        StringBuffer buf = new StringBuffer();
        PPrinter p = new PPrinter();
        for (Expr e : args) {
            buf.append(e.accept(p));
            buf.append(" ");
        }
        buf.deleteCharAt(buf.length()-1);
        return new Str(buf.toString());
    }
    public static Expr prn(Seq args) throws NoSuchMethodException {
        StringBuffer buf = new StringBuffer();
        PPrinter p = new PPrinter();
        for (Expr e : args) {
            buf.append(e.accept(p));
            buf.append(" ");
        }
        buf.deleteCharAt(buf.length()-1);
        System.out.println(buf.toString());
        return Literal.NIL;
    }
    public static Expr str(Seq args) throws NoSuchMethodException {
        Printer printer = new Printer();
        StringBuffer buf = new StringBuffer();
        for (Expr e : args) {
            buf.append(e.accept(printer));
        }
        return new Str(buf.toString());
    }
    public static Expr hash_map(Seq args) throws NoSuchMethodException {
        if (0 != args.size() % 2) throw new NoSuchMethodException("hash-map requires an even number of bindings");
        Map map = new Map();
        for (int i = 0; i < args.size(); i += 2) {
            map.assoc(args.get(i), args.get(i+1));
        }
        return map;
    }
    public static Expr get(Seq args) throws NoSuchMethodException {
        checkArities(Set.of(2,3), args, "get");
        Expr coll = args.first();
        Expr i = args.get(1);
        Expr defaultValue = args.get(2, Literal.NIL);
        if (coll.isMap()) return ((Map)coll).get(i, defaultValue);
        if (coll.isVector()) return ((Vector)coll).get(i, defaultValue);
        if (coll.isNil()) return defaultValue;
        throw new NoSuchMethodException("get applied to unexpected type '"+coll.type()+"'");
    }
    public static Expr assoc(Seq args) throws NoSuchMethodException {
        if (3 > args.size()) throw new NoSuchMethodException("assoc requires at least a map, a key, a value; but received "+args.size()+" arguments");
        if (1 != args.size() % 2) throw new NoSuchMethodException("assoc requires an even number of bindings");
        Map map = new Map((Map)args.first());
        for (int i = 1; i < args.size(); i += 2) {
            map.assoc(args.get(i), args.get(i+1));
        }
        return map;
    }
    public static Expr assoc_BANG_(Seq args) throws NoSuchMethodException {
        if (3 > args.size()) throw new NoSuchMethodException("assoc requires at least a map, a key, a value; but received "+args.size()+" arguments");
        if (1 != args.size() % 2) throw new NoSuchMethodException("assoc requires an even number of bindings");
        Map map = (Map)args.first();
        for (int i = 1; i < args.size(); i += 2) {
            map.assoc(args.get(i), args.get(i+1));
        }
        return map;
    }
    public static Expr dissoc(Seq args) throws NoSuchMethodException {
        if (1 > args.size()) throw new NoSuchMethodException("dissoc requires at least a map; but received "+args.size()+" arguments");
        Map map = (Map)args.first();
        Map result = new Map(map);
        for (int i = 1; i < args.size(); i++) {
            result.dissoc(args.get(i));
        }
        return result;
    }
    public static Expr dissoc_BANG_(Seq args) throws NoSuchMethodException {
        if (1 > args.size()) throw new NoSuchMethodException("dissoc requires at least a map; but received "+args.size()+" arguments");
        Map map = (Map)args.first();
        for (int i = 1; i < args.size(); i++) {
            map.dissoc(args.get(i));
        }
        return map;
    }
    public static Expr contains_QMARK_(Seq args) throws NoSuchMethodException {
        checkArity(2, args, "contains?");
        Map m = (Map)args.first();
        if (m.contains(args.get(1))) {
            return Literal.T;
        }
        return Literal.F;
    }
    public static Expr keys(Seq args) throws NoSuchMethodException {
        checkArity(1, args, "keys");
        return ((Map)args.first()).keys();
    }
    public static Expr vals(Seq args) throws NoSuchMethodException {
        checkArity(1, args, "vals");
        return ((Map)args.first()).values();
    }
    public static Expr read_string(Seq args) throws NoSuchMethodException {
        checkArity(1, args, "read-string");
        Str s = (Str)args.first();
        return Reader.readString(s.toString());
    }
    public static Expr slurp(Seq args) throws NoSuchMethodException, IOException {
        checkArity(1, args, "slurp");
        Path file = Path.of(args.first().toString());
        String content = Files.readString(file);
        return new Str(content);
    }
    // public static Expr (Seq args) throws NoSuchMethodException {}
}