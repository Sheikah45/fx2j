package io.github.sheikah45.fx2j.processor.internal;

import java.util.HashMap;
import java.util.Map;

public class NameTracker {

    private final Map<String, Integer> nameCounts = new HashMap<>();
    private final Map<String, Class<?>> idClassMap = new HashMap<>();

    public String getDeconflictedName(String requestedName) {
        Integer nameCount = nameCounts.compute(requestedName, (key, value) -> value == null ? 0 : value + 1);
        return requestedName + nameCount;
    }

    public void storeIdClass(String id, Class<?> clazz) {
        if (idClassMap.put(id, clazz) != null) {
            throw new IllegalStateException("Multiple objects have the same id %s".formatted(id));
        }
    }

    public Class<?> getStoredClassById(String id) {
        return idClassMap.computeIfAbsent(id, key -> {
            throw new IllegalStateException("No class known for id %s".formatted(id));
        });
    }
}
