package io.github.sheikah45.fx2j.processor.internal.model;

import io.github.sheikah45.fx2j.processor.internal.code.CodeValue;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public record ExpressionResult(Type type, CodeValue.Expression value, List<CodeValue.Declaration> initializers) {
    public ExpressionResult {
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(value, "value cannot be null");
        Objects.requireNonNull(initializers, "initializers cannot be null");
        initializers = List.copyOf(initializers);
    }
}
