package com.github.pqnelson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.ICountable;
import com.github.pqnelson.expr.IFn;
import com.github.pqnelson.expr.Int;
import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.LispException;
import com.github.pqnelson.expr.LispIOException;
import com.github.pqnelson.expr.LispIllegalArgumentException;
import com.github.pqnelson.expr.LispNoSuchMethodException;
import com.github.pqnelson.expr.Map;
import com.github.pqnelson.expr.Seq;
import com.github.pqnelson.expr.Str;
import com.github.pqnelson.expr.Symbol;
import com.github.pqnelson.expr.Vector;
import com.github.pqnelson.annotations.VisibleForTesting;

public final class Core {
    @VisibleForTesting
    static Function<Boolean, Expr> boxBool
        = (Boolean b) -> (b.equals(Boolean.TRUE) ? Literal.T : Literal.F);

    private Core() { }

    private static void checkArity(final int n,
                                   final Seq args,
                                   final String functionName)
            throws LispException {
        if (n != args.size()) {
            throw new LispException("Arity of function "
                                    + functionName
                                    + " is "
                                    + n
                                    + " but passed "
                                    + args.size()
                                    + " arguments");
        }
    }
    @VisibleForTesting
    static String aritiesToStr(final Set<Integer> arities) {
        StringBuffer buf = new StringBuffer("{");
        for (int i : arities) {
            buf.append(Integer.toString(i));
            buf.append(", ");
        }
        if (arities.size() > 0) {
            for (int i = 0; i < 2; i++) {
                buf.deleteCharAt(buf.length() - 1);
            }
        }
        buf.append("}");
        return buf.toString();
    }
    private static void checkArities(final Set<Integer> arities,
                                     final Seq args,
                                     final String functionName)
            throws LispException {
        if (!arities.contains(args.size())) {
            throw new LispException("Arities of function "
                                    + functionName
                                    + " are "
                                    + aritiesToStr(arities)
                                    + " but passed "
                                    + args.size()
                                    + " arguments");
        }
    }

    private static IFn predicateFactory(final String predicateName,
                                        final Predicate<Expr> pred) {
        return (Seq args) -> {
            checkArity(1, args, predicateName + "_QMARK_");
            if (pred.test(args.first())) {
                return Literal.T;
            } else {
                return Literal.F;
            }
        };
    }

    /**
     * Now we have publicly exported functions.
     *
     * @param args A list of numbers to sum together.
     * @return The sum, defaulting to zero for an empty argument list.
     */
    public static Expr add(final Seq args) throws LispException {
        com.github.pqnelson.expr.Number sum = new Int(0L);
        for (Expr e : args) {
            sum = sum.add((com.github.pqnelson.expr.Number) e);
        }
        return sum;
    }

    public static Expr subtract(final Seq args) throws LispException {
        if (0 == args.size()) {
            return Literal.ZERO;
        }
        com.github.pqnelson.expr.Number sum
            = (com.github.pqnelson.expr.Number) args.first();
        for (Expr e : args.slice(1)) {
            sum = sum.subtract((com.github.pqnelson.expr.Number) e);
        }
        return sum;
    }

    public static Expr multiply(final Seq args) throws LispException {
        if (0 == args.size()) {
            return new Int(1L);
        }
        com.github.pqnelson.expr.Number product = new Int(1L);
        for (Expr e : args) {
            product = product.multiply((com.github.pqnelson.expr.Number) e);
        }
        return product;
    }
    public static Expr divide(final Seq args) throws LispException {
        if (0 == args.size()) {
            return Literal.ONE;
        }
        if (1 == args.size()) {
            final com.github.pqnelson.expr.Number denominator
                = (com.github.pqnelson.expr.Number) (args.first());
            return (new Int(1L)).divide(denominator);
        }
        com.github.pqnelson.expr.Number quotient
            = (com.github.pqnelson.expr.Number) args.first();
        for (Expr e : args.slice(1)) {
            quotient = quotient.divide((com.github.pqnelson.expr.Number) e);
        }
        return quotient;
    }

