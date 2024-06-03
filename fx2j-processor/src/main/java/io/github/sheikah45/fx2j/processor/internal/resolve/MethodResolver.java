package io.github.sheikah45.fx2j.processor.internal.resolve;

import io.github.sheikah45.fx2j.processor.internal.model.NamedArgValue;
import io.github.sheikah45.fx2j.processor.internal.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MethodResolver {

    public static final String DEFAULT_PROPERTY_CLASS = "javafx.beans.DefaultProperty";
    private static final String NAMED_ARG_CLASS = "javafx.beans.NamedArg";

    private final TypeResolver typeResolver;

    private final Map<MethodCacheKey, Optional<Method>> methodCache = new HashMap<>();
    private final Map<FieldCacheKey, Optional<Field>> fieldCache = new HashMap<>();
    private final Map<Type, String> defaultPropertyCache = new HashMap<>();

    MethodResolver(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    public boolean hasCopyConstructor(Type type) {
        try {
            Class<?> clazz = typeResolver.resolveClassFromType(type);
            return Modifier.isPublic(clazz.getConstructor(clazz).getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public boolean hasDefaultConstructor(Type type) {
        try {
            return Modifier.isPublic(typeResolver.resolveClassFromType(type).getConstructor().getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public String resolveDefaultProperty(Type type) {
        return defaultPropertyCache.computeIfAbsent(type, key -> {
            Class<?> clazz = typeResolver.resolveClassFromType(key);
            Class<Annotation> defaultPropertyClass = typeResolver.resolve(DEFAULT_PROPERTY_CLASS);
            Annotation defaultProperty = clazz.getAnnotation(defaultPropertyClass);
            if (defaultProperty == null) {
                return null;
            }

            try {
                Method valueMethod = defaultProperty.getClass().getMethod("value");
                return (String) valueMethod.invoke(defaultProperty);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Optional<Field> resolveField(Type type, String fieldName) {
        return fieldCache.computeIfAbsent(new FieldCacheKey(type, fieldName), this::resolveFieldWithCacheKey);
    }

    private Optional<Field> resolveFieldWithCacheKey(FieldCacheKey fieldCacheKey) {
        return resolvePublicFieldWithCacheKey(fieldCacheKey);
    }

    private Optional<Field> resolvePublicFieldWithCacheKey(FieldCacheKey fieldCacheKey) {
        Class<?> clazz = typeResolver.resolveClassFromType(fieldCacheKey.type());
        String fieldName = fieldCacheKey.fieldName();
        try {
            return Optional.of(clazz.getField(fieldName));
        } catch (NoSuchFieldException ignored) {
            return Optional.empty();
        }
    }

    public Optional<Field> resolveFieldRequiredPublicIfExists(Type type, String fieldName) {
        return fieldCache.computeIfAbsent(new FieldCacheKey(type, fieldName), key -> resolveFieldWithCacheKey(key).or(
                () -> resolveDeclaredFieldWithCacheKey(key).map(field -> {
                    if (!Modifier.isPublic(field.getModifiers())) {
                        throw new IllegalArgumentException("%s is not public in %s".formatted(field, type));
                    }

                    return field;
                })));
    }

    private Optional<Field> resolveDeclaredFieldWithCacheKey(FieldCacheKey fieldCacheKey) {
        Class<?> clazz = typeResolver.resolveClassFromType(fieldCacheKey.type());
        String fieldName = fieldCacheKey.fieldName();
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return Optional.of(field);
        } catch (NoSuchFieldException ignored) {
            return Optional.empty();
        }
    }

    public Optional<Method> findMethod(Type type, String name, Type... parameterTypes) {
        return methodCache.computeIfAbsent(new MethodCacheKey.ParamTypes(type, name, parameterTypes),
                                           this::findMethodWithCacheKey);
    }

    private Optional<Method> findMethodWithCacheKey(MethodCacheKey methodCacheKey) {
        return switch (methodCacheKey) {
            case MethodCacheKey.Count(Type type, String name, int paramCount) -> {
                Method[] methods = typeResolver.resolveClassFromType(type).getMethods();
                yield findMatchingMethod(name, paramCount, methods);
            }
            case MethodCacheKey.ParamTypes(Type type, String name, Type[] parameterTypes) -> {
                Method[] methods = typeResolver.resolveClassFromType(type).getMethods();
                yield findMatchingMethod(name, parameterTypes, methods);
            }
        };
    }

    private static Optional<Method> findMatchingMethod(String name, int paramCount, Method[] methods) {
        List<Method> matchingMethods = Arrays.stream(methods)
                                             .filter(method -> method.getName().equals(name))
                                             .filter(method -> method.getParameterCount() == paramCount)
                                             .filter(method -> !method.isBridge())
                                             .toList();

        if (matchingMethods.size() > 1) {
            throw new IllegalArgumentException(
                    "Multiple matching methods found for name %s and count %d: %s".formatted(name, paramCount,
                                                                                             matchingMethods));
        } else if (matchingMethods.size() == 1) {
            return Optional.of(matchingMethods.getFirst());
        } else {
            return Optional.empty();
        }
    }

    private Optional<Method> findMatchingMethod(String name, Type[] parameterTypes, Method[] methods) {
        int numParameters = parameterTypes.length;
        return Arrays.stream(methods)
                     .filter(method -> method.getName().equals(name))
                     .filter(method -> method.getParameterCount() == numParameters)
                     .filter(method -> !method.isBridge())
                     .filter(method -> {
                         Type[] methodParameterTypes = method.getGenericParameterTypes();
                         for (int i = 0; i < numParameters; i++) {
                             if (!(typeResolver.isAssignableFrom(methodParameterTypes[i], parameterTypes[i]))) {
                                 return false;
                             }
                         }
                         return true;
                     })
                     .findFirst();
    }

    public Optional<Method> findMethod(Type type, String name, int paramCount) {
        return methodCache.computeIfAbsent(new MethodCacheKey.Count(type, name, paramCount),
                                           this::findMethodWithCacheKey);
    }

    public Optional<Method> findMethodRequiredPublicIfExists(Type type, String name, Type... parameterTypes) {
        return methodCache.computeIfAbsent(new MethodCacheKey.ParamTypes(type, name, parameterTypes),
                                           key -> findMethodWithCacheKey(key).or(
                                                   () -> findDeclaredMethodWithCacheKey(key).map(method -> {
                                                       if (!Modifier.isPublic(method.getModifiers())) {
                                                           throw new IllegalArgumentException(
                                                                   "%s is not public in %s".formatted(method, type));
                                                       }

                                                       return method;
                                                   })));
    }

    private Optional<Method> findDeclaredMethodWithCacheKey(MethodCacheKey methodCacheKey) {
        return switch (methodCacheKey) {
            case MethodCacheKey.Count(Type type, String name, int paramCount) -> {
                Method[] methods = typeResolver.resolveClassFromType(type).getDeclaredMethods();
                yield findMatchingMethod(name, paramCount, methods);
            }
            case MethodCacheKey.ParamTypes(Type type, String name, Type[] parameterTypes) -> {
                Method[] methods = typeResolver.resolveClassFromType(type).getDeclaredMethods();
                yield findMatchingMethod(name, parameterTypes, methods);
            }
        };
    }

    public Optional<Method> resolveGetter(Type type, String name) {
        return findMethod(type, "get%s".formatted(StringUtils.capitalize(name)), 0);
    }

    public Optional<Method> resolveProperty(Type type, String name) {
        return findMethod(type, "%sProperty".formatted(StringUtils.camelCase(name)), 0);
    }

    public Optional<Method> resolveSetter(Type type, String name) {
        return findMethod(type, "set%s".formatted(StringUtils.capitalize(name)), 1);
    }

    public Optional<Method> resolveSetter(Type type, String name, Type valueClass) {
        return findMethod(type, "set%s".formatted(StringUtils.capitalize(name)), valueClass);
    }

    public Optional<Method> resolveSetterRequiredPublicIfExists(Type type, String name, Type valueClass) {
        return findMethodRequiredPublicIfExists(type, "set%s".formatted(StringUtils.capitalize(name)), valueClass);
    }

    public Optional<Method> resolveStaticSetter(Type type, String name) {
        return findMethod(type, "set%s".formatted(StringUtils.capitalize(name)), 2);
    }


    public List<NamedArgValue> getNamedArgs(Executable executable) {
        return Arrays.stream(executable.getParameters()).map(this::getNamedArg).toList();
    }

    public NamedArgValue getNamedArg(Parameter parameter) {
        Class<Annotation> namedArgClass = typeResolver.resolve(NAMED_ARG_CLASS);
        Annotation namedArg = parameter.getAnnotation(namedArgClass);
        if (namedArg == null) {
            throw new IllegalArgumentException(
                    "Parameter does not have a NamedArg annotation: %s".formatted(parameter.getDeclaringExecutable()));
        }

        try {
            Method valueMethod = namedArg.getClass().getMethod("value");
            Method defaultValueMethod = namedArg.getClass().getMethod("defaultValue");
            return new NamedArgValue(parameter.getType(), (String) valueMethod.invoke(namedArg),
                                     (String) defaultValueMethod.invoke(namedArg));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasAllNamedArgs(Executable executable) {
        return Arrays.stream(executable.getParameters()).allMatch(parameter -> {
            Class<Annotation> namedArgClass = typeResolver.resolve(NAMED_ARG_CLASS);
            return parameter.getAnnotation(namedArgClass) != null;
        });
    }


    public List<Constructor<?>> getConstructors(Type type) {
        return List.of(typeResolver.resolveClassFromType(type).getConstructors());
    }

    private sealed interface MethodCacheKey {
        record Count(Type receiverType, String methodName, int paramCount) implements MethodCacheKey {}
        record ParamTypes(Type receiverType, String methodName, Type... paramTypes) implements MethodCacheKey {}
    }
    private record FieldCacheKey(Type type, String fieldName) {}
}
