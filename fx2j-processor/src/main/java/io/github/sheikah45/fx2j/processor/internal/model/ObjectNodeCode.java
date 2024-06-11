package io.github.sheikah45.fx2j.processor.internal.model;

import io.github.sheikah45.fx2j.processor.internal.code.Expression;
import io.github.sheikah45.fx2j.processor.internal.code.Statement;

import java.lang.reflect.Type;
import java.util.List;

public record ObjectNodeCode(Expression.Variable nodeValue,
                             Type nodeClass,
                             List<Statement> initializers) {
    public ObjectNodeCode {
        initializers = List.copyOf(initializers);
    }
}
