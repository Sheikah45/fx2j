package io.github.sheikah45.fx2j.parser.element;

import io.github.sheikah45.fx2j.parser.utils.StringUtils;

import java.util.Objects;

public record RootElement(String type, ElementContent<?, ?> content) implements DeclarationElement {
    public RootElement {
        Objects.requireNonNull(content, "content cannot be null");
        if (StringUtils.isNullOrBlank(type)) {
            throw new IllegalArgumentException("type cannot be blank or null");
        }
    }
}