    public static Expr count(final Seq args) throws LispException {
        checkArity(1, args, "count");
        final ICountable coll = (ICountable) (args.first());
        return new Int(coll.size());
    }

    public static IFn list_QMARK_ = predicateFactory("list", Expr::isList);

    public static Expr empty_QMARK_(final Seq args) throws LispException {
        checkArity(1, args, "empty?");
        if ((args.first().isList() && ((Seq) (args.first())).isEmpty())
            || (args.first().isVector() && ((Vector) (args.first())).isEmpty())
            || (args.first().isMap() && ((Map) (args.first())).isEmpty())) {
            return Literal.T;
        }
        return Literal.F;
    }

    private static Expr _seqArg(final Expr arg) throws LispException {
        if (arg.isList()) {
            return ((Seq) arg).seq();
        } else if (arg.isNil()) {
            return arg;
        } else if (arg.isVector()) {
            return ((Vector) arg).seq();
        } else if (arg.isMap()) {
            return ((Map) arg).seq();
        } else if (arg.isString()) {
            return ((Str) arg).seq();
        } else {
            throw new LispException("seq called on unknown type " + arg.type());
        }
    }

    /**
     * Produce a list from the given argument.
     *
     * <p>Empty lists, empty maps, and empty vectors all yield {@code nil}.</p>
     *
     * @param args - The parameters as a list.
     * @return Either a {@code Seq} instance or nil.
     * @throws LispException if given anything other than a
     * {@code Seq}, {@code Literal.NIL}, {@code Vector}, {@code Map}, or
     * {@code String} object.
     */
    public static Expr seq(final Seq args) throws LispException {
        checkArity(1, args, "seq");
        return _seqArg(args.first());
    }

    public static Expr first(final Seq args) throws LispException {
        checkArity(1, args, "first");
        final Expr arg = _seqArg(args.first());
        if (arg.isNil()) {
            return arg;
        }
        return ((Seq) arg).first();
    }

    public static Expr rest(final Seq args) throws LispException {
        checkArity(1, args, "rest");
        final Expr arg = _seqArg(args.first());
        if (arg.isNil()) {
            return arg;
        }
        return ((Seq) arg).slice(1);
    }

    public static Expr nth(final Seq args) throws LispException {
        checkArities(Set.of(2, 3), args, "nth");
        if (!args.get(1).isInt()) {
            throw new LispException("nth called with non-int index, received "
                                    + args.get(1).type());
        }
        int i = ((Int) args.get(1)).value().intValue();
        Expr arg = args.first();
        Expr defaultValue = args.get(2, Literal.NIL);
        if (arg.isNil()) {
            return defaultValue;
        } else if (arg.isMap() || arg.isString()) {
            arg = _seqArg(arg);
        } else if (arg.isVector()) {
            try {
                return ((Vector) arg).get(((Int) args.get(1)), defaultValue);
            } catch (NoSuchMethodException e) {
                throw new LispNoSuchMethodException(e);
            }
        }
        return ((Seq) arg).get(i, defaultValue);
    }

    public static Expr concat(final Seq args) throws LispException {
        Expr arg = args.first();
        if (args.isEmpty()) {
            return Literal.NIL;
        }
        Expr seqArg = _seqArg(arg);
        if (1 == args.size()) {
            return seqArg;
        } else if (seqArg.isNil()) {
            seqArg = new Seq();
        }
        Seq result = (Seq) seqArg;
        for (Expr e : args.slice(1)) {
            result = result.concat(_seqArg(e));
        }
        return result;
    }

    public static Expr cons(final Seq args) throws LispException {
        checkArity(2, args, "cons");
        Expr e = _seqArg(args.get(1));
        if (e.isNil()) {
            e = new Seq();
        }
        Seq coll = (Seq) e;
        return coll.cons(args.first());
    }

