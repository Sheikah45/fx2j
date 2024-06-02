package io.github.sheikah45.fx2j.processor.internal.resolve;

import com.squareup.javapoet.CodeBlock;
import io.github.sheikah45.fx2j.parser.attribute.InstancePropertyAttribute;
import io.github.sheikah45.fx2j.parser.element.ElementContent;
import io.github.sheikah45.fx2j.parser.element.InstancePropertyElement;
import io.github.sheikah45.fx2j.parser.property.Value;
import io.github.sheikah45.fx2j.processor.FxmlProcessor;
import io.github.sheikah45.fx2j.processor.internal.model.NamedArgValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class ValueResolver {
    private static final Map<Class<?>, Object> DEFAULTS_MAP = Map.of(byte.class, (byte) 0, short.class, (short) 0,
                                                                     int.class, 0, long.class, 0L, float.class, 0.0f,
                                                                     double.class, 0.0d, char.class, '\u0000',
                                                                     boolean.class, false);

    private final TypeResolver typeResolver;
    private final MethodResolver methodResolver;

    public ValueResolver(TypeResolver typeResolver, MethodResolver methodResolver) {
        this.typeResolver = typeResolver;
        this.methodResolver = methodResolver;
    }

    public CodeBlock coerceValue(Type valueType, Value value) {
        return switch (value) {
            case Value.Location ignored ->
                    throw new UnsupportedOperationException("Location resolution not yet supported");
            case Value.Reference(String reference) -> {
                Class<?> referenceClass = typeResolver.getStoredClassById(reference);
                if (!typeResolver.isAssignableFrom(typeResolver.resolveClassFromType(valueType), referenceClass)) {
                    throw new IllegalArgumentException("Cannot assign %s to %s".formatted(referenceClass, valueType));
                }

                yield CodeBlock.of("$L", reference);
            }
            case Value.Resource(String resource) when valueType == String.class ->
                    CodeBlock.of("$1L.getString($2S)", FxmlProcessor.RESOURCES_NAME, resource);
            case Value.Literal(String val) -> coerceValue(valueType, val);
            default -> throw new UnsupportedOperationException(
                    "Cannot create type %s from %s".formatted(valueType, value));
        };
    }

    public CodeBlock coerceValue(Type valueType, String value) {
        Class<?> rawType = typeResolver.resolveClassFromType(valueType);
        if (typeResolver.isAssignableFrom(String.class, valueType)) {
            return CodeBlock.of("$S", value);
        }

        if (typeResolver.isArray(valueType)) {
            Type componentType = typeResolver.getComponentType(valueType);
            CodeBlock arrayInitializer = Arrays.stream(value.split(","))
                                               .map(componentString -> coerceValue(componentType, componentString))
                                               .collect(CodeBlock.joining(", "));
            return CodeBlock.of("new $T{$L}", valueType, arrayInitializer);
        }

        if (typeResolver.isPrimitive(valueType)) {
            Class<?> boxedType = typeResolver.wrapType(valueType);
            Method method = methodResolver.findMethod(boxedType, "parse%s".formatted(boxedType.getSimpleName()),
                                                      String.class)
                                          .orElse(null);
            if (method != null) {
                try {
                    return coerceUsingMethodResults(value, method, boxedType);
                } catch (IllegalAccessException | InvocationTargetException ignored) {}
            }
        }

        Class<?> boxedType = typeResolver.wrapType(rawType);
        Method method = methodResolver.findMethod(boxedType, "valueOf", String.class).orElse(null);
        if (method != null) {
            try {
                return coerceUsingMethodResults(value, method, boxedType);
            } catch (IllegalAccessException | InvocationTargetException ignored) {}
        }

        if (valueType == Object.class) {
            return CodeBlock.of("$S", value);
        }

        throw new UnsupportedOperationException("Cannot create type %s from %s".formatted(valueType, value));
    }

    public CodeBlock coerceUsingMethodResults(String valueString, Method valueOfMethod, Type valueType)
            throws IllegalAccessException, InvocationTargetException {
        if (typeResolver.isPrimitive(valueType)) {
            Object value = valueOfMethod.invoke(null, valueString);
            if (Objects.equals(value, Double.POSITIVE_INFINITY) || Objects.equals(value, Float.POSITIVE_INFINITY)) {
                return CodeBlock.of("$T.POSITIVE_INFINITY", valueType);
            }

            if (Objects.equals(value, Double.NEGATIVE_INFINITY) || Objects.equals(value, Float.NEGATIVE_INFINITY)) {
                return CodeBlock.of("$T.NEGATIVE_INFINITY", valueType);
            }

            if (Objects.equals(value, Double.NaN) || Objects.equals(value, Float.NaN)) {
                return CodeBlock.of("$T.NaN", valueType);
            }

            return CodeBlock.of("$L", value);
        }

        if (typeResolver.isAssignableFrom(Enum.class, valueType)) {
            Object value = valueOfMethod.invoke(null, valueString);
            return CodeBlock.of("$T.$L", valueType, value);
        }

        return CodeBlock.of("$T.$L($S)", valueType, valueOfMethod.getName(), valueString);
    }

    public Value extractSingleValue(ElementContent<?, ?> content) {
        boolean multiSource = Stream.of(!content.attributes().isEmpty(), !content.elements().isEmpty(),
                                        !(content.value() instanceof Value.Empty))
                                    .filter(Boolean::booleanValue)
                                    .count() > 1;
        if (multiSource) {
            throw new UnsupportedOperationException("Cannot resolve parameter value from multiple sources");
        }

        if (!content.attributes().isEmpty()) {
            if (content.attributes().size() > 1) {
                throw new UnsupportedOperationException("Cannot resolve parameter value from multiple attributes");
            }

            if (!(content.attributes().getFirst() instanceof InstancePropertyAttribute(
                    String ignoredName, Value value
            ))) {
                throw new UnsupportedOperationException("Cannot resolve parameter value from non instance value");
            }

            return value;
        }

        if (!content.elements().isEmpty()) {
            if (content.elements().size() > 1) {
                throw new UnsupportedOperationException("Cannot resolve parameter value from multiple elements");
            }

            if (!(content.elements().getFirst() instanceof InstancePropertyElement(
                    String ignoredName, ElementContent<?, ?> innerContent
            ))) {
                throw new UnsupportedOperationException(
                        "Cannot resolve parameter value from non instance property element");
            }

            return extractSingleValue(innerContent);
        }

        return content.value();
    }

    public CodeBlock coerceDefaultValue(NamedArgValue namedArgValue) {
        String defaultValue = namedArgValue.defaultValue();
        if (defaultValue.isBlank()) {
            return CodeBlock.of("$L", DEFAULTS_MAP.get(namedArgValue.parameterType()));
        }
        return coerceValue(namedArgValue.parameterType(), defaultValue);
    }
}
