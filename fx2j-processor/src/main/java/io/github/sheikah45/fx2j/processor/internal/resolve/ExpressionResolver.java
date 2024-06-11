package io.github.sheikah45.fx2j.processor.internal.resolve;

import io.github.sheikah45.fx2j.parser.property.BindExpression;
import io.github.sheikah45.fx2j.processor.internal.code.CodeValues;
import io.github.sheikah45.fx2j.processor.internal.code.Expression;
import io.github.sheikah45.fx2j.processor.internal.code.Statement;
import io.github.sheikah45.fx2j.processor.internal.model.ExpressionResult;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ExpressionResolver {

    private static final String BINDINGS_CLASS_NAME = "javafx.beans.binding.Bindings";

    private final TypeResolver typeResolver;
    private final MethodResolver methodResolver;
    private final NameResolver nameResolver;

    ExpressionResolver(TypeResolver typeResolver, MethodResolver methodResolver, NameResolver nameResolver) {
        this.typeResolver = typeResolver;
        this.methodResolver = methodResolver;
        this.nameResolver = nameResolver;
    }

    public ExpressionResult resolveExpression(BindExpression value) {
        return switch (value) {
            case BindExpression.Null() -> new ExpressionResult(Object.class, CodeValues.nullValue(), List.of());
            case BindExpression.Whole(long val) when val > Integer.MAX_VALUE || val < Integer.MIN_VALUE ->
                    new ExpressionResult(long.class, CodeValues.literal(val), List.of());
            case BindExpression.Whole(long val) ->
                    new ExpressionResult(int.class, CodeValues.literal((int) val), List.of());
            case BindExpression.Fraction(double val) when val > Float.MAX_VALUE || val < Float.MIN_VALUE ->
                    new ExpressionResult(double.class, CodeValues.literal(val), List.of());
            case BindExpression.Fraction(double val) ->
                    new ExpressionResult(float.class, CodeValues.literal((float) val), List.of());
            case BindExpression.Boolean(boolean val) ->
                    new ExpressionResult(boolean.class, CodeValues.literal(val), List.of());
            case BindExpression.String(String val) ->
                    new ExpressionResult(String.class, CodeValues.literal(val), List.of());
            case BindExpression.Variable(String name) ->
                    new ExpressionResult(nameResolver.resolveTypeById(name), CodeValues.variable(name), List.of());
            case BindExpression.PropertyRead(BindExpression bindExpression, String property) -> {
                ExpressionResult expressionResult = resolveExpression(bindExpression);
                List<Statement.Declaration> initializers = new ArrayList<>(expressionResult.initializers());

                Method readProperty = methodResolver.resolveProperty(expressionResult.type(), property)
                                                    .orElseThrow(() -> new IllegalArgumentException(
                                                            "No property found for expression binding %s".formatted(
                                                                    property)));
                Type valueType = readProperty.getGenericReturnType();
                String identifier = nameResolver.resolveUniqueName(valueType);

                initializers.add(CodeValues.declaration(valueType, identifier,
                                                        CodeValues.methodCall(expressionResult.value(), readProperty)));
                yield new ExpressionResult(valueType, CodeValues.variable(identifier), initializers);
            }
            case BindExpression.MethodCall(
                    BindExpression bindExpression, String methodName, List<BindExpression> args
            ) -> {
                ExpressionResult expressionResult = resolveExpression(bindExpression);
                List<Statement.Declaration> initializers = new ArrayList<>(expressionResult.initializers());
                List<Type> parameterTypes = new ArrayList<>();
                List<Expression> methodArgs = new ArrayList<>();
                for (BindExpression arg : args) {
                    ExpressionResult argResult = resolveExpression(arg);
                    parameterTypes.add(argResult.type());
                    methodArgs.add(argResult.value());
                    initializers.addAll(argResult.initializers());
                }

                Method method = methodResolver.findMethod(expressionResult.type(), methodName,
                                                          parameterTypes.toArray(Type[]::new))
                                              .orElseThrow(() -> new IllegalArgumentException(
                                                      "No method found for class %s method name %s and parameters %s".formatted(
                                                              expressionResult.type(), methodName, parameterTypes)));
                Type valueType = method.getGenericReturnType();
                String identifier = nameResolver.resolveUniqueName(valueType);

                initializers.add(CodeValues.declaration(valueType, identifier,
                                                        CodeValues.methodCall(expressionResult.value(), method,
                                                                              methodArgs.toArray())));
                yield new ExpressionResult(valueType, CodeValues.variable(identifier), initializers);
            }
            case BindExpression.CollectionAccess(BindExpression bindExpression, BindExpression key) -> {
                ExpressionResult expressionResult = resolveExpression(bindExpression);
                ExpressionResult keyResult = resolveExpression(key);
                List<Statement.Declaration> initializers = new ArrayList<>();
                initializers.addAll(expressionResult.initializers());
                initializers.addAll(keyResult.initializers());
                Class<?> bindingsClass = typeResolver.resolve(BINDINGS_CLASS_NAME);
                Method valueAtMethod = methodResolver.findMethod(bindingsClass, "valueAt", expressionResult.type(),
                                                                 keyResult.type())
                                                     .orElseThrow(() -> new IllegalArgumentException(
                                                             "Unable to find method to access collection"));
                Type valueType = valueAtMethod.getGenericReturnType();
                String identifier = nameResolver.resolveUniqueName(valueType);
                initializers.add(CodeValues.declaration(valueType, identifier,
                                                        CodeValues.methodCall(valueAtMethod, expressionResult.value(),
                                                                              keyResult.value())));
                yield new ExpressionResult(valueType, CodeValues.variable(identifier), initializers);
            }
            case BindExpression.Add(BindExpression left, BindExpression right) ->
                    computeExpressionWithMethod(left, right, "add", "concat");
            case BindExpression.Subtract(BindExpression left, BindExpression right) ->
                    computeExpressionWithMethod(left, right, "subtract");
            case BindExpression.Multiply(BindExpression left, BindExpression right) ->
                    computeExpressionWithMethod(left, right, "multiply");
            case BindExpression.Divide(BindExpression left, BindExpression right) ->
                    computeExpressionWithMethod(left, right, "divide");
            case BindExpression.GreaterThan(BindExpression left, BindExpression right) ->
                    computeExpressionWithMethod(left, right, "greaterThan");
            case BindExpression.GreaterThanEqual(BindExpression left, BindExpression right) ->
                    computeExpressionWithMethod(left, right, "greaterThanOrEqualTo", "greaterThanOrEqual");
            case BindExpression.LessThan(BindExpression left, BindExpression right) ->
                    computeExpressionWithMethod(left, right, "lessThan");
            case BindExpression.LessThanEqual(BindExpression left, BindExpression right) ->
                    computeExpressionWithMethod(left, right, "lessThanOrEqualTo", "lessThanOrEqual");
            case BindExpression.Equal(BindExpression left, BindExpression right) ->
                    computeExpressionWithMethod(left, right, "isEqualTo", "equal");
            case BindExpression.NotEqual(BindExpression left, BindExpression right) ->
                    computeExpressionWithMethod(left, right, "isNotEqualTo", "notEqual");
            case BindExpression.And(BindExpression left, BindExpression right) ->
                    computeExpressionWithMethod(left, right, "and");
            case BindExpression.Or(BindExpression left, BindExpression right) ->
                    computeExpressionWithMethod(left, right, "or");
            case BindExpression.Invert(BindExpression bindExpression) ->
                    computeExpressionWithMethod(bindExpression, "not");
            case BindExpression.Negate(BindExpression bindExpression) ->
                    computeExpressionWithMethod(bindExpression, "negate");
            case BindExpression.Modulo ignored ->
                    throw new UnsupportedOperationException("Modulo operation in expression not supported");
        };
    }

    private ExpressionResult computeExpressionWithMethod(BindExpression left, BindExpression right,
                                                         String... methodNames) {
        ExpressionResult leftResult = resolveExpression(left);
        ExpressionResult rightResult = resolveExpression(right);
        List<Statement.Declaration> initializers = new ArrayList<>();
        initializers.addAll(leftResult.initializers());
        initializers.addAll(rightResult.initializers());

        Method directMethod = Arrays.stream(methodNames)
                                    .map(methodName -> methodResolver.findMethod(leftResult.type(), methodName,
                                                                                 rightResult.type()))
                                    .flatMap(Optional::stream)
                                    .findFirst()
                                    .orElse(null);
        if (directMethod != null) {
            Type valueType = directMethod.getGenericReturnType();
            String identifier = nameResolver.resolveUniqueName(valueType);
            initializers.add(CodeValues.declaration(valueType, identifier,
                                                    CodeValues.methodCall(leftResult.value(), directMethod,
                                                                          rightResult.value())));
            return new ExpressionResult(valueType, CodeValues.variable(identifier), initializers);
        }

        Class<?> bindingsClass = typeResolver.resolve(BINDINGS_CLASS_NAME);
        Method indirectMethod = Arrays.stream(methodNames)
                                      .map(methodName -> methodResolver.findMethod(bindingsClass, methodName,
                                                                                   leftResult.type(),
                                                                                   rightResult.type()))
                                      .flatMap(Optional::stream)
                                      .findFirst()
                                      .orElse(null);
        if (indirectMethod != null) {
            Type valueType = indirectMethod.getGenericReturnType();
            String identifier = nameResolver.resolveUniqueName(valueType);
            initializers.add(CodeValues.declaration(valueType, identifier,
                                                    CodeValues.methodCall(indirectMethod, leftResult.value(),
                                                                          rightResult.value())));
            return new ExpressionResult(valueType, CodeValues.variable(identifier), initializers);
        }

        throw new IllegalArgumentException(
                "Cannot %s %s and %s".formatted(String.join(" or ", methodNames), left, right));
    }

    private ExpressionResult computeExpressionWithMethod(BindExpression value, String methodName) {
        ExpressionResult result = resolveExpression(value);
        List<Statement.Declaration> initializers = new ArrayList<>(result.initializers());

        Method directMethod = methodResolver.findMethod(result.type(), methodName).orElse(null);
        if (directMethod != null) {
            Type valueType = directMethod.getGenericReturnType();
            String identifier = nameResolver.resolveUniqueName(valueType);
            initializers.add(
                    CodeValues.declaration(valueType, identifier, CodeValues.methodCall(result.value(), directMethod)));
            return new ExpressionResult(valueType, CodeValues.variable(identifier), initializers);
        }

        Class<?> bindingsClass = typeResolver.resolve(BINDINGS_CLASS_NAME);
        Method indirectMethod = methodResolver.findMethod(bindingsClass, methodName, result.type()).orElse(null);
        if (indirectMethod != null) {
            Type valueType = indirectMethod.getGenericReturnType();
            String identifier = nameResolver.resolveUniqueName(valueType);
            initializers.add(CodeValues.declaration(valueType, identifier,
                                                    CodeValues.methodCall(indirectMethod, result.value())));
            return new ExpressionResult(valueType, CodeValues.variable(identifier), initializers);
        }

        throw new IllegalArgumentException("Cannot %s %s".formatted(methodName, value));
    }
}
