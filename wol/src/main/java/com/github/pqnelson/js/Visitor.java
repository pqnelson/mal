package com.github.pqnelson.js;


/**
 * Visitor pattern to both expressions and statements.
 */
public interface Visitor<T> extends ExprVisitor<T> {
    T visitBlock(BlockStatement s);
    T visitIf(IfStatement s);
    T visitDeclaration(VarDeclarationStatement s);
    T visitWhile(WhileStatement s);
    T visitReturn(ReturnStatement s);
    T visitBreak(BreakStatement s);
    T visitAssignment(AssignmentStatement s);
    T visitFunctionCallStatement(FunctionCallStatement funcall);

    /*
    default T visitName(Name name) {
        name.accept((ExprVisitor<T>) this);
    }
    default T visitRefinement(RefinementExpr expr) {
        expr.accept((ExprVisitor<T>) this);
    }
    T visitFunctionCallExpr(FunctionCallExpr funcall);
    T visitString(JsString string);
    T visitBool(JsBool string);
    T visitNumber(JsNumber number);
    default T visitBinaryOpExpr(BinaryOpExpr expr) {
        expr.accept((ExprVisitor<T>) this);
    }
    default T visitPrefixOpExpr(PrefixOpExpr expr) {
        expr.accept((ExprVisitor<T>) this);
    }
    default T visitNull(Null e) {
        e.accept((ExprVisitor<T>) this);
    }
    default T visitUndefined(Undefined e) {
        e.accept((ExprVisitor<T>) this);
    }
    default T visitLambdaExpr(LambdaExpr e) {
        e.accept((ExprVisitor<T>) this);
    }
    default T visitConditionalExpr(ConditionalExpr e) {
        e.accept((ExprVisitor<T>) this);
    }
    */
}
