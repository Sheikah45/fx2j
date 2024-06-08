package io.github.sheikah45.fx2j.processor.internal.resolve;

import io.github.sheikah45.fx2j.parser.property.Expression;
import io.github.sheikah45.fx2j.processor.internal.code.CodeValue;
import io.github.sheikah45.fx2j.processor.internal.code.CodeValues;
import io.github.sheikah45.fx2j.processor.internal.model.ExpressionResult;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableNumberValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExpressionResolverTest extends AbstractResolverTest {

    private final ExpressionResolver expressionResolver = resolverContainer.getExpressionResolver();

    @Test
    void testResolveNull() {
        assertEquals(new ExpressionResult(Object.class, CodeValues.nullValue(), List.of()),
                     expressionResolver.resolveExpression(new Expression.Null()));
    }

    @Test
    void testResolveWhole() {
        int value = 1;
        assertEquals(new ExpressionResult(int.class, CodeValues.literal(value), List.of()),
                     expressionResolver.resolveExpression(new Expression.Whole(value)));
    }

    @Test
    void testResolveWholeLarge() {
        long value = Integer.MAX_VALUE + 1L;
        assertEquals(new ExpressionResult(long.class, CodeValues.literal(value), List.of()),
                     expressionResolver.resolveExpression(new Expression.Whole(value)));
    }

    @Test
    void testResolveWholeLargeNegative() {
        long value = Integer.MIN_VALUE - 1L;
        assertEquals(new ExpressionResult(long.class, CodeValues.literal(value), List.of()),
                     expressionResolver.resolveExpression(new Expression.Whole(value)));
    }

    @Test
    void testResolveFraction() {
        assertEquals(new ExpressionResult(float.class, CodeValues.literal(1f), List.of()),
                     expressionResolver.resolveExpression(new Expression.Fraction(1)));
    }

    @Test
    void testResolveFractionLarge() {
        double value = Float.MAX_VALUE * 2d;
        assertEquals(new ExpressionResult(double.class, CodeValues.literal(value), List.of()),
                     expressionResolver.resolveExpression(new Expression.Fraction(value)));
    }

    @Test
    void testResolveFractionLargeNegative() {
        double value = -(Float.MAX_VALUE * 2d);
        assertEquals(new ExpressionResult(double.class, CodeValues.literal(value), List.of()),
                     expressionResolver.resolveExpression(new Expression.Fraction(value)));
    }

    @Test
    void testResolveBoolean() {
        assertEquals(new ExpressionResult(boolean.class, CodeValues.literal(true), List.of()),
                     expressionResolver.resolveExpression(new Expression.Boolean(true)));
    }

    @Test
    void testResolveString() {
        assertEquals(new ExpressionResult(String.class, CodeValues.literal("hello"), List.of()),
                     expressionResolver.resolveExpression(new Expression.String("hello")));
    }

    @Test
    void testResolveVariable() {
        resolverContainer.getNameResolver().storeIdType("a", Integer.class);
        assertEquals(new ExpressionResult(Integer.class, CodeValues.variable("a"), List.of()),
                     expressionResolver.resolveExpression(new Expression.Variable("a")));
    }

    @Test
    void testResolvePropertyRead() {
        resolverContainer.getNameResolver().storeIdType("a", Label.class);
        CodeValue.Variable stringProperty0 = CodeValues.variable("stringProperty0");
        assertEquals(new ExpressionResult(StringProperty.class, stringProperty0,
                                          List.of(CodeValues.declaration(StringProperty.class, stringProperty0,
                                                                         CodeValues.methodCall("a", "textProperty")))),
                     expressionResolver.resolveExpression(
                             new Expression.PropertyRead(new Expression.Variable("a"), "text")));
    }

    @Test
    void testResolvePropertyReadUnknownProperty() {
        resolverContainer.getNameResolver().storeIdType("a", Label.class);
        assertThrows(IllegalArgumentException.class, () -> expressionResolver.resolveExpression(
                new Expression.PropertyRead(new Expression.Variable("a"), "blank")));
    }

    @Test
    void testResolveMethodCall() {
        resolverContainer.getNameResolver().storeIdType("a", Label.class);
        CodeValue.Variable variable = CodeValues.variable("node0");
        assertEquals(new ExpressionResult(Node.class, variable, List.of(CodeValues.declaration(Node.class, variable,
                                                                                               CodeValues.methodCall(
                                                                                                       "a", "lookup",
                                                                                                       "parent")))),
                     expressionResolver.resolveExpression(
                             new Expression.MethodCall(new Expression.Variable("a"), "lookup",
                                                       List.of(new Expression.String("parent")))));
    }

    @Test
    void testResolveMethodCallUnknownProperty() {
        resolverContainer.getNameResolver().storeIdType("a", Label.class);
        assertThrows(IllegalArgumentException.class, () -> expressionResolver.resolveExpression(
                new Expression.MethodCall(new Expression.Variable("a"), "getBlank", List.of())));
    }

    @Test
    void testResolveCollectionAccess() {
        Method valueAt = resolverContainer.getMethodResolver()
                                          .findMethod(Bindings.class, "valueAt", ObservableList.class, int.class)
                                          .orElseThrow();
        resolverContainer.getNameResolver().storeIdType("a", ObservableList.class);
        Type bindingType = valueAt.getGenericReturnType();
        CodeValue.Variable variable = CodeValues.variable("objectBinding0");
        assertEquals(new ExpressionResult(bindingType, variable, List.of(CodeValues.declaration(bindingType, variable,
                                                                                                CodeValues.methodCall(
                                                                                                        valueAt,
                                                                                                        CodeValues.variable(
                                                                                                                "a"),
                                                                                                        0)))),
                     expressionResolver.resolveExpression(
                             new Expression.CollectionAccess(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveCollectionAccessUnknownProperty() {
        resolverContainer.getNameResolver().storeIdType("a", Label.class);
        assertThrows(IllegalArgumentException.class, () -> expressionResolver.resolveExpression(
                new Expression.MethodCall(new Expression.Variable("a"), "getBlank", List.of())));
    }

    @Test
    void testResolveNegate() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        CodeValue.Variable variable = CodeValues.variable("integerBinding0");
        assertEquals(new ExpressionResult(IntegerBinding.class, variable,
                                          List.of(CodeValues.declaration(IntegerBinding.class, variable,
                                                                         CodeValues.methodCall("a", "negate")))),
                     expressionResolver.resolveExpression(new Expression.Negate(new Expression.Variable("a"))));
    }

    @Test
    void testResolveNegateObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        CodeValue.Variable variable = CodeValues.variable("numberBinding0");
        assertEquals(new ExpressionResult(NumberBinding.class, variable,
                                          List.of(CodeValues.declaration(NumberBinding.class, variable,
                                                                         CodeValues.methodCall(Bindings.class, "negate",
                                                                                               CodeValues.variable(
                                                                                                       "a"))))),
                     expressionResolver.resolveExpression(new Expression.Negate(new Expression.Variable("a"))));
    }

    @Test
    void testResolveAdd() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        CodeValue.Variable variable = CodeValues.variable("integerBinding0");
        assertEquals(new ExpressionResult(IntegerBinding.class, variable,
                                          List.of(CodeValues.declaration(IntegerBinding.class, variable,
                                                                         CodeValues.methodCall(CodeValues.variable("a"),
                                                                                               "add", 0)))),
                     expressionResolver.resolveExpression(
                             new Expression.Add(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveAddObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        CodeValue.Variable variable = CodeValues.variable("numberBinding0");
        assertEquals(new ExpressionResult(NumberBinding.class, variable,
                                          List.of(CodeValues.declaration(NumberBinding.class, variable,
                                                                         CodeValues.methodCall(Bindings.class, "add",
                                                                                               CodeValues.variable("a"),
                                                                                               0)))),
                     expressionResolver.resolveExpression(
                             new Expression.Add(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveSubtract() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        CodeValue.Variable variable = CodeValues.variable("integerBinding0");
        assertEquals(new ExpressionResult(IntegerBinding.class, variable,
                                          List.of(CodeValues.declaration(IntegerBinding.class, variable,
                                                                         CodeValues.methodCall(CodeValues.variable("a"),
                                                                                               "subtract", 0)))),
                     expressionResolver.resolveExpression(
                             new Expression.Subtract(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveSubtractObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        CodeValue.Variable variable = CodeValues.variable("numberBinding0");
        assertEquals(new ExpressionResult(NumberBinding.class, variable,
                                          List.of(CodeValues.declaration(NumberBinding.class, variable,
                                                                         CodeValues.methodCall(Bindings.class,
                                                                                               "subtract",
                                                                                               CodeValues.variable("a"),
                                                                                               0)))),
                     expressionResolver.resolveExpression(
                             new Expression.Subtract(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveMultiply() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        CodeValue.Variable variable = CodeValues.variable("integerBinding0");
        assertEquals(new ExpressionResult(IntegerBinding.class, variable,
                                          List.of(CodeValues.declaration(IntegerBinding.class, variable,
                                                                         CodeValues.methodCall(CodeValues.variable("a"),
                                                                                               "multiply", 0)))),
                     expressionResolver.resolveExpression(
                             new Expression.Multiply(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveMultiplyObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        CodeValue.Variable variable = CodeValues.variable("numberBinding0");
        assertEquals(new ExpressionResult(NumberBinding.class, variable,
                                          List.of(CodeValues.declaration(NumberBinding.class, variable,
                                                                         CodeValues.methodCall(Bindings.class,
                                                                                               "multiply",
                                                                                               CodeValues.variable("a"),
                                                                                               0)))),
                     expressionResolver.resolveExpression(
                             new Expression.Multiply(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveDivide() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        CodeValue.Variable variable = CodeValues.variable("integerBinding0");
        assertEquals(new ExpressionResult(IntegerBinding.class, variable,
                                          List.of(CodeValues.declaration(IntegerBinding.class, variable,
                                                                         CodeValues.methodCall(CodeValues.variable("a"),
                                                                                               "divide", 0)))),
                     expressionResolver.resolveExpression(
                             new Expression.Divide(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveDivideObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        CodeValue.Variable variable = CodeValues.variable("numberBinding0");
        assertEquals(new ExpressionResult(NumberBinding.class, variable,
                                          List.of(CodeValues.declaration(NumberBinding.class, variable,
                                                                         CodeValues.methodCall(Bindings.class, "divide",
                                                                                               CodeValues.variable("a"),
                                                                                               0)))),
                     expressionResolver.resolveExpression(
                             new Expression.Divide(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveModulo() {
        assertThrows(UnsupportedOperationException.class, () -> expressionResolver.resolveExpression(
                new Expression.Modulo(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveGreaterThan() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        CodeValue.Variable variable = CodeValues.variable("booleanBinding0");
        assertEquals(new ExpressionResult(BooleanBinding.class, variable,
                                          List.of(CodeValues.declaration(BooleanBinding.class, variable,
                                                                         CodeValues.methodCall(CodeValues.variable("a"),
                                                                                               "greaterThan", 0)))),
                     expressionResolver.resolveExpression(
                             new Expression.GreaterThan(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveGreaterThanObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        CodeValue.Variable variable = CodeValues.variable("booleanBinding0");
        assertEquals(new ExpressionResult(BooleanBinding.class, variable,
                                          List.of(CodeValues.declaration(BooleanBinding.class, variable,
                                                                         CodeValues.methodCall(Bindings.class,
                                                                                               "greaterThan",
                                                                                               CodeValues.variable("a"),
                                                                                               0)))),
                     expressionResolver.resolveExpression(
                             new Expression.GreaterThan(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveGreaterThanEqual() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        CodeValue.Variable variable = CodeValues.variable("booleanBinding0");
        assertEquals(new ExpressionResult(BooleanBinding.class, variable,
                                          List.of(CodeValues.declaration(BooleanBinding.class, variable,
                                                                         CodeValues.methodCall(CodeValues.variable("a"),
                                                                                               "greaterThanOrEqualTo",
                                                                                               0)))),
                     expressionResolver.resolveExpression(
                             new Expression.GreaterThanEqual(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveGreaterThanOrEqualObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        CodeValue.Variable variable = CodeValues.variable("booleanBinding0");
        assertEquals(new ExpressionResult(BooleanBinding.class, variable,
                                          List.of(CodeValues.declaration(BooleanBinding.class, variable,
                                                                         CodeValues.methodCall(Bindings.class,
                                                                                               "greaterThanOrEqual",
                                                                                               CodeValues.variable("a"),
                                                                                               0)))),
                     expressionResolver.resolveExpression(
                             new Expression.GreaterThanEqual(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveLessThan() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        CodeValue.Variable variable = CodeValues.variable("booleanBinding0");
        assertEquals(new ExpressionResult(BooleanBinding.class, variable,
                                          List.of(CodeValues.declaration(BooleanBinding.class, variable,
                                                                         CodeValues.methodCall(CodeValues.variable("a"),
                                                                                               "lessThan", 0)))),
                     expressionResolver.resolveExpression(
                             new Expression.LessThan(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveLessThanObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        CodeValue.Variable variable = CodeValues.variable("booleanBinding0");
        assertEquals(new ExpressionResult(BooleanBinding.class, variable,
                                          List.of(CodeValues.declaration(BooleanBinding.class, variable,
                                                                         CodeValues.methodCall(Bindings.class,
                                                                                               "lessThan",
                                                                                               CodeValues.variable("a"),
                                                                                               0)))),
                     expressionResolver.resolveExpression(
                             new Expression.LessThan(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveLessThanEqual() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        CodeValue.Variable variable = CodeValues.variable("booleanBinding0");
        assertEquals(new ExpressionResult(BooleanBinding.class, variable,
                                          List.of(CodeValues.declaration(BooleanBinding.class, variable,
                                                                         CodeValues.methodCall(CodeValues.variable("a"),
                                                                                               "lessThanOrEqualTo",
                                                                                               0)))),
                     expressionResolver.resolveExpression(
                             new Expression.LessThanEqual(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveLessThanOrEqualObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        CodeValue.Variable variable = CodeValues.variable("booleanBinding0");
        assertEquals(new ExpressionResult(BooleanBinding.class, variable,
                                          List.of(CodeValues.declaration(BooleanBinding.class, variable,
                                                                         CodeValues.methodCall(Bindings.class,
                                                                                               "lessThanOrEqual",
                                                                                               CodeValues.variable("a"),
                                                                                               0)))),
                     expressionResolver.resolveExpression(
                             new Expression.LessThanEqual(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveEqual() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        CodeValue.Variable variable = CodeValues.variable("booleanBinding0");
        assertEquals(new ExpressionResult(BooleanBinding.class, variable,
                                          List.of(CodeValues.declaration(BooleanBinding.class, variable,
                                                                         CodeValues.methodCall(CodeValues.variable("a"),
                                                                                               "isEqualTo", 0)))),
                     expressionResolver.resolveExpression(
                             new Expression.Equal(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveEqualObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        CodeValue.Variable variable = CodeValues.variable("booleanBinding0");
        assertEquals(new ExpressionResult(BooleanBinding.class, variable,
                                          List.of(CodeValues.declaration(BooleanBinding.class, variable,
                                                                         CodeValues.methodCall(Bindings.class, "equal",
                                                                                               CodeValues.variable("a"),
                                                                                               0)))),
                     expressionResolver.resolveExpression(
                             new Expression.Equal(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveNotEqual() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        CodeValue.Variable variable = CodeValues.variable("booleanBinding0");
        assertEquals(new ExpressionResult(BooleanBinding.class, variable,
                                          List.of(CodeValues.declaration(BooleanBinding.class, variable,
                                                                         CodeValues.methodCall(CodeValues.variable("a"),
                                                                                               "isNotEqualTo", 0)))),
                     expressionResolver.resolveExpression(
                             new Expression.NotEqual(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveNotEqualObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        CodeValue.Variable variable = CodeValues.variable("booleanBinding0");
        assertEquals(new ExpressionResult(BooleanBinding.class, variable,
                                          List.of(CodeValues.declaration(BooleanBinding.class, variable,
                                                                         CodeValues.methodCall(Bindings.class,
                                                                                               "notEqual",
                                                                                               CodeValues.variable("a"),
                                                                                               0)))),
                     expressionResolver.resolveExpression(
                             new Expression.NotEqual(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveAnd() {
        resolverContainer.getNameResolver().storeIdType("a", BooleanProperty.class);
        resolverContainer.getNameResolver().storeIdType("b", BooleanProperty.class);
        CodeValue.Variable variable = CodeValues.variable("booleanBinding0");
        assertEquals(new ExpressionResult(BooleanBinding.class, variable,
                                          List.of(CodeValues.declaration(BooleanBinding.class, variable,
                                                                         CodeValues.methodCall(CodeValues.variable("a"),
                                                                                               "and",
                                                                                               CodeValues.variable(
                                                                                                       "b"))))),
                     expressionResolver.resolveExpression(
                             new Expression.And(new Expression.Variable("a"), new Expression.Variable("b"))));
    }

    @Test
    void testResolveAndObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableBooleanValue.class);
        resolverContainer.getNameResolver().storeIdType("b", ObservableBooleanValue.class);
        CodeValue.Variable variable = CodeValues.variable("booleanBinding0");
        assertEquals(new ExpressionResult(BooleanBinding.class, variable,
                                          List.of(CodeValues.declaration(BooleanBinding.class, variable,
                                                                         CodeValues.methodCall(Bindings.class, "and",
                                                                                               CodeValues.variable("a"),
                                                                                               CodeValues.variable(
                                                                                                       "b"))))),
                     expressionResolver.resolveExpression(
                             new Expression.And(new Expression.Variable("a"), new Expression.Variable("b"))));
    }

    @Test
    void testResolveOr() {
        resolverContainer.getNameResolver().storeIdType("a", BooleanProperty.class);
        resolverContainer.getNameResolver().storeIdType("b", BooleanProperty.class);
        CodeValue.Variable variable = CodeValues.variable("booleanBinding0");
        assertEquals(new ExpressionResult(BooleanBinding.class, variable,
                                          List.of(CodeValues.declaration(BooleanBinding.class, variable,
                                                                         CodeValues.methodCall(CodeValues.variable("a"),
                                                                                               "or",
                                                                                               CodeValues.variable(
                                                                                                       "b"))))),
                     expressionResolver.resolveExpression(
                             new Expression.Or(new Expression.Variable("a"), new Expression.Variable("b"))));
    }

    @Test
    void testResolveOrObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableBooleanValue.class);
        resolverContainer.getNameResolver().storeIdType("b", ObservableBooleanValue.class);
        CodeValue.Variable variable = CodeValues.variable("booleanBinding0");
        assertEquals(new ExpressionResult(BooleanBinding.class, variable,
                                          List.of(CodeValues.declaration(BooleanBinding.class, variable,
                                                                         CodeValues.methodCall(Bindings.class, "or",
                                                                                               CodeValues.variable("a"),
                                                                                               CodeValues.variable(
                                                                                                       "b"))))),
                     expressionResolver.resolveExpression(
                             new Expression.Or(new Expression.Variable("a"), new Expression.Variable("b"))));
    }

    @Test
    void testResolveNot() {
        resolverContainer.getNameResolver().storeIdType("a", BooleanProperty.class);
        CodeValue.Variable variable = CodeValues.variable("booleanBinding0");
        assertEquals(new ExpressionResult(BooleanBinding.class, variable,
                                          List.of(CodeValues.declaration(BooleanBinding.class, variable,
                                                                         CodeValues.methodCall(CodeValues.variable("a"),
                                                                                               "not")))),
                     expressionResolver.resolveExpression(new Expression.Invert(new Expression.Variable("a"))));
    }

    @Test
    void testResolveNotObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableBooleanValue.class);
        CodeValue.Variable variable = CodeValues.variable("booleanBinding0");
        assertEquals(new ExpressionResult(BooleanBinding.class, variable,
                                          List.of(CodeValues.declaration(BooleanBinding.class, variable,
                                                                         CodeValues.methodCall(Bindings.class, "not",
                                                                                               CodeValues.variable(
                                                                                                       "a"))))),
                     expressionResolver.resolveExpression(new Expression.Invert(new Expression.Variable("a"))));
    }

}
