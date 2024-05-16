// Generated from C:/Users/corey/FAFProjects/fx2j/fx2j-parser/src/main/antlr4/io/github/sheikah45/fx2j/parser/expression/BindExpression.g4 by ANTLR 4.13.1

package io.github.sheikah45.fx2j.parser.antlr;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link BindExpressionParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 *            operations with no return type.
 */
public interface BindExpressionVisitor<T> extends ParseTreeVisitor<T> {
    /**
     * Visit a parse tree produced by the {@code decimalLiteral}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDecimalLiteral(BindExpressionParser.DecimalLiteralContext ctx);

    /**
     * Visit a parse tree produced by the {@code lessThanEqual}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitLessThanEqual(BindExpressionParser.LessThanEqualContext ctx);

    /**
     * Visit a parse tree produced by the {@code collectionAccess}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCollectionAccess(BindExpressionParser.CollectionAccessContext ctx);

    /**
     * Visit a parse tree produced by the {@code fractionalLiteral}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitFractionalLiteral(BindExpressionParser.FractionalLiteralContext ctx);

    /**
     * Visit a parse tree produced by the {@code trueLiteral}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTrueLiteral(BindExpressionParser.TrueLiteralContext ctx);

    /**
     * Visit a parse tree produced by the {@code enclosed}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitEnclosed(BindExpressionParser.EnclosedContext ctx);

    /**
     * Visit a parse tree produced by the {@code falseLiteral}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitFalseLiteral(BindExpressionParser.FalseLiteralContext ctx);

    /**
     * Visit a parse tree produced by the {@code and}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAnd(BindExpressionParser.AndContext ctx);

    /**
     * Visit a parse tree produced by the {@code lessThan}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitLessThan(BindExpressionParser.LessThanContext ctx);

    /**
     * Visit a parse tree produced by the {@code divide}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDivide(BindExpressionParser.DivideContext ctx);

    /**
     * Visit a parse tree produced by the {@code multiply}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitMultiply(BindExpressionParser.MultiplyContext ctx);

    /**
     * Visit a parse tree produced by the {@code equality}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitEquality(BindExpressionParser.EqualityContext ctx);

    /**
     * Visit a parse tree produced by the {@code greaterThan}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitGreaterThan(BindExpressionParser.GreaterThanContext ctx);

    /**
     * Visit a parse tree produced by the {@code add}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAdd(BindExpressionParser.AddContext ctx);

    /**
     * Visit a parse tree produced by the {@code greaterThanEqual}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitGreaterThanEqual(BindExpressionParser.GreaterThanEqualContext ctx);

    /**
     * Visit a parse tree produced by the {@code nullLiteral}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitNullLiteral(BindExpressionParser.NullLiteralContext ctx);

    /**
     * Visit a parse tree produced by the {@code or}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitOr(BindExpressionParser.OrContext ctx);

    /**
     * Visit a parse tree produced by the {@code invert}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitInvert(BindExpressionParser.InvertContext ctx);

    /**
     * Visit a parse tree produced by the {@code subtract}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSubtract(BindExpressionParser.SubtractContext ctx);

    /**
     * Visit a parse tree produced by the {@code inequality}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitInequality(BindExpressionParser.InequalityContext ctx);

    /**
     * Visit a parse tree produced by the {@code stringLiteral}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitStringLiteral(BindExpressionParser.StringLiteralContext ctx);

    /**
     * Visit a parse tree produced by the {@code negate}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitNegate(BindExpressionParser.NegateContext ctx);

    /**
     * Visit a parse tree produced by the {@code variable}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitVariable(BindExpressionParser.VariableContext ctx);

    /**
     * Visit a parse tree produced by the {@code propertyRead}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitPropertyRead(BindExpressionParser.PropertyReadContext ctx);

    /**
     * Visit a parse tree produced by the {@code remainder}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitRemainder(BindExpressionParser.RemainderContext ctx);

    /**
     * Visit a parse tree produced by the {@code methodCall}
     * labeled alternative in {@link BindExpressionParser#expression()}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitMethodCall(BindExpressionParser.MethodCallContext ctx);
}