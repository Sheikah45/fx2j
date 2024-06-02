package io.github.sheikah45.fx2j.processor.internal.resolve;

import com.squareup.javapoet.CodeBlock;
import io.github.sheikah45.fx2j.parser.property.Expression;
import io.github.sheikah45.fx2j.processor.internal.model.ExpressionResult;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ExpressionResolver {

    private final TypeResolver typeResolver;
    private final MethodResolver methodResolver;

    public ExpressionResolver(TypeResolver typeResolver, MethodResolver methodResolver) {
        this.typeResolver = typeResolver;
        this.methodResolver = methodResolver;
    }

    public ExpressionResult resolveExpression(Expression value) {
        CodeBlock.Builder expressionBlockBuilder = CodeBlock.builder();
        return switch (value) {
            case Expression.Null() -> new ExpressionResult(Object.class, null, expressionBlockBuilder.build());
            case Expression.Whole(long val) -> {
                String identifier = typeResolver.getDeconflictedName(long.class);
                expressionBlockBuilder.addStatement("$T $L = $L", long.class, identifier, val);
                yield new ExpressionResult(long.class, identifier, expressionBlockBuilder.build());
            }
            case Expression.Fraction(double val) -> {
                String identifier = typeResolver.getDeconflictedName(double.class);
                expressionBlockBuilder.addStatement("$T $L = $L", double.class, identifier, val);
                yield new ExpressionResult(double.class, identifier, expressionBlockBuilder.build());
            }
            case Expression.Boolean(boolean val) -> {
                String identifier = typeResolver.getDeconflictedName(boolean.class);
                expressionBlockBuilder.addStatement("$T $L = $L", boolean.class, identifier, val);
                yield new ExpressionResult(boolean.class, identifier, expressionBlockBuilder.build());
            }
            case Expression.Str(String val) -> {
                String identifier = typeResolver.getDeconflictedName(String.class);
                expressionBlockBuilder.addStatement("$T $L = $L", String.class, identifier, val);
                yield new ExpressionResult(String.class, identifier, expressionBlockBuilder.build());
            }
            case Expression.Variable(String name) ->
                    new ExpressionResult(typeResolver.getStoredTypeById(name), name, CodeBlock.of(""));
            case Expression.PropertyRead(Expression expression, String property) -> {
                ExpressionResult expressionResult = resolveExpression(expression);
                expressionBlockBuilder.add(expressionResult.initializationCode());

                Method readProperty = methodResolver.resolveProperty(expressionResult.type(), property)
                                                    .orElseThrow(() -> new IllegalArgumentException(
                                                            "No property found for expression binding %s".formatted(
                                                                    property)));
                Type valueType = readProperty.getGenericReturnType();
                String identifier = typeResolver.getDeconflictedName(valueType);

                expressionBlockBuilder.addStatement("$T $L = $L.$L()", valueType, identifier,
                                                    expressionResult.identifier(), readProperty.getName());
                yield new ExpressionResult(valueType, identifier, expressionBlockBuilder.build());
            }
            case Expression.MethodCall(
                    Expression expression, String methodName, List<Expression> args
            ) -> {
                ExpressionResult expressionResult = resolveExpression(expression);
                expressionBlockBuilder.add(expressionResult.initializationCode());
                List<Type> parameterTypes = new ArrayList<>();
                List<CodeBlock> methodArgs = new ArrayList<>();
                for (Expression arg : args) {
                    ExpressionResult argResult = resolveExpression(arg);
                    parameterTypes.add(argResult.type());
                    methodArgs.add(CodeBlock.of("$S", argResult.identifier()));
                    expressionBlockBuilder.add(argResult.initializationCode());
                }

                Method method = methodResolver.findMethod(expressionResult.type(), methodName,
                                                          parameterTypes.toArray(Type[]::new))
                                              .orElseThrow(() -> new IllegalArgumentException(
                                                      "No method found for class %s method name %s and parameters %s".formatted(
                                                              expressionResult.type(), methodName, parameterTypes)));
                Type valueType = method.getGenericReturnType();
                String identifier = typeResolver.getDeconflictedName(valueType);

                expressionBlockBuilder.addStatement("$T $L = $L.$L($L)", valueType, identifier,
                                                    expressionResult.identifier(), method.getName(),
                                                    CodeBlock.join(methodArgs, ", "));
                yield new ExpressionResult(valueType, identifier, expressionBlockBuilder.build());
            }
            case Expression.CollectionAccess(Expression expression, Expression key) -> {
                ExpressionResult expressionResult = resolveExpression(expression);
                expressionBlockBuilder.add(expressionResult.initializationCode());
                ExpressionResult keyResult = resolveExpression(key);
                expressionBlockBuilder.add(expressionResult.initializationCode());
                Class<?> bindingsClass = typeResolver.resolve("javafx.beans.binding.Bindings");
                Method valueAtMethod = methodResolver.findMethod(bindingsClass, "valueAt", expressionResult.type(),
                                                                 keyResult.type())
                                                     .orElseThrow(() -> new IllegalArgumentException(
                                                             "Unable to find method to access collection"));
                Type valueType = valueAtMethod.getGenericReturnType();
                String identifier = typeResolver.getDeconflictedName(valueType);
                expressionBlockBuilder.addStatement("$T $L = $T.$L($L, $L)", valueType, identifier, bindingsClass,
                                                    valueAtMethod.getName(), expressionResult.identifier(),
                                                    keyResult.identifier());
                yield new ExpressionResult(valueType, identifier, expressionBlockBuilder.build());
            }
            case Expression.Add(Expression left, Expression right) ->
                    computeExpressionWithMethod(expressionBlockBuilder, left, right, "add", "concat");
            case Expression.Subtract(Expression left, Expression right) ->
                    computeExpressionWithMethod(expressionBlockBuilder, left, right, "subtract");
            case Expression.Multiply(Expression left, Expression right) ->
                    computeExpressionWithMethod(expressionBlockBuilder, left, right, "multiply");
            case Expression.Divide(Expression left, Expression right) ->
                    computeExpressionWithMethod(expressionBlockBuilder, left, right, "divide");
            case Expression.GreaterThan(Expression left, Expression right) ->
                    computeExpressionWithMethod(expressionBlockBuilder, left, right, "greaterThan");
            case Expression.GreaterThanEqual(Expression left, Expression right) ->
                    computeExpressionWithMethod(expressionBlockBuilder, left, right, "greaterThanEqual");
            case Expression.LessThan(Expression left, Expression right) ->
                    computeExpressionWithMethod(expressionBlockBuilder, left, right, "lessThan");
            case Expression.LessThanEqual(Expression left, Expression right) ->
                    computeExpressionWithMethod(expressionBlockBuilder, left, right, "lessThanEqual");
            case Expression.Equal(Expression left, Expression right) ->
                    computeExpressionWithMethod(expressionBlockBuilder, left, right, "equal");
            case Expression.NotEqual(Expression left, Expression right) ->
                    computeExpressionWithMethod(expressionBlockBuilder, left, right, "notEqual");
            case Expression.And(Expression left, Expression right) ->
                    computeExpressionWithMethod(expressionBlockBuilder, left, right, "and");
            case Expression.Or(Expression left, Expression right) ->
                    computeExpressionWithMethod(expressionBlockBuilder, left, right, "or");
            case Expression.Invert(Expression expression) ->
                    computeExpressionWithMethod(expressionBlockBuilder, expression, "not");
            case Expression.Negate(Expression expression) ->
                    computeExpressionWithMethod(expressionBlockBuilder, expression, "negate");
            case Expression.Modulo ignored ->
                    throw new UnsupportedOperationException("Modulo operation in expression not supported");
        };
    }

    private ExpressionResult computeExpressionWithMethod(CodeBlock.Builder expressionBlockBuilder, Expression left,
                                                         Expression right, String... methodNames) {
        ExpressionResult leftResult = resolveExpression(left);
        expressionBlockBuilder.add(leftResult.initializationCode());

        ExpressionResult rightResult = resolveExpression(right);
        expressionBlockBuilder.add(rightResult.initializationCode());

        Method directMethod = Arrays.stream(methodNames)
                                    .map(methodName -> methodResolver.findMethod(leftResult.type(), methodName,
                                                                                 rightResult.type()))
                                    .flatMap(Optional::stream)
                                    .findFirst()
                                    .orElse(null);
        if (directMethod != null) {
            Type valueType = directMethod.getGenericReturnType();
            String identifier = typeResolver.getDeconflictedName(valueType);
            expressionBlockBuilder.addStatement("$T $L = $L.$L($L)", valueType, identifier, leftResult.identifier(),
                                                directMethod.getName(), rightResult.identifier());
            return new ExpressionResult(valueType, identifier, expressionBlockBuilder.build());
        }

        Class<?> bindingsClass = typeResolver.resolve("javafx.beans.binding.Bindings");
        Method indirectMethod = Arrays.stream(methodNames)
                                      .map(methodName -> methodResolver.findMethod(bindingsClass, methodName,
                                                                                   leftResult.type(),
                                                                                   rightResult.type()))
                                      .flatMap(Optional::stream)
                                      .findFirst()
                                      .orElse(null);
        if (indirectMethod != null) {
            Type valueType = indirectMethod.getGenericReturnType();
            String identifier = typeResolver.getDeconflictedName(valueType);
            expressionBlockBuilder.addStatement("$T $L = $T.$L($L, $L)", valueType, identifier, bindingsClass,
                                                indirectMethod.getName(), leftResult.identifier(),
                                                rightResult.identifier());
            return new ExpressionResult(valueType, identifier, expressionBlockBuilder.build());
        }

        throw new IllegalArgumentException(
                "Cannot %s %s and %s".formatted(String.join(" or ", methodNames), left, right));
    }

    private ExpressionResult computeExpressionWithMethod(CodeBlock.Builder expressionBlockBuilder, Expression value,
                                                         String methodName) {
        ExpressionResult result = resolveExpression(value);

        Method directMethod = methodResolver.findMethod(result.type(), methodName).orElse(null);
        if (directMethod != null) {
            Type valueType = directMethod.getGenericReturnType();
            String identifier = typeResolver.getDeconflictedName(valueType);
            expressionBlockBuilder.addStatement("$T $L = $L.$L()", valueType, identifier, result.identifier(),
                                                directMethod.getName());
            return new ExpressionResult(valueType, identifier, expressionBlockBuilder.build());
        }

        Class<?> bindingsClass = typeResolver.resolve("javafx.beans.binding.Bindings");
        Method indirectMethod = methodResolver.findMethod(bindingsClass, methodName, result.type()).orElse(null);
        if (indirectMethod != null) {
            Type valueType = indirectMethod.getGenericReturnType();
            String identifier = typeResolver.getDeconflictedName(valueType);
            expressionBlockBuilder.addStatement("$T $L = $T.$L($L)", valueType, identifier, bindingsClass,
                                                indirectMethod.getName(), result.identifier());
            return new ExpressionResult(valueType, identifier, expressionBlockBuilder.build());
        }

        throw new IllegalArgumentException("Cannot %s %s".formatted(methodName, value));
    }
}
