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
     * @see {@link https://github.com/clojure/clojure/blob/clojure-1.10.1/src/jvm/clojure/lang/LispReader.java#L943-L986}
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