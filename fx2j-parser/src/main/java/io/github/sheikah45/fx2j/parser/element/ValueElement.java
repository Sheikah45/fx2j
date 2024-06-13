package io.github.sheikah45.fx2j.parser.element;

import io.github.sheikah45.fx2j.parser.internal.utils.StringUtils;

import java.util.Objects;

public record ValueElement(String className, String value, ElementContent<?, ?> content) implements
        DeclarationElement {
    public ValueElement {
        Objects.requireNonNull(content, "content cannot be null");
        if (StringUtils.isNullOrBlank(className)) {
            throw new IllegalArgumentException("className cannot be blank or null");
        }

        Objects.requireNonNull(value, "value cannot be null");
    }
}
