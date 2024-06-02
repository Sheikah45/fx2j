package io.github.sheikah45.fx2j.processor.internal.model;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public record ExpressionResult(Type type, String identifier, List<CodeValue.Assignment> initializers) {
    public ExpressionResult {
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(identifier, "identifier cannot be null");
        Objects.requireNonNull(initializers, "initializers cannot be null");
        initializers = List.copyOf(initializers);
    }
}
