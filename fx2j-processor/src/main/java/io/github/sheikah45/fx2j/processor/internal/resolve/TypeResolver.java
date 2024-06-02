package io.github.sheikah45.fx2j.processor.internal.resolve;

import io.github.sheikah45.fx2j.processor.internal.utils.StringUtils;

import java.lang.invoke.MethodType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class TypeResolver {

    private final ClassLoader classLoader;

    private final Map<String, Integer> idCounts = new HashMap<>();
    private final Map<String, Type> idTypeMap = new HashMap<>();
    private final Set<String> importPrefixes = new HashSet<>();
    private final Map<String, Class<?>> resolvedClassesMap = new HashMap<>();

    public TypeResolver(Set<String> imports, ClassLoader classLoader) {
        this.classLoader = classLoader;
        resolveImports(imports);
    }

    private void resolveImports(Set<String> imports) {
        imports.forEach(importString -> {
            if (importString.endsWith(".*")) {
                importPrefixes.add(importString.substring(0, importString.length() - 2));
            } else {
                Class<?> type = resolve(importString);
                String simpleName = StringUtils.substringAfterLast(importString, ".");
                Class<?> previousType = resolvedClassesMap.put(simpleName, type);
                if (previousType != null) {
                    throw new IllegalArgumentException(
                            "Name collision between imports %s and %s".formatted(previousType, type));
                }
            }
        });
    }

    public <T> Class<T> resolve(String typeName) {
        Class<T> clazz = (Class<T>) resolvedClassesMap.computeIfAbsent(typeName, this::resolveWithoutCache);
        if (clazz == null) {
            throw new IllegalArgumentException("Unable to find class for %s".formatted(typeName));
        }

        return clazz;
    }

    private Class<?> resolveWithoutCache(String typeName) {
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
        Class<?> clazz = null;
        while (clazz == null && subclassName.contains(".")) {
            subclassName = StringUtils.replaceLast(subclassName, ".", "$");
            clazz = resolveWithoutCache(subclassName);
        }

        return clazz;
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

    public Type[] resolveUpperBoundTypeArguments(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            return parameterizedType.getActualTypeArguments();
        } else {
            return new Type[0];
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

    public boolean typeArgumentsMeetBounds(Type type, Type[] boundTypeArguments) {
        if (!(type instanceof ParameterizedType parameterizedType)) {
            return true;
        }

        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length != boundTypeArguments.length) {
            return false;
        }

        for (int i = 0; i < actualTypeArguments.length; i++) {
            Type typeArgument = actualTypeArguments[i];
            Type wildcardBound = boundTypeArguments[i];
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

    public boolean isArray(Type type) {
        Class<?> clazz = resolveClassFromType(type);
        return clazz.isArray();
    }

    public Type getComponentType(Type type) {
        return resolveClassFromType(type).getComponentType();
    }

    public boolean isPrimitive(Type type) {
        Class<?> clazz = resolveClassFromType(type);
        return clazz.isPrimitive() || MethodType.methodType(clazz).hasWrappers();
    }

    public Class<?> wrapType(Type type) {
        return MethodType.methodType(resolveClassFromType(type)).wrap().returnType();
    }

    public boolean isAssignableFrom(Type baseType, Type checkedType) {
        return resolveClassFromType(baseType).isAssignableFrom(resolveClassFromType(checkedType));
    }

    public String getDeconflictedName(Type type) {
        Class<?> clazz = resolveClassFromType(type);
        String rawIdentifier = StringUtils.camelCase(clazz.getSimpleName());
        Integer nameCount = idCounts.compute(rawIdentifier, (key, value) -> value == null ? 0 : value + 1);
        String identifier = rawIdentifier + nameCount;
        storeIdType(identifier, type);
        return identifier;
    }

    public void storeIdType(String id, Type type) {
        if (idTypeMap.containsKey(id)) {
            throw new IllegalStateException("Multiple objects have the same id %s".formatted(id));
        }

        idTypeMap.put(id, type);
    }

    public Type getStoredTypeById(String id) {
        return idTypeMap.computeIfAbsent(id, key -> {
            throw new IllegalStateException("No type known for id %s".formatted(id));
        });
    }

    public Class<?> getStoredClassById(String id) {
        return resolveClassFromType(idTypeMap.computeIfAbsent(id, key -> {
            throw new IllegalStateException("No type known for id %s".formatted(id));
        }));
    }
}
