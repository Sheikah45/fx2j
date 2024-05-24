package io.github.sheikah45.fx2j.parser.element;

import io.github.sheikah45.fx2j.parser.utils.StringUtils;

import java.util.Objects;

public record FactoryElement(String factoryClassName, String factoryMethod, Content content)
        implements DeclarationElement {
    public FactoryElement {
        Objects.requireNonNull(content, "content cannot be null");
        if (StringUtils.isNullOrBlank(factoryClassName)) {
            throw new IllegalArgumentException("factoryClassName cannot be blank or null");
        }

        if (StringUtils.isNullOrBlank(factoryMethod)) {
            throw new IllegalArgumentException("factoryMethod cannot be blank or null");
        }
    }
}
