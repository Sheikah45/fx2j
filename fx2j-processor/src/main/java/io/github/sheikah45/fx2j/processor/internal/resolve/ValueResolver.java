package io.github.sheikah45.fx2j.processor.internal.resolve;

import io.github.sheikah45.fx2j.parser.property.BindExpression;
import io.github.sheikah45.fx2j.parser.property.Value;
import io.github.sheikah45.fx2j.processor.FxmlProcessor;
import io.github.sheikah45.fx2j.processor.internal.code.CodeValues;
import io.github.sheikah45.fx2j.processor.internal.code.Expression;
import io.github.sheikah45.fx2j.processor.internal.code.StatementExpression;
import io.github.sheikah45.fx2j.processor.internal.model.NamedArgValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ValueResolver {
    static final Map<Class<?>, Object> DEFAULTS_MAP = Map.of(byte.class, 0, short.class, 0, int.class, 0, long.class,
                                                             0L, float.class, 0f, double.class, 0d, char.class,
                                                             '\u0000', boolean.class, false);

    private final TypeResolver typeResolver;
    private final MethodResolver methodResolver;
    private final NameResolver nameResolver;

    ValueResolver(TypeResolver typeResolver, MethodResolver methodResolver, NameResolver nameResolver) {
        this.typeResolver = typeResolver;
        this.methodResolver = methodResolver;
        this.nameResolver = nameResolver;
    }

    public Expression coerceDefaultValue(NamedArgValue namedArgValue) {
        String defaultValue = namedArgValue.defaultValue();
        if (defaultValue.isBlank()) {
            Object typeDefault = DEFAULTS_MAP.get(namedArgValue.parameterType());
            if (typeDefault != null) {
                return CodeValues.of(typeDefault);
            }

            return CodeValues.nullValue();
        }
        return resolveCodeValue(namedArgValue.parameterType(), defaultValue);
    }

    public Expression resolveCodeValue(Type valueType, String value) {
        if (typeResolver.isAssignableFrom(char.class, valueType) ||
            typeResolver.isAssignableFrom(Character.class, valueType)) {
            if (value.length() != 1) {
                throw new IllegalArgumentException("Cannot coerce char from non single character string");
            }

            return CodeValues.literal(value.charAt(0));
        }

        if (typeResolver.isAssignableFrom(String.class, valueType)) {
            return CodeValues.literal(value);
        }

        if (typeResolver.isArray(valueType)) {
            Type componentType = typeResolver.getComponentType(valueType);
            Object[] arrayValues = Arrays.stream(value.split(","))
                                         .map(componentString -> resolveCodeValue(componentType,
                                                                                  componentString))
                                         .toArray(Expression[]::new);
            return CodeValues.array(componentType, arrayValues);
        }

        if (typeResolver.isPrimitive(valueType)) {
            Class<?> boxedType = typeResolver.wrapType(valueType);
            Method method = methodResolver.findMethod(boxedType, "parse%s".formatted(boxedType.getSimpleName()),
                                                      String.class).orElse(null);
            if (method != null) {
                try {
                    return resolveCodeValue(method, value);
                } catch (IllegalAccessException | InvocationTargetException ignored) {}
            }
        }

        Class<?> boxedType = typeResolver.wrapType(valueType);
        Method method = methodResolver.findMethod(boxedType, "valueOf", String.class).orElse(null);
        if (method != null) {
            try {
                return resolveCodeValue(method, value);
            } catch (IllegalAccessException | InvocationTargetException ignored) {}
        }

        if (valueType == Object.class) {
            return CodeValues.literal(value);
        }

        throw new UnsupportedOperationException("Cannot create type %s from %s".formatted(valueType, value));
    }

    public Expression resolveCodeValue(Method staticMethod, String valueString)
            throws IllegalAccessException, InvocationTargetException {
        if (!Modifier.isStatic(staticMethod.getModifiers())) {
            throw new IllegalArgumentException("Provided method is not static");
        }

        Type[] parameterTypes = staticMethod.getGenericParameterTypes();
        if (parameterTypes.length != 1 || !typeResolver.isAssignableFrom(String.class, parameterTypes[0])) {
            throw new IllegalArgumentException(
                    "Provided method %s does not accept only one argument of type string".formatted(staticMethod));
        }

        return switch (staticMethod.invoke(null, valueString)) {
            case null -> CodeValues.nullValue();
            case Double number when number == Double.POSITIVE_INFINITY ->
                    CodeValues.fieldAccess(Double.class, "POSITIVE_INFINITY");
            case Double number when number == Double.NEGATIVE_INFINITY ->
                    CodeValues.fieldAccess(Double.class, "NEGATIVE_INFINITY");
            case Double number when Objects.equals(number, Double.NaN) -> CodeValues.fieldAccess(Double.class, "NaN");
            case Float number when number == Float.POSITIVE_INFINITY ->
                    CodeValues.fieldAccess(Float.class, "POSITIVE_INFINITY");
            case Float number when number == Float.NEGATIVE_INFINITY ->
                    CodeValues.fieldAccess(Float.class, "NEGATIVE_INFINITY");
            case Float number when Objects.equals(number, Float.NaN) -> CodeValues.fieldAccess(Float.class, "NaN");
            case Enum<?> enumValue -> CodeValues.enumValue(enumValue);
            case Object val when typeResolver.isPrimitive(val.getClass()) -> CodeValues.of(val);
            default -> CodeValues.methodCall(staticMethod, valueString);
        };
    }

    public Expression resolveCodeValue(Type valueType, Value value) {
        return switch (value) {
            case Value.Empty() -> CodeValues.nullValue();
            case Value.Reference(String reference) -> {
                Type referenceType = nameResolver.resolveTypeById(reference);
                if (!typeResolver.isAssignableFrom(typeResolver.resolveClassFromType(valueType), referenceType)) {
                    throw new IllegalArgumentException("Cannot assign %s to %s".formatted(referenceType, valueType));
                }

                yield CodeValues.variable(reference);
            }
            case Value.Resource(String resource) when valueType == String.class ->
                    new StatementExpression.MethodCall(CodeValues.variable(FxmlProcessor.RESOURCES_NAME), "getString",
                                                       List.of(CodeValues.literal(resource)));
            case Value.Literal(String val) -> resolveCodeValue(valueType, val);
            case Value.Location ignored ->
                    throw new UnsupportedOperationException("Location resolution not yet supported");
            case Value.Resource ignored -> throw new UnsupportedOperationException(
                    "Non string resource types not supported");
            case BindExpression ignored ->
                    throw new UnsupportedOperationException("Cannot resolve an expression to a code value");
        };
    }
}
