package io.github.sheikah45.fx2j.parser.antlr;

import io.github.sheikah45.fx2j.parser.property.Expression;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

public class BindExpressionVisitorImpl extends AbstractParseTreeVisitor<Expression>
        implements BindExpressionVisitor<Expression> {
    @Override
    public Expression visitFractionLiteral(BindExpressionParser.FractionLiteralContext ctx) {
        return new Expression.Fraction(Double.parseDouble(ctx.getText()));
    }

    @Override
    public Expression visitCollectionAccess(BindExpressionParser.CollectionAccessContext ctx) {
        return new Expression.CollectionAccess(visit(ctx.collection), visit(ctx.accessor));
    }

    @Override
    public Expression visitNullLiteral(BindExpressionParser.NullLiteralContext ctx) {
        return new Expression.Null();
    }

    @Override
    public Expression visitInvert(BindExpressionParser.InvertContext ctx) {
        return new Expression.Invert(visit(ctx.base));
    }

    @Override
    public Expression visitComparative(BindExpressionParser.ComparativeContext ctx) {
        Expression left = visit(ctx.left);
        Expression right = visit(ctx.right);
        return switch (ctx.operator.getText()) {
            case "==" -> new Expression.Equal(left, right);
            case "!=" -> new Expression.NotEqual(left, right);
            case ">=" -> new Expression.GreaterThanEqual(left, right);
            case ">" -> new Expression.GreaterThan(left, right);
            case "<=" -> new Expression.LessThanEqual(left, right);
            case "<" -> new Expression.LessThan(left, right);
            case String operator -> throw new IllegalArgumentException("Unknown operator: %s".formatted(operator));
        };
    }

    @Override
    public Expression visitMultiplicative(BindExpressionParser.MultiplicativeContext ctx) {
        Expression left = visit(ctx.left);
        Expression right = visit(ctx.right);
        return switch (ctx.operator.getText()) {
            case "*" -> new Expression.Multiply(left, right);
            case "/" -> new Expression.Divide(left, right);
            case "%" -> new Expression.Modulo(left, right);
            case String operator -> throw new IllegalArgumentException("Unknown operator: %s".formatted(operator));
        };
    }

    @Override
    public Expression visitLogical(BindExpressionParser.LogicalContext ctx) {
        Expression left = visit(ctx.left);
        Expression right = visit(ctx.right);
        return switch (ctx.operator.getText()) {
            case "&&" -> new Expression.And(left, right);
            case "||" -> new Expression.Or(left, right);
            case String operator -> throw new IllegalArgumentException("Unknown operator: %s".formatted(operator));
        };
    }

    @Override
    public Expression visitEnclosed(BindExpressionParser.EnclosedContext ctx) {
        return visit(ctx.inside);
    }

    @Override
    public Expression visitAdditive(BindExpressionParser.AdditiveContext ctx) {
        Expression left = visit(ctx.left);
        Expression right = visit(ctx.right);
        return switch (ctx.operator.getText()) {
            case "+" -> new Expression.Add(left, right);
            case "-" -> new Expression.Subtract(left, right);
            case String operator -> throw new IllegalArgumentException("Unknown operator: %s".formatted(operator));
        };
    }

    @Override
    public Expression visitStringLiteral(BindExpressionParser.StringLiteralContext ctx) {
        String text = ctx.getText();
        return new Expression.Str(text.substring(1, text.length() - 1));
    }

    @Override
    public Expression visitNegate(BindExpressionParser.NegateContext ctx) {
        return new Expression.Negate(visit(ctx.base));
    }

    @Override
    public Expression visitVariable(BindExpressionParser.VariableContext ctx) {
        return new Expression.Variable(ctx.getText());
    }

    @Override
    public Expression visitWholeLiteral(BindExpressionParser.WholeLiteralContext ctx) {
        return new Expression.Whole(Long.parseLong(ctx.getText()));
    }

    @Override
    public Expression visitPropertyRead(BindExpressionParser.PropertyReadContext ctx) {
        return new Expression.PropertyRead(visit(ctx.base), ctx.property.getText());
    }

    @Override
    public Expression visitBooleanLiteral(BindExpressionParser.BooleanLiteralContext ctx) {
        return switch (ctx.getText()) {
            case "true" -> new Expression.Boolean(true);
            case "false" -> new Expression.Boolean(false);
            case String val -> throw new IllegalArgumentException("Unknown boolean: %s".formatted(val));
        };
    }

    @Override
    public Expression visitMethodCall(BindExpressionParser.MethodCallContext ctx) {
        return new Expression.MethodCall(visit(ctx.base), ctx.method.getText(),
                                         ctx.args.stream().map(this::visit).toList());
    }
}
