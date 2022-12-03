package com.github.pqnelson;

import java.util.function.Function;

import com.github.pqnelson.expr.Expr;
import com.github.pqnelson.expr.Fun;
import com.github.pqnelson.expr.IObj;
import com.github.pqnelson.expr.Keyword;
import com.github.pqnelson.expr.Literal;
import com.github.pqnelson.expr.Map;
import com.github.pqnelson.expr.Seq;

public class RT {
    public static Fun withMeta = new Fun((Seq args) -> {
                IObj obj = (IObj)args.get(1);
                Map new_meta = (Map)args.first();
                return (Expr)((Object)obj.withMeta(new_meta));
    });
    static final Keyword TAG_KEY = new Keyword("tag");
    // This is the macro delegated by caret reader macros
    /**
     * Read a caret to a metadata binding instruction.
     *
     * <p>This uses a "secret" macro to bind a keyword as metadata.
     * Specifically, {@code (def ^:key term val)} means, roughly,
     * {@code (def (with-meta term {:key true}) val)}.</p>
     *
     * <p>If we prefix a symbol or a string with a caret, then we set the
     * {@code tag} metadata to the given symbol. For example,
     * {@code (def ^my-tag term val)} means roughly
     * {@code (def (with-meta term {:tag my-tag}) val)}.</p>
     *
     * @see <a href="https://github.com/clojure/clojure/blob/clojure-1.10.1/src/jvm/clojure/lang/LispReader.java#L943-L986">Clojure's <code>LispReader</code></a>
     */
    public static final Fun metaReader =
        new Fun((Seq args) -> {
                    IObj obj = (IObj)args.get(1);
                    Map new_meta = obj.meta();
                    Expr meta = args.first();
                    if ((meta.isLiteral() && ((Literal)meta).isString())
                        || meta.isSymbol()) {
                        new_meta.assoc(TAG_KEY, meta);
                    } else if (meta.isKeyword()) {
                        new_meta.assoc(meta, Literal.T);
                    } else if (meta.isMap()) {
                        new_meta = new_meta.merge((Map)meta);
                    } else {
                        throw new IllegalArgumentException("Metadata must be Symbol, Keyword, String or Map");
                    }
                    return (Expr)((Object)obj.withMeta(new_meta));
        });
    static {
        metaReader.setMacro();
    }
}