    public static Expr equality(final Seq args) throws LispException {
        switch (args.size()) {
        case 0: return Literal.T;
        case 1: return Literal.T;
        case 2: if (args.get(0).equals(args.get(1))) {
                return Literal.T;
            } else {
                return Literal.F;
            }
        default:
            for (int i = 0; i < args.size() - 1; i++) {
                if (!args.get(i).equals(args.get(i + 1))) {
                    return Literal.F;
                }
            }
            return Literal.T;
        }
    }
    public static Expr LT(final Seq args) throws LispException {
        switch (args.size()) {
        case 0: return Literal.T;
        case 1: return Literal.T;
        case 2:
            if (!args.get(0).isNumber() || !args.get(1).isNumber()) {
                throw new LispException("< works on numbers");
            }
            return (((com.github.pqnelson.expr.Number)
                     (args.get(0))).compareTo((com.github.pqnelson.expr.Number)(args.get(1))) < 0 ? Literal.T : Literal.F);
        default:
            for (int i = 0; i < args.size() - 1; i++) {
                if (!args.get(i).isNumber() || !args.get(i + 1).isNumber()) {
                    throw new LispException("< works on numbers");
                }
                if (!(((com.github.pqnelson.expr.Number)
                       (args.get(i))).compareTo((com.github.pqnelson.expr.Number)
                                                (args.get(i + 1)))
                      < 0)) {
                    return Literal.F;
                }
            }
            return Literal.T;
        }
    }
    public static Expr LEQ(final Seq args) throws LispException {
        switch (args.size()) {
        case 0: return Literal.T;
        case 1: return Literal.T;
        case 2:
            if (!args.get(0).isNumber() || !args.get(1).isNumber()) {
                throw new LispException("<= works on numbers");
            }
            return (((com.github.pqnelson.expr.Number) (args.get(0))).compareTo((com.github.pqnelson.expr.Number)(args.get(1))) <= 0 ? Literal.T : Literal.F);
        default:
            for (int i = 0; i < args.size() - 1; i++) {
                if (!args.get(i).isNumber() || !args.get(i + 1).isNumber()) {
                    throw new LispException("<= works on numbers");
                }
                if (!(((com.github.pqnelson.expr.Number) (args.get(i))).compareTo((com.github.pqnelson.expr.Number)(args.get(i + 1))) <= 0)) {
                    return Literal.F;
                }
            }
            return Literal.T;
        }
    }
    public static Expr GT(final Seq args) throws LispException {
        switch (args.size()) {
        case 0: return Literal.T;
        case 1: return Literal.T;
        case 2:
            if (!args.get(0).isNumber() || !args.get(1).isNumber()) {
                throw new LispException("> works on numbers");
            }
            return (((com.github.pqnelson.expr.Number) (args.get(0))).compareTo((com.github.pqnelson.expr.Number)(args.get(1))) > 0 ? Literal.T : Literal.F);
        default:
            for (int i = 0; i < args.size() - 1; i++) {
                if (!args.get(i).isNumber() || !args.get(i + 1).isNumber()) {
                    throw new LispException("> works on numbers");
                }
                if (!(((com.github.pqnelson.expr.Number) (args.get(i))).compareTo((com.github.pqnelson.expr.Number)(args.get(i + 1))) > 0)) {
                    return Literal.F;
                }
            }
            return Literal.T;
        }
    }
    public static Expr GEQ(final Seq args) throws LispException {
        switch (args.size()) {
        case 0: return Literal.T;
        case 1: return Literal.T;
        case 2:
            if (!args.get(0).isNumber() || !args.get(1).isNumber()) {
                throw new LispException(">= works on numbers");
            }
            return (((com.github.pqnelson.expr.Number)(args.get(0))).compareTo((com.github.pqnelson.expr.Number)(args.get(1))) >= 0 ? Literal.T : Literal.F);
        default:
            for (int i = 0; i < args.size() - 1; i++) {
                if (!args.get(i).isNumber() || !args.get(i + 1).isNumber()) {
                    throw new LispException(">= works on numbers");
                }
                if (!(((com.github.pqnelson.expr.Number)(args.get(i))).compareTo((com.github.pqnelson.expr.Number)(args.get(i + 1))) >= 0)) {
                    return Literal.F;
                }
            }
            return Literal.T;
        }
    }
    public static IFn nil_QMARK_ = predicateFactory("nil", Expr::isNil);
    public static IFn true_QMARK_
        = predicateFactory("true", Literal::exprIsTrue);
    public static IFn false_QMARK_
        = predicateFactory("false", Literal::exprIsFalse);
    public static IFn symbol_QMARK_
        = predicateFactory("symbol", Expr::isSymbol);

