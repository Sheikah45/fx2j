// Generated from C:/Users/corey/FAFProjects/fx2j/fx2j-parser/src/main/antlr4/io/github/sheikah45/fx2j/parser/expression/BindExpression.g4 by ANTLR 4.13.1

package io.github.sheikah45.fx2j.parser.antlr;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link BindExpressionParser}.
 */
public interface BindExpressionListener extends ParseTreeListener {
    /**
     * Enter a parse tree produced by the {@code decimalLiteral}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterDecimalLiteral(BindExpressionParser.DecimalLiteralContext ctx);

    /**
     * Exit a parse tree produced by the {@code decimalLiteral}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitDecimalLiteral(BindExpressionParser.DecimalLiteralContext ctx);

    /**
     * Enter a parse tree produced by the {@code lessThanEqual}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterLessThanEqual(BindExpressionParser.LessThanEqualContext ctx);

    /**
     * Exit a parse tree produced by the {@code lessThanEqual}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitLessThanEqual(BindExpressionParser.LessThanEqualContext ctx);

    /**
     * Enter a parse tree produced by the {@code collectionAccess}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterCollectionAccess(BindExpressionParser.CollectionAccessContext ctx);

    /**
     * Exit a parse tree produced by the {@code collectionAccess}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitCollectionAccess(BindExpressionParser.CollectionAccessContext ctx);

    /**
     * Enter a parse tree produced by the {@code fractionalLiteral}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterFractionalLiteral(BindExpressionParser.FractionalLiteralContext ctx);

    /**
     * Exit a parse tree produced by the {@code fractionalLiteral}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitFractionalLiteral(BindExpressionParser.FractionalLiteralContext ctx);

    /**
     * Enter a parse tree produced by the {@code trueLiteral}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterTrueLiteral(BindExpressionParser.TrueLiteralContext ctx);

    /**
     * Exit a parse tree produced by the {@code trueLiteral}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitTrueLiteral(BindExpressionParser.TrueLiteralContext ctx);

    /**
     * Enter a parse tree produced by the {@code enclosed}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterEnclosed(BindExpressionParser.EnclosedContext ctx);

    /**
     * Exit a parse tree produced by the {@code enclosed}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitEnclosed(BindExpressionParser.EnclosedContext ctx);

    /**
     * Enter a parse tree produced by the {@code falseLiteral}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterFalseLiteral(BindExpressionParser.FalseLiteralContext ctx);

    /**
     * Exit a parse tree produced by the {@code falseLiteral}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitFalseLiteral(BindExpressionParser.FalseLiteralContext ctx);

    /**
     * Enter a parse tree produced by the {@code and}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterAnd(BindExpressionParser.AndContext ctx);

    /**
     * Exit a parse tree produced by the {@code and}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitAnd(BindExpressionParser.AndContext ctx);

    /**
     * Enter a parse tree produced by the {@code lessThan}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterLessThan(BindExpressionParser.LessThanContext ctx);

    /**
     * Exit a parse tree produced by the {@code lessThan}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitLessThan(BindExpressionParser.LessThanContext ctx);

    /**
     * Enter a parse tree produced by the {@code divide}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterDivide(BindExpressionParser.DivideContext ctx);

    /**
     * Exit a parse tree produced by the {@code divide}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitDivide(BindExpressionParser.DivideContext ctx);

    /**
     * Enter a parse tree produced by the {@code multiply}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterMultiply(BindExpressionParser.MultiplyContext ctx);

    /**
     * Exit a parse tree produced by the {@code multiply}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitMultiply(BindExpressionParser.MultiplyContext ctx);

    /**
     * Enter a parse tree produced by the {@code equality}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterEquality(BindExpressionParser.EqualityContext ctx);

    /**
     * Exit a parse tree produced by the {@code equality}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitEquality(BindExpressionParser.EqualityContext ctx);

    /**
     * Enter a parse tree produced by the {@code greaterThan}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterGreaterThan(BindExpressionParser.GreaterThanContext ctx);

    /**
     * Exit a parse tree produced by the {@code greaterThan}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitGreaterThan(BindExpressionParser.GreaterThanContext ctx);

    /**
     * Enter a parse tree produced by the {@code add}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterAdd(BindExpressionParser.AddContext ctx);

    /**
     * Exit a parse tree produced by the {@code add}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitAdd(BindExpressionParser.AddContext ctx);

    /**
     * Enter a parse tree produced by the {@code greaterThanEqual}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterGreaterThanEqual(BindExpressionParser.GreaterThanEqualContext ctx);

    /**
     * Exit a parse tree produced by the {@code greaterThanEqual}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitGreaterThanEqual(BindExpressionParser.GreaterThanEqualContext ctx);

    /**
     * Enter a parse tree produced by the {@code nullLiteral}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterNullLiteral(BindExpressionParser.NullLiteralContext ctx);

    /**
     * Exit a parse tree produced by the {@code nullLiteral}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitNullLiteral(BindExpressionParser.NullLiteralContext ctx);

    /**
     * Enter a parse tree produced by the {@code or}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterOr(BindExpressionParser.OrContext ctx);

    /**
     * Exit a parse tree produced by the {@code or}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitOr(BindExpressionParser.OrContext ctx);

    /**
     * Enter a parse tree produced by the {@code invert}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterInvert(BindExpressionParser.InvertContext ctx);

    /**
     * Exit a parse tree produced by the {@code invert}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitInvert(BindExpressionParser.InvertContext ctx);

    /**
     * Enter a parse tree produced by the {@code subtract}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterSubtract(BindExpressionParser.SubtractContext ctx);

    /**
     * Exit a parse tree produced by the {@code subtract}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitSubtract(BindExpressionParser.SubtractContext ctx);

    /**
     * Enter a parse tree produced by the {@code inequality}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterInequality(BindExpressionParser.InequalityContext ctx);

    /**
     * Exit a parse tree produced by the {@code inequality}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitInequality(BindExpressionParser.InequalityContext ctx);

    /**
     * Enter a parse tree produced by the {@code stringLiteral}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterStringLiteral(BindExpressionParser.StringLiteralContext ctx);

    /**
     * Exit a parse tree produced by the {@code stringLiteral}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitStringLiteral(BindExpressionParser.StringLiteralContext ctx);

    /**
     * Enter a parse tree produced by the {@code negate}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterNegate(BindExpressionParser.NegateContext ctx);

    /**
     * Exit a parse tree produced by the {@code negate}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitNegate(BindExpressionParser.NegateContext ctx);

    /**
     * Enter a parse tree produced by the {@code variable}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterVariable(BindExpressionParser.VariableContext ctx);

    /**
     * Exit a parse tree produced by the {@code variable}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitVariable(BindExpressionParser.VariableContext ctx);

    /**
     * Enter a parse tree produced by the {@code propertyRead}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterPropertyRead(BindExpressionParser.PropertyReadContext ctx);

    /**
     * Exit a parse tree produced by the {@code propertyRead}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitPropertyRead(BindExpressionParser.PropertyReadContext ctx);

    /**
     * Enter a parse tree produced by the {@code remainder}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterRemainder(BindExpressionParser.RemainderContext ctx);

    /**
     * Exit a parse tree produced by the {@code remainder}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitRemainder(BindExpressionParser.RemainderContext ctx);

    /**
     * Enter a parse tree produced by the {@code methodCall}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void enterMethodCall(BindExpressionParser.MethodCallContext ctx);

    /**
     * Exit a parse tree produced by the {@code methodCall}
     * labeled alternative in {@link BindExpressionParser#expression}.
     *
     * @param ctx the parse tree
     */
    void exitMethodCall(BindExpressionParser.MethodCallContext ctx);
}