package com.github.pqnelson.js;


/**
 * Visitor pattern to both expressions and statements.
 */
public interface Visitor<T> {
    T visitBlock(BlockStatement s);
    T visitIf(IfStatement s);
    T visitDeclaration(VarDeclarationStatement s);
    T visitWhile(WhileStatement s);
    T visitReturn(ReturnStatement s);
    T visitBreak(BreakStatement s);
    T visitAssignment(AssignmentStatement s);
    T visitFunctionCallStatement(FunctionCallStatement funcall);

    T visitName(Name name);
    T visitRefinement(RefinementExpr expr);
    T visitFunctionCallExpr(FunctionCallExpr funcall);
    T visitString(JsString string);
    T visitBool(JsBool string);
    T visitNumber(JsNumber number);
    T visitBinaryOpExpr(BinaryOpExpr expr);
    T visitPrefixOpExpr(PrefixOpExpr expr);
    T visitNull(Null e);
    T visitUndefined(Undefined e);
    T visitLambdaExpr(LambdaExpr e);
    T visitConditionalExpr(ConditionalExpr e);
}