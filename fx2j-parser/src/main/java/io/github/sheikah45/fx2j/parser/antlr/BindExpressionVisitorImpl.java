package io.github.sheikah45.fx2j.parser.antlr;

import io.github.sheikah45.fx2j.parser.property.Value;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

public class BindExpressionVisitorImpl extends AbstractParseTreeVisitor<Value.Expression>
        implements BindExpressionVisitor<Value.Expression> {
    @Override
    public Value.Expression visitDecimalLiteral(BindExpressionParser.DecimalLiteralContext ctx) {
        return new Value.Expression.Whole(Long.parseLong(ctx.getText()));
    }

    @Override
    public Value.Expression visitLessThanEqual(BindExpressionParser.LessThanEqualContext ctx) {
        return new Value.Expression.LessThanEqual(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Value.Expression visitCollectionAccess(BindExpressionParser.CollectionAccessContext ctx) {
        return new Value.Expression.CollectionAccess(ctx.collection.accept(this), ctx.accessor.accept(this));
    }

    @Override
    public Value.Expression visitFractionalLiteral(BindExpressionParser.FractionalLiteralContext ctx) {
        return new Value.Expression.Fraction(Double.parseDouble(ctx.getText()));
    }

    @Override
    public Value.Expression visitTrueLiteral(BindExpressionParser.TrueLiteralContext ctx) {
        return new Value.Expression.True();
    }

    @Override
    public Value.Expression visitEnclosed(BindExpressionParser.EnclosedContext ctx) {
        return ctx.inside.accept(this);
    }

    @Override
    public Value.Expression visitFalseLiteral(BindExpressionParser.FalseLiteralContext ctx) {
        return new Value.Expression.False();
    }

    @Override
    public Value.Expression visitAnd(BindExpressionParser.AndContext ctx) {
        return new Value.Expression.And(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Value.Expression visitLessThan(BindExpressionParser.LessThanContext ctx) {
        return new Value.Expression.LessThan(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Value.Expression visitDivide(BindExpressionParser.DivideContext ctx) {
        return new Value.Expression.Divide(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Value.Expression visitMultiply(BindExpressionParser.MultiplyContext ctx) {
        return new Value.Expression.Multiply(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Value.Expression visitEquality(BindExpressionParser.EqualityContext ctx) {
        return new Value.Expression.Equality(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Value.Expression visitGreaterThan(BindExpressionParser.GreaterThanContext ctx) {
        return new Value.Expression.GreaterThan(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Value.Expression visitAdd(BindExpressionParser.AddContext ctx) {
        return new Value.Expression.Add(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Value.Expression visitGreaterThanEqual(BindExpressionParser.GreaterThanEqualContext ctx) {
        return new Value.Expression.GreaterThanEqual(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Value.Expression visitNullLiteral(BindExpressionParser.NullLiteralContext ctx) {
        return new Value.Expression.Null();
    }

    @Override
    public Value.Expression visitOr(BindExpressionParser.OrContext ctx) {
        return new Value.Expression.Or(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Value.Expression visitInvert(BindExpressionParser.InvertContext ctx) {
        return new Value.Expression.Invert(ctx.base.accept(this));
    }

    @Override
    public Value.Expression visitSubtract(BindExpressionParser.SubtractContext ctx) {
        return new Value.Expression.Subtract(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Value.Expression visitInequality(BindExpressionParser.InequalityContext ctx) {
        return new Value.Expression.Inequality(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Value.Expression visitStringLiteral(BindExpressionParser.StringLiteralContext ctx) {
        return new Value.Expression.Str(ctx.getText());
    }

    @Override
    public Value.Expression visitNegate(BindExpressionParser.NegateContext ctx) {
        return new Value.Expression.Negate(ctx.base.accept(this));
    }

    @Override
    public Value.Expression visitVariable(BindExpressionParser.VariableContext ctx) {
        return new Value.Expression.Variable(ctx.getText());
    }

    @Override
    public Value.Expression visitPropertyRead(BindExpressionParser.PropertyReadContext ctx) {
        return new Value.Expression.PropertyRead(ctx.base.accept(this), ctx.property.getText());
    }

    @Override
    public Value.Expression visitRemainder(BindExpressionParser.RemainderContext ctx) {
        return new Value.Expression.Remainder(ctx.left.accept(this), ctx.right.accept(this));
    }

    @Override
    public Value.Expression visitMethodCall(BindExpressionParser.MethodCallContext ctx) {
        return new Value.Expression.MethodCall(ctx.base.accept(this), ctx.method.getText(),
                                               ctx.args.stream().map(arg -> arg.accept(this)).toList());
    }
}
