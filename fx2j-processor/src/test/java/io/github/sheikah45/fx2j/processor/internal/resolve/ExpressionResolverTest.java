package io.github.sheikah45.fx2j.processor.internal.resolve;

import io.github.sheikah45.fx2j.parser.property.Expression;
import io.github.sheikah45.fx2j.processor.internal.model.CodeValue;
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
        assertEquals(new ExpressionResult(Object.class, "null", List.of()),
                     expressionResolver.resolveExpression(new Expression.Null()));
    }

    @Test
    void testResolveWhole() {
        assertEquals(new ExpressionResult(int.class, "1", List.of()),
                     expressionResolver.resolveExpression(new Expression.Whole(1)));
    }

    @Test
    void testResolveWholeLarge() {
        assertEquals(new ExpressionResult(long.class, String.valueOf(Integer.MAX_VALUE + 1L), List.of()),
                     expressionResolver.resolveExpression(new Expression.Whole(Integer.MAX_VALUE + 1L)));
    }

    @Test
    void testResolveFraction() {
        assertEquals(new ExpressionResult(float.class, "1.0", List.of()),
                     expressionResolver.resolveExpression(new Expression.Fraction(1)));
    }

    @Test
    void testResolveFractionLarge() {
        assertEquals(new ExpressionResult(double.class, String.valueOf(Float.MAX_VALUE * 2d), List.of()),
                     expressionResolver.resolveExpression(new Expression.Fraction(Float.MAX_VALUE * 2d)));
    }

    @Test
    void testResolveBoolean() {
        assertEquals(new ExpressionResult(boolean.class, "true", List.of()),
                     expressionResolver.resolveExpression(new Expression.Boolean(true)));
    }

    @Test
    void testResolveString() {
        assertEquals(new ExpressionResult(String.class, "\"hello\"", List.of()),
                     expressionResolver.resolveExpression(new Expression.String("hello")));
    }

    @Test
    void testResolveVariable() {
        resolverContainer.getNameResolver().storeIdType("a", Integer.class);
        assertEquals(new ExpressionResult(Integer.class, "a", List.of()),
                     expressionResolver.resolveExpression(new Expression.Variable("a")));
    }

    @Test
    void testResolvePropertyRead() {
        resolverContainer.getNameResolver().storeIdType("a", Label.class);
        assertEquals(new ExpressionResult(StringProperty.class, "stringProperty0",
                                          List.of(new CodeValue.Assignment(StringProperty.class, "stringProperty0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Literal("a"),
                                                                                   "textProperty", List.of())))),
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
        assertEquals(new ExpressionResult(Node.class, "node0", List.of(
                             new CodeValue.Assignment(Node.class, "node0",
                                                      new CodeValue.MethodCall(
                                                              new CodeValue.Literal(
                                                                      "a"),
                                                              "lookup",
                                                              List.of(new CodeValue.Literal(
                                                                      "\"parent\"")))))),
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
        assertEquals(new ExpressionResult(bindingType, "objectBinding0",
                                          List.of(new CodeValue.Assignment(bindingType, "objectBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Type(Bindings.class),
                                                                                   "valueAt",
                                                                                   List.of(new CodeValue.Literal("a"),
                                                                                           new CodeValue.Literal(
                                                                                                   "0")))))),
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
        assertEquals(new ExpressionResult(IntegerBinding.class, "integerBinding0",
                                          List.of(new CodeValue.Assignment(IntegerBinding.class, "integerBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Literal("a"), "negate",
                                                                                   List.of())))),
                     expressionResolver.resolveExpression(new Expression.Negate(new Expression.Variable("a"))));
    }

    @Test
    void testResolveNegateObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        assertEquals(new ExpressionResult(NumberBinding.class, "numberBinding0",
                                          List.of(new CodeValue.Assignment(NumberBinding.class, "numberBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Type(Bindings.class),
                                                                                   "negate",
                                                                                   List.of(new CodeValue.Literal(
                                                                                           "a")))))),
                     expressionResolver.resolveExpression(new Expression.Negate(new Expression.Variable("a"))));
    }

    @Test
    void testResolveAdd() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        assertEquals(new ExpressionResult(IntegerBinding.class, "integerBinding0",
                                          List.of(new CodeValue.Assignment(IntegerBinding.class, "integerBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Literal("a"), "add",
                                                                                   List.of(new CodeValue.Literal(
                                                                                           "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.Add(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveAddObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        assertEquals(new ExpressionResult(NumberBinding.class, "numberBinding0",
                                          List.of(new CodeValue.Assignment(NumberBinding.class, "numberBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Type(Bindings.class),
                                                                                   "add",
                                                                                   List.of(new CodeValue.Literal("a"),
                                                                                           new CodeValue.Literal(
                                                                                                   "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.Add(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveSubtract() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        assertEquals(new ExpressionResult(IntegerBinding.class, "integerBinding0",
                                          List.of(new CodeValue.Assignment(IntegerBinding.class, "integerBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Literal("a"),
                                                                                   "subtract",
                                                                                   List.of(new CodeValue.Literal(
                                                                                           "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.Subtract(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveSubtractObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        assertEquals(new ExpressionResult(NumberBinding.class, "numberBinding0",
                                          List.of(new CodeValue.Assignment(NumberBinding.class, "numberBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Type(Bindings.class),
                                                                                   "subtract",
                                                                                   List.of(new CodeValue.Literal("a"),
                                                                                           new CodeValue.Literal(
                                                                                                   "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.Subtract(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveMultiply() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        assertEquals(new ExpressionResult(IntegerBinding.class, "integerBinding0",
                                          List.of(new CodeValue.Assignment(IntegerBinding.class, "integerBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Literal("a"),
                                                                                   "multiply",
                                                                                   List.of(new CodeValue.Literal(
                                                                                           "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.Multiply(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveMultiplyObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        assertEquals(new ExpressionResult(NumberBinding.class, "numberBinding0",
                                          List.of(new CodeValue.Assignment(NumberBinding.class, "numberBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Type(Bindings.class),
                                                                                   "multiply",
                                                                                   List.of(new CodeValue.Literal("a"),
                                                                                           new CodeValue.Literal(
                                                                                                   "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.Multiply(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveDivide() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        assertEquals(new ExpressionResult(IntegerBinding.class, "integerBinding0",
                                          List.of(new CodeValue.Assignment(IntegerBinding.class, "integerBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Literal("a"), "divide",
                                                                                   List.of(new CodeValue.Literal(
                                                                                           "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.Divide(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveDivideObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        assertEquals(new ExpressionResult(NumberBinding.class, "numberBinding0",
                                          List.of(new CodeValue.Assignment(NumberBinding.class, "numberBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Type(Bindings.class),
                                                                                   "divide",
                                                                                   List.of(new CodeValue.Literal("a"),
                                                                                           new CodeValue.Literal(
                                                                                                   "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.Divide(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveGreaterThan() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        assertEquals(new ExpressionResult(BooleanBinding.class, "booleanBinding0",
                                          List.of(new CodeValue.Assignment(BooleanBinding.class, "booleanBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Literal("a"),
                                                                                   "greaterThan",
                                                                                   List.of(new CodeValue.Literal(
                                                                                           "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.GreaterThan(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveGreaterThanObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        assertEquals(new ExpressionResult(BooleanBinding.class, "booleanBinding0",
                                          List.of(new CodeValue.Assignment(BooleanBinding.class, "booleanBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Type(Bindings.class),
                                                                                   "greaterThan",
                                                                                   List.of(new CodeValue.Literal("a"),
                                                                                           new CodeValue.Literal(
                                                                                                   "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.GreaterThan(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveGreaterThanEqual() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        assertEquals(new ExpressionResult(BooleanBinding.class, "booleanBinding0",
                                          List.of(new CodeValue.Assignment(BooleanBinding.class, "booleanBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Literal("a"),
                                                                                   "greaterThanOrEqualTo",
                                                                                   List.of(new CodeValue.Literal(
                                                                                           "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.GreaterThanEqual(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveGreaterThanOrEqualObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        assertEquals(new ExpressionResult(BooleanBinding.class, "booleanBinding0",
                                          List.of(new CodeValue.Assignment(BooleanBinding.class, "booleanBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Type(Bindings.class),
                                                                                   "greaterThanOrEqual",
                                                                                   List.of(new CodeValue.Literal("a"),
                                                                                           new CodeValue.Literal(
                                                                                                   "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.GreaterThanEqual(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveLessThan() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        assertEquals(new ExpressionResult(BooleanBinding.class, "booleanBinding0",
                                          List.of(new CodeValue.Assignment(BooleanBinding.class, "booleanBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Literal("a"),
                                                                                   "lessThan",
                                                                                   List.of(new CodeValue.Literal(
                                                                                           "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.LessThan(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveLessThanObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        assertEquals(new ExpressionResult(BooleanBinding.class, "booleanBinding0",
                                          List.of(new CodeValue.Assignment(BooleanBinding.class, "booleanBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Type(Bindings.class),
                                                                                   "lessThan",
                                                                                   List.of(new CodeValue.Literal("a"),
                                                                                           new CodeValue.Literal(
                                                                                                   "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.LessThan(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveLessThanEqual() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        assertEquals(new ExpressionResult(BooleanBinding.class, "booleanBinding0",
                                          List.of(new CodeValue.Assignment(BooleanBinding.class, "booleanBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Literal("a"),
                                                                                   "lessThanOrEqualTo",
                                                                                   List.of(new CodeValue.Literal(
                                                                                           "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.LessThanEqual(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveLessThanOrEqualObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        assertEquals(new ExpressionResult(BooleanBinding.class, "booleanBinding0",
                                          List.of(new CodeValue.Assignment(BooleanBinding.class, "booleanBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Type(Bindings.class),
                                                                                   "lessThanOrEqual",
                                                                                   List.of(new CodeValue.Literal("a"),
                                                                                           new CodeValue.Literal(
                                                                                                   "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.LessThanEqual(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveEqual() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        assertEquals(new ExpressionResult(BooleanBinding.class, "booleanBinding0",
                                          List.of(new CodeValue.Assignment(BooleanBinding.class, "booleanBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Literal("a"),
                                                                                   "isEqualTo",
                                                                                   List.of(new CodeValue.Literal(
                                                                                           "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.Equal(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveEqualObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        assertEquals(new ExpressionResult(BooleanBinding.class, "booleanBinding0",
                                          List.of(new CodeValue.Assignment(BooleanBinding.class, "booleanBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Type(Bindings.class),
                                                                                   "equal",
                                                                                   List.of(new CodeValue.Literal("a"),
                                                                                           new CodeValue.Literal(
                                                                                                   "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.Equal(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveNotEqual() {
        resolverContainer.getNameResolver().storeIdType("a", IntegerProperty.class);
        assertEquals(new ExpressionResult(BooleanBinding.class, "booleanBinding0",
                                          List.of(new CodeValue.Assignment(BooleanBinding.class, "booleanBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Literal("a"),
                                                                                   "isNotEqualTo",
                                                                                   List.of(new CodeValue.Literal(
                                                                                           "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.NotEqual(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveNotEqualObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableNumberValue.class);
        assertEquals(new ExpressionResult(BooleanBinding.class, "booleanBinding0",
                                          List.of(new CodeValue.Assignment(BooleanBinding.class, "booleanBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Type(Bindings.class),
                                                                                   "notEqual",
                                                                                   List.of(new CodeValue.Literal("a"),
                                                                                           new CodeValue.Literal(
                                                                                                   "0")))))),
                     expressionResolver.resolveExpression(
                             new Expression.NotEqual(new Expression.Variable("a"), new Expression.Whole(0))));
    }

    @Test
    void testResolveAnd() {
        resolverContainer.getNameResolver().storeIdType("a", BooleanProperty.class);
        resolverContainer.getNameResolver().storeIdType("b", BooleanProperty.class);
        assertEquals(new ExpressionResult(BooleanBinding.class, "booleanBinding0",
                                          List.of(new CodeValue.Assignment(BooleanBinding.class, "booleanBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Literal("a"), "and",
                                                                                   List.of(new CodeValue.Literal(
                                                                                           "b")))))),
                     expressionResolver.resolveExpression(
                             new Expression.And(new Expression.Variable("a"), new Expression.Variable("b"))));
    }

    @Test
    void testResolveAndObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableBooleanValue.class);
        resolverContainer.getNameResolver().storeIdType("b", ObservableBooleanValue.class);
        assertEquals(new ExpressionResult(BooleanBinding.class, "booleanBinding0",
                                          List.of(new CodeValue.Assignment(BooleanBinding.class, "booleanBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Type(Bindings.class),
                                                                                   "and",
                                                                                   List.of(new CodeValue.Literal("a"),
                                                                                           new CodeValue.Literal(
                                                                                                   "b")))))),
                     expressionResolver.resolveExpression(
                             new Expression.And(new Expression.Variable("a"), new Expression.Variable("b"))));
    }

    @Test
    void testResolveOr() {
        resolverContainer.getNameResolver().storeIdType("a", BooleanProperty.class);
        resolverContainer.getNameResolver().storeIdType("b", BooleanProperty.class);
        assertEquals(new ExpressionResult(BooleanBinding.class, "booleanBinding0",
                                          List.of(new CodeValue.Assignment(BooleanBinding.class, "booleanBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Literal("a"), "or",
                                                                                   List.of(new CodeValue.Literal(
                                                                                           "b")))))),
                     expressionResolver.resolveExpression(
                             new Expression.Or(new Expression.Variable("a"), new Expression.Variable("b"))));
    }

    @Test
    void testResolveOrObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableBooleanValue.class);
        resolverContainer.getNameResolver().storeIdType("b", ObservableBooleanValue.class);
        assertEquals(new ExpressionResult(BooleanBinding.class, "booleanBinding0",
                                          List.of(new CodeValue.Assignment(BooleanBinding.class, "booleanBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Type(Bindings.class),
                                                                                   "or",
                                                                                   List.of(new CodeValue.Literal("a"),
                                                                                           new CodeValue.Literal(
                                                                                                   "b")))))),
                     expressionResolver.resolveExpression(
                             new Expression.Or(new Expression.Variable("a"), new Expression.Variable("b"))));
    }

    @Test
    void testResolveNot() {
        resolverContainer.getNameResolver().storeIdType("a", BooleanProperty.class);
        assertEquals(new ExpressionResult(BooleanBinding.class, "booleanBinding0",
                                          List.of(new CodeValue.Assignment(BooleanBinding.class, "booleanBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Literal("a"), "not",
                                                                                   List.of())))),
                     expressionResolver.resolveExpression(new Expression.Invert(new Expression.Variable("a"))));
    }

    @Test
    void testResolveNotObservable() {
        resolverContainer.getNameResolver().storeIdType("a", ObservableBooleanValue.class);
        assertEquals(new ExpressionResult(BooleanBinding.class, "booleanBinding0",
                                          List.of(new CodeValue.Assignment(BooleanBinding.class, "booleanBinding0",
                                                                           new CodeValue.MethodCall(
                                                                                   new CodeValue.Type(Bindings.class),
                                                                                   "not", List.of(new CodeValue.Literal(
                                                                                   "a")))))),
                     expressionResolver.resolveExpression(new Expression.Invert(new Expression.Variable("a"))));
    }

}
