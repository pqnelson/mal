package com.github.pqnelson.js;

/**
 * The base class for Javascript expressions.
 *
 * <p>We use the following restricted grammar (with those expressions
 * <span style="color: red;">not yet implemented in red</span>):</p>
 * <blockquote>
 * <dl>
 * <dt><i>Expression</i></dt>
 * <dd><i>Literal</i></dd>
 * <dd><i>Name</i></dd>
 * <dd style="color: red;"><i>Expression</i> <i>SuffixOperator</i></dd>
 * <dd><i>PrefixOperator</i> <i>Expression</i></dd>
 * <dd>{@code '('} <i>Expression</i> {@code ')'}</dd>
 * <dd><i>Expression</i> <i>InfixOperator</i> <i>Expression</i></dd>
 * <dd><i>Expression</i> {@code '?'} <i>Expression</i> {@code ':'} <i>Expression</i></dd>
 * <dd><i>Expression</i> <i>Invocation</i></dd>
 * <dd><i>Expression</i> <i>Refinement</i></dd>
 * <dd style="color: red;">{@code 'new'} <i>Expression</i> <i>Invocation</i></dd>
 * <dd style="color: red;">{@code 'delete'} <i>Expression</i> <i>Refinement</i></dd>
 * <dd><i>LambdaExpression</i></dd>
 * </dl>
 * </blockquote>
 */
public abstract class JsExpr {
    public abstract <T> T accept(final ExprVisitor<T> visitor);
}