    public static Expr symbol(final Seq args) throws LispException {
        checkArity(1, args, "symbol");
        final Expr e = args.first();
        if (e.isSymbol()) {
            return e;
        } else if (e.isKeyword()) {
            return ((Keyword) e).symbol();
        } else if (e.isString()) {
            return new Symbol(((Str) e).value());
        } else {
            throw new LispIllegalArgumentException("no conversion of "
                                                   + e.type()
                                                   + " to symbol");
        }
    }

    public static IFn keyword_QMARK_ = predicateFactory("keyword", Expr::isKeyword);

    public static Expr keyword(final Seq args) throws LispException {
        checkArity(1, args, "keyword");
        final Expr e = args.first();
        if (e.isSymbol()) {
            return new Keyword(((Symbol) e).name());
        } else if (e.isKeyword()) {
            return e;
        } else if (e.isString()) {
            return new Keyword(((Str) e).value());
        } else {
            throw new LispIllegalArgumentException("no conversion of "
                                                   + e.type()
                                                   + " to keyword");
        }
    }

    public static Expr vector(final Seq args) throws LispException {
        return args.vec();
    }

    public static IFn vector_QMARK_
        = predicateFactory("vector", Expr::isVector);

    public static IFn map_QMARK_ = predicateFactory("map", Expr::isMap);

    public static IFn string_QMARK_
        = predicateFactory("string", Expr::isString);

    public static IFn fn_QMARK_ = predicateFactory("fn", Expr::isFunction);

    public static IFn macro_QMARK_ = predicateFactory("macro", Expr::isMacro);

