package io.github.sheikah45.fx2j.parser.attribute;

import io.github.sheikah45.fx2j.parser.internal.utils.StringUtils;

import java.net.URI;
import java.util.Objects;

public record NameSpaceAttribute(String namespace, URI location) implements SpecialAttribute {
    public NameSpaceAttribute {
        Objects.requireNonNull(location, "location cannot be null");
        if (StringUtils.isNullOrBlank(namespace)) {
            throw new IllegalArgumentException("namespace cannot be blank or null");
        }
    }
}
