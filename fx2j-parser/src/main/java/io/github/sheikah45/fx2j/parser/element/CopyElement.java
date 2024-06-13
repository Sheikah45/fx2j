package io.github.sheikah45.fx2j.parser.element;

import io.github.sheikah45.fx2j.parser.internal.utils.StringUtils;

import java.util.Objects;

public record CopyElement(String source, ElementContent<?, ?> content) implements ClassInstanceElement {
    public CopyElement {
        Objects.requireNonNull(content, "content cannot be null");
        if (StringUtils.isNullOrBlank(source)) {
            throw new IllegalArgumentException("source cannot be blank or null");
        }
    }
}