    public static Expr println(final Seq args) throws LispException {
        StringBuffer buf = new StringBuffer();
        final Printer p = new Printer();
        for (final Expr e : args) {
            buf.append(e.accept(p));
            buf.append(" ");
        }
        buf.deleteCharAt(buf.length() - 1);
        System.out.println(buf.toString());
        return Literal.NIL;
    }
    public static Expr pr_str(final Seq args) throws LispException {
        StringBuffer buf = new StringBuffer();
        final PPrinter p = new PPrinter();
        for (final Expr e : args) {
            buf.append(e.accept(p));
            buf.append(" ");
        }
        buf.deleteCharAt(buf.length()-  1);
        return new Str(buf.toString());
    }
    public static Expr prn(final Seq args) throws LispException {
        StringBuffer buf = new StringBuffer();
        final PPrinter p = new PPrinter();
        for (final Expr e : args) {
            buf.append(e.accept(p));
            buf.append(" ");
        }
        buf.deleteCharAt(buf.length() - 1);
        System.out.println(buf.toString());
        return Literal.NIL;
    }
    public static Expr str(final Seq args) throws LispException {
        StringBuffer buf = new StringBuffer();
        final Printer printer = new Printer();
        for (final Expr e : args) {
            buf.append(e.accept(printer));
        }
        return new Str(buf.toString());
    }
    public static Expr hash_map(final Seq args) throws LispException {
        if (0 != args.size() % 2) {
            throw new LispException("hash-map requires"
                                    + " an even number of bindings");
        }
        Map map = new Map();
        for (int i = 0; i < args.size(); i += 2) {
            map.assoc(args.get(i), args.get(i+1));
        }
        return map;
    }
    public static Expr get(final Seq args) throws LispException {
        checkArities(Set.of(2,3), args, "get");
        final Expr coll = args.first();
        final Expr i = args.get(1);
        final Expr defaultValue = args.get(2, Literal.NIL);
        if (coll.isMap()) {
            return ((Map) coll).get(i, defaultValue);
        } else if (coll.isVector()) {
            try {
                return ((Vector) coll).get(i, defaultValue);
            } catch (NoSuchMethodException e) {
                throw new LispNoSuchMethodException(e);
            }
        } else if (coll.isNil()) {
            return defaultValue;
        }
        throw new LispException("get applied to unexpected type '"
                                + coll.type()
                                + "'");
    }
    public static Expr assoc(final Seq args) throws LispException {
        if (args.size() < 3) {
            throw new LispException("assoc requires at least a map, a key, "
                                    + "a value; but received "
                                    + args.size()
                                    + " arguments");
        }
        final int bindingsCount = args.size() - 1;
        if (0 != bindingsCount % 2) {
            throw new LispException("assoc requires"
                                    + " an even number of bindings");
        }
        Map map = new Map((Map)args.first());
        for (int i = 1; i < args.size(); i += 2) {
            map.assoc(args.get(i), args.get(i + 1));
        }
        return map;
    }
    public static Expr assoc_BANG_(final Seq args) throws LispException {
        if (3 > args.size()) {
            throw new LispException("assoc requires at least a map, a key, "
                                    + "a value; but received "
                                    + args.size()
                                    + " arguments");
        }
        if (1 != args.size() % 2) {
            throw new LispException("assoc requires"
                                    + " an even number of bindings");
        }
        Map map = (Map)args.first();
        for (int i = 1; i < args.size(); i += 2) {
            map.assoc(args.get(i), args.get(i + 1));
        }
        return map;
    }

    public static Expr dissoc(final Seq args) throws LispException {
        if (1 > args.size()) {
            throw new LispException("dissoc requires at least a map;"
                                    + " but received "
                                    + args.size()
                                    + " arguments");
        }
        Map result = new Map((Map) args.first());
        for (int i = 1; i < args.size(); i++) {
            result.dissoc(args.get(i));
        }
        return result;
    }
    public static Expr dissoc_BANG_(final Seq args) throws LispException {
        if (1 > args.size()) {
            throw new LispException("dissoc requires at least a map;"
                                    + " but received "
                                    + args.size()
                                    + " arguments");
        }
        Map map = (Map) args.first();
        for (int i = 1; i < args.size(); i++) {
            map.dissoc(args.get(i));
        }
        return map;
    }
    public static Expr contains_QMARK_(final Seq args) throws LispException {
        checkArity(2, args, "contains?");
        Map m = (Map) args.first();
        if (m.contains(args.get(1))) {
            return Literal.T;
        }
        return Literal.F;
    }
    public static Expr keys(final Seq args) throws LispException {
        checkArity(1, args, "keys");
        return ((Map) args.first()).keys();
    }
    public static Expr vals(final Seq args) throws LispException {
        checkArity(1, args, "vals");
        return ((Map) args.first()).values();
    }
    public static Expr read_string(final Seq args) throws LispException {
        checkArity(1, args, "read-string");
        final Str s = (Str) args.first();
        ReadTable reader = new ReadTable(s.toString());
        return reader.read();
    }
    public static Expr slurp(final Seq args) throws LispException {
        try {
            checkArity(1, args, "slurp");
            final Path file = Path.of(args.first().toString());
            final String content = Files.readString(file);
            return new Str(content);
        } catch (IOException e) {
            throw new LispIOException(e);
        }
    }
    public static Expr list(final Seq args) throws LispException {
        return args;
    }

    public static Expr _throw(final Seq args) throws LispException {
        final Str s = (Str) Core.str(args);
        throw new LispException(s.value());
    }
    // public static Expr (final Seq args) throws LispException {}
}
