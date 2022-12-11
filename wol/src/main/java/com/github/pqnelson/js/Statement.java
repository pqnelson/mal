package com.github.pqnelson.js;

/**
 * The base class for Javascript statements.
 *
 * <p>We actually use a subset of the Javascript language using the
 * following grammar (the statement can be prefixed by an optional
 * "<i>Name</i> {@code ':'}" label), with the statements
 * <span style="color: red;">not yet implemented</span> in Java code in red:</p>
 * <blockquote>
 * <dl>
 * <dt><i>Statement</i></dt>
 * <dd><i>EmptyStatement</i></dd>
 * <dd><i>BlockStatement</i></dd>
 * <dd><i>LetStatement</i></dd>
 * <dd><i>IfStatement</i></dd>
 * <dd style="color: red;"><i>SwitchStatement</i></dd>
 * <dd><i>WhileStatement</i></dd>
 * <dd style="color: red;"><i>ForStatement</i></dd>
 * <dd><i>DisruptiveStatement</i></dd>
 * <dd style="color: red;"><i>TryStatement</i></dd>
 * <dd style="color: red;"><i>DoStatement</i></dd>
 * <dd><i>ExpressionStatement</i> {@code ';'}</dd>
 * </dl>
 * <dl>
 * <dt><i>EmptyStatement</i></dt>
 * <dd>{@code ';'}</dd>
 * </dl>
 * <dl>
 * <dt><i>BlockStatement</i></dt>
 * <dd>'{' <i>Statements</i> '}'</dd>
 * </dl>
 * <dl>
 * <dt><i>LetStatement</i></dt>
 * <dd>'let' <i>Bindings</i> {@code ';'}</dd>
 * <dt><i>Bindings</i></dt>
 * <dd><i>Name</i> {@code '='} <i>Expression</i></dd>
 * <dd><i>Name</i> {@code '='} <i>Expression</i> {@code ','} <i>Bindings</i></dd>
 * </dl>
 * <dl>
 * <dt><i>IfStatement</i></dt>
 * <dd> {@code 'if'} {@code '('} <i>Expression</i> {@code ')'} '{' <i>Statements</i> '}'</dd>
 * <dd> {@code 'if'} {@code '('} <i>Expression</i> {@code ')'} '{' <i>Statements</i> '}'
 *      {@code 'else'} '{' <i>Statements</i> '}'</dd>
 * </dl>
 * <dl style="color: red;">
 * <dt><i>SwitchStatement</i></dt>
 * <dd>{@code 'switch'} {@code '('} <i>Expression</i> {@code ')'} '{' <i>CaseClause</i><sup>+</sup> '}'</dd>
 * <dd>{@code 'switch'} {@code '('} <i>Expression</i> {@code ')'} '{' <i>CaseClause</i><sup>+</sup>
 * {@code 'default'} {@code ':'} <i>Statements</i> '}'</dd>
 * <dt><i>CaseClause</i></dt>
 * <dd>{@code 'case'} <i>Expression</i> {@code ':'} <i>Statements</i></dd>
 * </dl>
 * <dl>
 * <dt><i>WhileStatement</i></dt>
 * <dd>{@code 'while'} {@code '('} <i>Expression</i> {@code ')'} <i>BlockStatement</i></dd>
 * </dl>
 * <dl style="color: red;">
 * <dt><i>ForStatement</i></dt>
 * <dd>{@code 'for'} {@code '('} <i>Expression</i><sub><i>opt</i></sub> {@code ';'}
 * <i>Expression</i><sub><i>opt</i></sub> {@code ';'} <i>Expression</i><sub><i>opt</i></sub> {@code ')'}
 * <i>BlockStatement</i></dd>
 * <dd>{@code 'for'} {@code '('} <i>Name</i> {@code 'in'} <i>Expression</i> {@code ')'}
 * <i>BlockStatement</i></dd>
 * <dd>{@code 'for'} {@code '('} <i>Name</i> {@code 'of'} <i>Expression</i> {@code ')'}
 * <i>BlockStatement</i></dd>
 * </dl>
 * <dl style="color: red;">
 * <dt><i>DoStatement</i></dt>
 * <dd>{@code 'do'} <i>BlockStatement</i> {@code 'while'}
 * {@code '('} <i>Expression</i> {@code ')'} {@code ';'}</dd>
 * </dl>
 * <dl style="color: red;">
 * <dt><i>TryStatement</i></dt>
 * <dd>{@code 'try'} <i>BlockStatement</i> {@code 'catch'}
 * {@code '('} <i>Name</i> {@code ')'} <i>BlockStatement</i></dd>
 * </dl>
 * <dl>
 * <dt><i>DisruptiveStatement</i></dt>
 * <dd><i>BreakStatement</i></dd>
 * <dd><i>ReturnStatement</i></dd>
 * <dd style="color: red;"><i>ThrowStatement</i></dd>
 * <dt><i>ReturnStatement</i></dt>
 * <dd>{@code 'return'} <i>Expression</i><sub><i>opt</i></sub> {@code ';'}</dd>
 * <dt style="color: red;"><i>ThrowStatement</i></dt>
 * <dd style="color: red;">{@code 'throw'} <i>Expression</i> {@code ';'}</dd>
 * <dt><i>BreakStatement</i></dt>
 * <dd>{@code 'break'} <i>Name</i><sub><i>opt</i></sub> {@code ';'}</dd>
 * </dl>
 * <dl>
 * <dt><i>ExpressionStatement</i></dt>
 * <dd><i>AssignmentExpressionStatement</i></dd>
 * <dd><i>Name</i> <i>Refinement</i><sub><i>opt</i></sub> <i>Invocation</i> {@code ';'}</dd>
 * <dd style="color: red;">{@code 'delete'} <i>Name</i> <i>Refinement</i> {@code ';'}</dd>
 * <dt><i>AssignmentExpressionStatement</i></dt>
 * <dd><i>Name</i> <i>Refinement</i><sub><i>opt</i></sub> {@code '='} <i>Expression</i> {@code ';'}</dd>
 * <dd style="color: red;"><i>Name</i> <i>Refinement</i><sub><i>opt</i></sub> {@code '+='} <i>Expression</i> {@code ';'}</dd>
 * <dd style="color: red;"><i>Name</i> <i>Refinement</i><sub><i>opt</i></sub> {@code '-='} <i>Expression</i> {@code ';'}</dd>
 * </dl>
 * </blockquote>
 *
 * <p>It may be worth while to consider including {@code 'import'} and
 * {@code 'export'} statements modifiers for ES6, as well as {@code 'class'}
 * definitions.</p>
 *
 * @see <a href="https://tc39.es/ecma262/multipage/grammar-summary.html#sec-statements">Formal Grammar of Javascript Statements</a>
 */
public abstract class Statement {
    public abstract <T> T accept(final Visitor<T> visitor);

    public String toJavascriptCode() {
        return this.toString();
    }
    /*
    static final class EmptyStatement extends Statement {
        private EmptyStatement() { }
        @Override
        public abstract <T> T accept(final StatementVisitor<T> visitor);
        @Override
        public String toString() { return ";"; }
        @Override
        public String toJavascriptCode() { return ";"; }
    }
    *
     * The empty statement.
     *
    public static final Statement EMPTY = new EmptyStatement();
    */
}