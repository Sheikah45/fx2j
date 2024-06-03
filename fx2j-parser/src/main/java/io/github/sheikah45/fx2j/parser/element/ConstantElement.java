package io.github.sheikah45.fx2j.parser.element;

import io.github.sheikah45.fx2j.parser.utils.StringUtils;

import java.util.Objects;

public record ConstantElement(String className, String member, ElementContent<?, ?> content)
        implements DeclarationElement {
    public ConstantElement {
        Objects.requireNonNull(content, "content cannot be null");
        if (StringUtils.isNullOrBlank(className)) {
            throw new IllegalArgumentException("className cannot be blank or null");
        }

        if (StringUtils.isNullOrBlank(member)) {
            throw new IllegalArgumentException("member cannot be blank or null");
        }
    }
}
