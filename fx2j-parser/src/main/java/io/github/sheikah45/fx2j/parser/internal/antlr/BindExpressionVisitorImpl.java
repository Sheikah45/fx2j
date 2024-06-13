package io.github.sheikah45.fx2j.parser.internal.antlr;

import io.github.sheikah45.fx2j.parser.property.BindExpression;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

public class BindExpressionVisitorImpl extends AbstractParseTreeVisitor<BindExpression>
        implements BindExpressionVisitor<BindExpression> {
    @Override
    public BindExpression visitFractionLiteral(BindExpressionParser.FractionLiteralContext ctx) {
        return new BindExpression.Fraction(Double.parseDouble(ctx.getText()));
    }

    @Override
    public BindExpression visitCollectionAccess(BindExpressionParser.CollectionAccessContext ctx) {
        return new BindExpression.CollectionAccess(visit(ctx.collection), visit(ctx.accessor));
    }

    @Override
    public BindExpression visitNullLiteral(BindExpressionParser.NullLiteralContext ctx) {
        return new BindExpression.Null();
    }

    @Override
    public BindExpression visitInvert(BindExpressionParser.InvertContext ctx) {
        return new BindExpression.Invert(visit(ctx.base));
    }

    @Override
    public BindExpression visitComparative(BindExpressionParser.ComparativeContext ctx) {
        BindExpression left = visit(ctx.left);
        BindExpression right = visit(ctx.right);
        return switch (ctx.operator.getText()) {
            case "==" -> new BindExpression.Equal(left, right);
            case "!=" -> new BindExpression.NotEqual(left, right);
            case ">=" -> new BindExpression.GreaterThanEqual(left, right);
            case ">" -> new BindExpression.GreaterThan(left, right);
            case "<=" -> new BindExpression.LessThanEqual(left, right);
            case "<" -> new BindExpression.LessThan(left, right);
            case String operator -> throw new IllegalArgumentException("Unknown operator: %s".formatted(operator));
        };
    }

    @Override
    public BindExpression visitMultiplicative(BindExpressionParser.MultiplicativeContext ctx) {
        BindExpression left = visit(ctx.left);
        BindExpression right = visit(ctx.right);
        return switch (ctx.operator.getText()) {
            case "*" -> new BindExpression.Multiply(left, right);
            case "/" -> new BindExpression.Divide(left, right);
            case "%" -> new BindExpression.Modulo(left, right);
            case String operator -> throw new IllegalArgumentException("Unknown operator: %s".formatted(operator));
        };
    }

    @Override
    public BindExpression visitLogical(BindExpressionParser.LogicalContext ctx) {
        BindExpression left = visit(ctx.left);
        BindExpression right = visit(ctx.right);
        return switch (ctx.operator.getText()) {
            case "&&" -> new BindExpression.And(left, right);
            case "||" -> new BindExpression.Or(left, right);
            case String operator -> throw new IllegalArgumentException("Unknown operator: %s".formatted(operator));
        };
    }

    @Override
    public BindExpression visitEnclosed(BindExpressionParser.EnclosedContext ctx) {
        return visit(ctx.inside);
    }

    @Override
    public BindExpression visitAdditive(BindExpressionParser.AdditiveContext ctx) {
        BindExpression left = visit(ctx.left);
        BindExpression right = visit(ctx.right);
        return switch (ctx.operator.getText()) {
            case "+" -> new BindExpression.Add(left, right);
            case "-" -> new BindExpression.Subtract(left, right);
            case String operator -> throw new IllegalArgumentException("Unknown operator: %s".formatted(operator));
        };
    }

    @Override
    public BindExpression visitStringLiteral(BindExpressionParser.StringLiteralContext ctx) {
        String text = ctx.getText();
        return new BindExpression.String(text.substring(1, text.length() - 1));
    }

    @Override
    public BindExpression visitNegate(BindExpressionParser.NegateContext ctx) {
        return new BindExpression.Negate(visit(ctx.base));
    }

    @Override
    public BindExpression visitVariable(BindExpressionParser.VariableContext ctx) {
        return new BindExpression.Variable(ctx.getText());
    }

    @Override
    public BindExpression visitWholeLiteral(BindExpressionParser.WholeLiteralContext ctx) {
        return new BindExpression.Whole(Long.parseLong(ctx.getText()));
    }

    @Override
    public BindExpression visitPropertyRead(BindExpressionParser.PropertyReadContext ctx) {
        return new BindExpression.PropertyRead(visit(ctx.base), ctx.property.getText());
    }

    @Override
    public BindExpression visitBooleanLiteral(BindExpressionParser.BooleanLiteralContext ctx) {
        return switch (ctx.getText()) {
            case "true" -> new BindExpression.Boolean(true);
            case "false" -> new BindExpression.Boolean(false);
            case String val -> throw new IllegalArgumentException("Unknown boolean: %s".formatted(val));
        };
    }

    @Override
    public BindExpression visitMethodCall(BindExpressionParser.MethodCallContext ctx) {
        return new BindExpression.MethodCall(visit(ctx.base), ctx.method.getText(),
                                             ctx.args.stream().map(this::visit).toList());
    }
}
