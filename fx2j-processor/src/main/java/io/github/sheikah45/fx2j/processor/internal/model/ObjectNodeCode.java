package io.github.sheikah45.fx2j.processor.internal.model;

import io.github.sheikah45.fx2j.processor.internal.code.CodeValue;

import java.lang.reflect.Type;
import java.util.List;

public record ObjectNodeCode(CodeValue.Variable nodeValue,
                             Type nodeClass,
                             List<CodeValue.Statement> initializers) {
    public ObjectNodeCode {
        initializers = List.copyOf(initializers);
    }
}
