package io.github.sheikah45.fx2j.parser.element;

import io.github.sheikah45.fx2j.parser.utils.StringUtils;

import java.util.Objects;

public record InstanceElement(String className, ElementContent<?, ?> content)
        implements DeclarationElement {
    public InstanceElement {
        Objects.requireNonNull(content, "content cannot be null");
        if (StringUtils.isNullOrBlank(className)) {
            throw new IllegalArgumentException("className cannot be blank or null");
        }
    }
}
