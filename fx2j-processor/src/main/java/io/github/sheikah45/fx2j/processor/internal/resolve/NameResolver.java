package io.github.sheikah45.fx2j.processor.internal.resolve;

import io.github.sheikah45.fx2j.processor.internal.utils.StringUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class NameResolver {

    private final TypeResolver typeResolver;

    private final Map<String, Integer> idCounts = new HashMap<>();
    private final Map<String, Type> idTypeMap = new HashMap<>();

    NameResolver(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    public String resolveUniqueName(Type type) {
        Class<?> clazz = typeResolver.resolveClassFromType(type);
        String rawIdentifier = StringUtils.camelCase(clazz.getSimpleName());
        Integer nameCount = idCounts.compute(rawIdentifier, (key, value) -> value == null ? 0 : value + 1);
        String identifier = rawIdentifier + nameCount;
        storeIdType(identifier, type);
        return identifier;
    }

    public void storeIdType(String id, Type type) {
        if (idTypeMap.containsKey(id)) {
            throw new IllegalArgumentException(
                    "Type mapping to %s already exists for id %s".formatted(idTypeMap.get(id), id));
        }

        idTypeMap.put(id, type);
    }

    public Type resolveTypeById(String id) {
        return idTypeMap.computeIfAbsent(id, key -> {
            throw new IllegalArgumentException("No type known for id %s".formatted(id));
        });
    }
}
