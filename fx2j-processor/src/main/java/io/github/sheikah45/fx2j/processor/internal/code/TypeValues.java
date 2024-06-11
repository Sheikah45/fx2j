package io.github.sheikah45.fx2j.processor.internal.code;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;

public final class TypeValues {

    private TypeValues() {}

    public static TypeValue.Raw.Array arrayOf(Class<?> clazz) {
        return arrayOf(of(clazz));
    }

    public static TypeValue.Raw.Array arrayOf(ParameterizedType type) {
        return arrayOf(of(type));
    }

    public static TypeValue.Raw.Array arrayOf(TypeValue.Declarable type) {
        return new TypeValue.Raw.Array(type);
    }

    public static TypeValue.Raw.Top of(String className) {
        int index = className.lastIndexOf(".");
        return new TypeValue.Raw.Top(className.substring(0, index), className.substring(index + 1));
    }

    public static TypeValue.Raw of(Class<?> clazz) {
        if (clazz.isArray()) {
            return arrayOf(clazz.componentType());
        } else if (clazz.isPrimitive()) {
            return new TypeValue.Raw.Primitive(clazz.getSimpleName());
        } else if (clazz.getNestHost() != clazz) {
            return new TypeValue.Raw.Nested(of(clazz.getNestHost()), clazz.getSimpleName());
        } else {
            return new TypeValue.Raw.Top(clazz.getPackageName(), clazz.getSimpleName());
        }
    }

    public static TypeValue.Parameterized of(ParameterizedType parameterizedType) {
        if (!(of(parameterizedType.getRawType()) instanceof TypeValue.Raw raw)) {
            throw new IllegalArgumentException("Parameterized type %s raw type not raw".formatted(parameterizedType));
        }

        List<TypeValue> typeArguments = Arrays.stream(parameterizedType.getActualTypeArguments())
                                              .map(TypeValues::of)
                                              .toList();

        return new TypeValue.Parameterized(raw, typeArguments);
    }

    public static TypeValue.Variable of(TypeVariable<?> typeVariable) {
        return new TypeValue.Variable(typeVariable.getName(),
                                      Arrays.stream(typeVariable.getBounds()).map(TypeValues::of).toList());
    }

    public static TypeValue.Wildcard of(WildcardType wildcardType) {
        return new TypeValue.Wildcard(Arrays.stream(wildcardType.getLowerBounds()).map(TypeValues::of).toList(),
                                      Arrays.stream(wildcardType.getUpperBounds()).map(TypeValues::of).toList());
    }

    public static TypeValue of(Type type) {
        return switch (type) {
            case Class<?> clazz when clazz.isArray() -> TypeValues.of(clazz);
            case Class<?> clazz -> TypeValues.of(clazz);
            case ParameterizedType parameterizedType -> of(parameterizedType);
            case WildcardType wildcardType -> of(wildcardType);
            case TypeVariable<?> typeVariable -> of(typeVariable);
            default -> throw new UnsupportedOperationException("Cannot create CodeType from %s".formatted(type));
        };
    }

}
