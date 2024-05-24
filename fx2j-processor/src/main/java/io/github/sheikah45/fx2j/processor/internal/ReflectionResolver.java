package io.github.sheikah45.fx2j.processor.internal;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;
import io.github.sheikah45.fx2j.processor.internal.model.NamedArgValue;
import io.github.sheikah45.fx2j.processor.internal.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class ReflectionResolver {

    private static final Set<Class<?>> ALLOWED_LITERALS = Set.of(Byte.class, Short.class, Integer.class, Long.class,
                                                                 Character.class, Float.class, Double.class,
                                                                 Boolean.class);

    public static final String NAMED_ARG_CLASS = "javafx.beans.NamedArg";
    public static final String DEFAULT_PROPERTY_CLASS = "javafx.beans.DefaultProperty";
    private final ClassLoader classLoader;

    private final Map<String, Integer> nameCounts = new HashMap<>();
    private final Map<String, Type> idTypeMap = new HashMap<>();
    private final Set<String> importPrefixes = new HashSet<>();
    private final Map<String, Class<?>> resolvedClassesMap = new HashMap<>();

    public ReflectionResolver(Set<String> imports, ClassLoader classLoader) {
        this.classLoader = classLoader;
        resolveImports(imports);
    }

    public boolean isResolvable(String typeName) {
        return resolve(typeName) != null;
    }

    public Class<?> checkResolved(Type type) {
        Class<?> clazz = extractClass(type);
        Class<?> previousValue = resolvedClassesMap.put(clazz.getCanonicalName(), clazz);
        if (previousValue != null && previousValue != clazz) {
            throw new IllegalArgumentException("Class canonical name already resolved with a different class");
        }
        return clazz;
    }

    public Class<?> resolveRequired(String typeName) {
        Class<?> clazz = resolve(typeName);
        if (clazz == null) {
            throw new IllegalArgumentException("Unable to find class for %s".formatted(typeName));
        }

        return clazz;
    }

    private Class<?> resolve(String typeName) {
        return resolvedClassesMap.computeIfAbsent(typeName, this::resolveInternal);
    }

    private Class<?> resolveInternal(String typeName) {
        try {
            return Class.forName(typeName, false, classLoader);
        } catch (ClassNotFoundException ignored) {
        }

        for (String importPrefix : importPrefixes) {
            String fullName = importPrefix + "." + typeName;
            try {
                return Class.forName(fullName, false, classLoader);
            } catch (ClassNotFoundException ignored) {
            }
        }

        String subclassName = typeName;
        while (subclassName.contains(".")) {
            subclassName = StringUtils.replaceLast(subclassName, ".", "$");
            Class<?> clazz = resolveInternal(subclassName);
            if (clazz != null) {
                return clazz;
            }
        }

        return null;
    }

    private void resolveImports(Set<String> imports) {
        Map<Boolean, List<String>> splitImportsMap = imports.stream()
                                                            .collect(Collectors.partitioningBy(
                                                                    importString -> importString.endsWith(".*")));

        splitImportsMap.getOrDefault(true, List.of())
                       .stream()
                       .map(importString -> importString.substring(0, importString.length() - 2))
                       .forEach(importPrefixes::add);

        splitImportsMap.getOrDefault(false, List.of()).forEach(importString -> {
            Class<?> type = resolveRequired(importString);
            String simpleName = StringUtils.substringAfterLast(importString, ".");
            resolvedClassesMap.put(simpleName, type);
        });
    }

    public boolean hasCopyConstructor(Type type) {
        try {
            Class<?> clazz = extractClass(type);
            return Modifier.isPublic(clazz.getConstructor(clazz).getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public boolean hasDefaultConstructor(Type type) {
        try {
            return Modifier.isPublic(extractClass(type).getConstructor().getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public Optional<Class<?>> resolveFieldTypeRequiredPublic(Type type, String fieldName) {
        return resolveFieldRequiredPublic(type, fieldName).map(Field::getType);
    }

    public Optional<Field> resolveField(Type type, String fieldName) {
        try {
            return Optional.of(extractClass(type).getField(fieldName));
        } catch (NoSuchFieldException e) {
            return Optional.empty();
        }
    }

    public Optional<Field> resolveFieldRequiredPublic(Type type, String fieldName) {
        Class<?> clazz = extractClass(type);
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (!Modifier.isPublic(field.getModifiers())) {
                throw new IllegalArgumentException("%s is not public from %s".formatted(field, clazz));
            }

            return Optional.of(field);
        } catch (NoSuchFieldException ignored) {}

        try {
            return Optional.of(clazz.getField(fieldName));
        } catch (NoSuchFieldException e) {
            return Optional.empty();
        }
    }

    public Class<?> resolveClassFromType(Type type) {
        return switch (type) {
            case Class<?> clazz -> clazz;
            case ParameterizedType parameterizedType -> resolveClassFromType(parameterizedType.getRawType());
            case WildcardType wildcardType -> {
                Type[] upperBounds = wildcardType.getUpperBounds();
                if (upperBounds.length != 1) {
                    throw new IllegalArgumentException("Type does not have exactly one upper bound");
                }
                yield resolveClassFromType(upperBounds[0]);
            }
            case null, default ->
                    throw new UnsupportedOperationException("Unable to get class from type %s".formatted(type));
        };
    }

    public Class<?>[] resolveUpperBoundTypeArguments(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            return Arrays.stream(parameterizedType.getActualTypeArguments())
                         .map(this::resolveTypeUpperBound)
                         .toArray(Class[]::new);
        } else {
            return null;
        }
    }

    public Class<?> resolveTypeUpperBound(Type type) {
        return switch (type) {
            case Class<?> clazz -> clazz;
            case ParameterizedType parameterizedType -> resolveTypeUpperBound(parameterizedType.getRawType());
            case WildcardType wildcardType -> {
                Type[] upperBounds = wildcardType.getUpperBounds();
                if (upperBounds.length != 1) {
                    throw new IllegalArgumentException("Type does not have exactly one upper bound");
                }
                yield resolveTypeUpperBound(upperBounds[0]);
            }
            case null, default ->
                    throw new UnsupportedOperationException("Cannot resolve upper bound of type %s".formatted(type));
        };
    }

    public Class<?>[] resolveLowerBoundTypeArguments(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            return Arrays.stream(parameterizedType.getActualTypeArguments())
                         .map(this::resolveTypeLowerBound)
                         .toArray(Class[]::new);
        } else {
            return null;
        }
    }

    public Class<?> resolveTypeLowerBound(Type type) {
        return switch (type) {
            case Class<?> clazz -> clazz;
            case ParameterizedType parameterizedType -> resolveTypeLowerBound(parameterizedType.getRawType());
            case WildcardType wildcardType -> {
                Type[] lowerBounds = wildcardType.getLowerBounds();
                if (lowerBounds.length != 1) {
                    throw new IllegalArgumentException("Type does not have exactly one lower bound");
                }
                yield resolveTypeUpperBound(lowerBounds[0]);
            }
            case null, default ->
                    throw new UnsupportedOperationException("Cannot resolve lower bound of type %s".formatted(type));
        };
    }

    public boolean parameterTypeArgumentsMeetBounds(Type parameterType, Class<?>[] boundTypeArguments) {
        if (!(parameterType instanceof ParameterizedType parameterizedType)) {
            return true;
        }

        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length != boundTypeArguments.length) {
            return false;
        }

        for (int i = 0; i < actualTypeArguments.length; i++) {
            Type typeArgument = actualTypeArguments[i];
            Class<?> wildcardBound = boundTypeArguments[i];
            if (typeArgument instanceof ParameterizedType parameterizedTypeArgument &&
                hasNonMatchingWildcardUpperBounds(parameterizedTypeArgument, wildcardBound)) {
                return false;
            }
        }

        return true;
    }

    public boolean hasNonMatchingWildcardUpperBounds(ParameterizedType parameterizedType, Type desiredUpperBound) {
        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
        if (!(actualTypeArgument instanceof WildcardType wildcardType)) {
            return true;
        }

        Type[] upperBounds = wildcardType.getUpperBounds();
        if (upperBounds.length != 1) {
            return true;
        }

        Type upperBound = upperBounds[0];
        return upperBound != desiredUpperBound;
    }

    public Optional<Method> findMethod(Type type, String name, Type... parameterTypes) {
        int numParameters = parameterTypes.length;
        return Arrays.stream(extractClass(type).getMethods())
                     .filter(method -> method.getName().equals(name))
                     .filter(method -> method.getParameterCount() == numParameters)
                     .filter(method -> {
                         Map<String, TypeVariable<? extends GenericDeclaration>> typeParameters = new HashMap<>();
                         Arrays.stream(method.getDeclaringClass().getTypeParameters())
                               .forEach(typeVariable -> typeParameters.put(typeVariable.getName(), typeVariable));
                         Arrays.stream(method.getTypeParameters())
                               .forEach(typeVariable -> typeParameters.put(typeVariable.getName(), typeVariable));
                         Type[] methodParameterTypes = method.getGenericParameterTypes();
                         for (int i = 0; i < numParameters; i++) {
                             if (!(isAssignableFrom(methodParameterTypes[i], parameterTypes[i]))) {
                                 return false;
                             }
                         }
                         return true;
                     })
                     .findFirst();
    }

    public Optional<Method> resolveGetter(Type type, String name) {
        return findMethod(type, "get%s".formatted(StringUtils.capitalize(name)), 0);
    }

    public Optional<Method> findMethod(Type type, String name, int paramCount) {
        return Arrays.stream(extractClass(type).getMethods())
                     .filter(method -> method.getName().equals(name))
                     .filter(method -> method.getParameterCount() == paramCount)
                     .findFirst();
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

    public Optional<Method> findMethodRequiredPublicIfExists(Type type, String name, Type... parameterTypes) {
        Class<?> clazz = extractClass(type);
        Class<?>[] parameterClasses = Arrays.stream(parameterTypes).map(this::extractClass).toArray(Class[]::new);
        try {
            Method method = clazz.getDeclaredMethod(name, parameterClasses);
            if (!Modifier.isPublic(method.getModifiers())) {
                throw new IllegalArgumentException("%s is not public from %s".formatted(method, clazz));
            }

            return Optional.of(method);
        } catch (NoSuchMethodException ignored) {}

        try {
            return Optional.of(clazz.getMethod(name, parameterClasses));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    public Optional<Method> resolveStaticSetter(Type type, String name) {
        return findMethod(type, "set%s".formatted(StringUtils.capitalize(name)), 2);
    }

    public String getDefaultProperty(Type type) {
        Class<?> clazz = extractClass(type);
        Class<Annotation> defaultPropertyClass = (Class<Annotation>) resolveRequired(DEFAULT_PROPERTY_CLASS);
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
    }

    public List<NamedArgValue> getNamedArgs(Executable executable) {
        return Arrays.stream(executable.getParameters()).map(this::getNamedArg).toList();
    }

    public NamedArgValue getNamedArg(Parameter parameter) {
        Class<Annotation> namedArgClass = (Class<Annotation>) resolveRequired(NAMED_ARG_CLASS);
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

    public boolean hasAllNamedArgs(Constructor<?> constructor) {
        return Arrays.stream(constructor.getParameters()).allMatch(parameter -> {
            Class<Annotation> defaultPropertyClass = (Class<Annotation>) resolveRequired(NAMED_ARG_CLASS);
            return parameter.getAnnotation(defaultPropertyClass) != null;
        });
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Set<String> getResolvedModules() {
        return resolvedClassesMap.values()
                                 .stream()
                                 .map(Class::getModule)
                                 .filter(Objects::nonNull)
                                 .map(Module::getName)
                                 .filter(Objects::nonNull)
                                 .collect(Collectors.toSet());
    }

    public TypeName resolveTypeNameWithoutVariables(Type type) {
        return switch (type) {
            case Class<?> clazz -> ClassName.get(clazz);
            case ParameterizedType parameterizedType -> {
                TypeName[] typeNames = Arrays.stream(parameterizedType.getActualTypeArguments())
                                             .map(this::resolveTypeNameWithoutVariables)
                                             .toArray(TypeName[]::new);
                Type rawType = parameterizedType.getRawType();
                if (!(rawType instanceof Class<?> rawClass)) {
                    throw new UnsupportedOperationException(
                            "Unable to resolve type name for parameterized type that isn't a class %s".formatted(
                                    rawType));
                }
                yield ParameterizedTypeName.get(ClassName.get(rawClass), typeNames);
            }
            case WildcardType wildcardType -> WildcardTypeName.get(wildcardType);
            case TypeVariable<?> typeVariable -> {
                Type[] bounds = typeVariable.getBounds();
                if (bounds.length != 1) {
                    throw new UnsupportedOperationException(
                            "Unable to resolve type name for multiple bounds for type %s".formatted(typeVariable));
                }

                yield WildcardTypeName.subtypeOf(resolveTypeNameWithoutVariables(bounds[0]));
            }
            case null -> null;
            default -> TypeName.get(type);
        };
    }

    public String getDeconflictedName(Type type) {
        Class<?> clazz = extractClass(type);
        String rawIdentifier = StringUtils.camelCase(clazz.getSimpleName());
        Integer nameCount = nameCounts.compute(rawIdentifier, (key, value) -> value == null ? 0 : value + 1);
        String identifier = rawIdentifier + nameCount;
        storeIdType(identifier, type);
        return identifier;
    }

    public void storeIdType(String id, Type type) {
        if (idTypeMap.put(id, type) != null) {
            throw new IllegalStateException("Multiple objects have the same id %s".formatted(id));
        }
    }

    public Type getStoredTypeById(String id) {
        return idTypeMap.computeIfAbsent(id, key -> {
            throw new IllegalStateException("No type known for id %s".formatted(id));
        });
    }

    public Class<?> getStoredClassById(String id) {
        return extractClass(idTypeMap.computeIfAbsent(id, key -> {
            throw new IllegalStateException("No type known for id %s".formatted(id));
        }));
    }

    public List<Constructor<?>> getConstructors(Type type) {
        return List.of(extractClass(type).getConstructors());
    }

    public boolean isPrimitive(Type type) {
        Class<?> clazz = extractClass(type);
        return clazz.isPrimitive() || MethodType.methodType(clazz).hasWrappers();
    }

    public Class<?> wrapType(Type type) {
        return MethodType.methodType(extractClass(type)).wrap().returnType();
    }

    private Class<?> extractClass(Type type) {
        return switch (type) {
            case Class<?> clazz -> clazz;
            case ParameterizedType parameterizedType -> extractClass(parameterizedType.getRawType());
            default -> throw new IllegalArgumentException("Unable to extract class from type %s".formatted(type));
        };
    }

    public boolean isAssignableFrom(Type baseType, Type checkedType) {
        return extractClass(baseType).isAssignableFrom(extractClass(checkedType));
    }
}
