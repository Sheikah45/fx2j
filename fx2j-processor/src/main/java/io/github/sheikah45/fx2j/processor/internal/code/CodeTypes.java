package io.github.sheikah45.fx2j.processor.internal.code;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;

public final class CodeTypes {

    private CodeTypes() {}

    public static CodeType.Raw.Array arrayOf(Class<?> clazz) {
        return arrayOf(of(clazz));
    }

    public static CodeType.Raw.Array arrayOf(ParameterizedType type) {
        return arrayOf(of(type));
    }

    public static CodeType.Raw.Array arrayOf(CodeType.Declarable type) {
        return new CodeType.Raw.Array(type);
    }

    public static CodeType.Raw.TopLevel of(String className) {
        int index = className.lastIndexOf(".");
        return new CodeType.Raw.TopLevel(className.substring(0, index), className.substring(index + 1));
    }

    public static CodeType.Raw of(Class<?> clazz) {
        if (clazz.isArray()) {
            return arrayOf(clazz.componentType());
        } else if (clazz.isPrimitive()) {
            return new CodeType.Raw.Primitive(clazz.getSimpleName());
        } else if (clazz.getNestHost() != clazz) {
            return new CodeType.Raw.Nested(of(clazz.getNestHost()), clazz.getSimpleName());
        } else {
            return new CodeType.Raw.TopLevel(clazz.getPackageName(), clazz.getSimpleName());
        }
    }

    public static CodeType.Parameterized of(ParameterizedType parameterizedType) {
        if (!(of(parameterizedType.getRawType()) instanceof CodeType.Raw raw)) {
            throw new IllegalArgumentException("Parameterized type %s raw type not raw".formatted(parameterizedType));
        }

        List<CodeType> typeArguments = Arrays.stream(parameterizedType.getActualTypeArguments())
                                             .map(CodeTypes::of)
                                             .toList();

        return new CodeType.Parameterized(raw, typeArguments);
    }

    public static CodeType.Variable of(TypeVariable<?> typeVariable) {
        return new CodeType.Variable(typeVariable.getName(),
                                     Arrays.stream(typeVariable.getBounds()).map(CodeTypes::of).toList());
    }

    public static CodeType.Wildcard of(WildcardType wildcardType) {
        return new CodeType.Wildcard(Arrays.stream(wildcardType.getLowerBounds()).map(CodeTypes::of).toList(),
                                     Arrays.stream(wildcardType.getUpperBounds()).map(CodeTypes::of).toList());
    }

    public static CodeType of(Type type) {
        return switch (type) {
            case Class<?> clazz when clazz.isArray() -> CodeTypes.of(clazz);
            case Class<?> clazz -> CodeTypes.of(clazz);
            case ParameterizedType parameterizedType -> of(parameterizedType);
            case WildcardType wildcardType -> of(wildcardType);
            case TypeVariable<?> typeVariable -> of(typeVariable);
            default -> throw new UnsupportedOperationException("Cannot create CodeType from %s".formatted(type));
        };
    }

}
