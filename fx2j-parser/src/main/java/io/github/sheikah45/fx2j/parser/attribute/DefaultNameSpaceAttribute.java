package io.github.sheikah45.fx2j.parser.attribute;

import io.github.sheikah45.fx2j.parser.utils.StringUtils;

import java.net.URI;
import java.util.Objects;

public record DefaultNameSpaceAttribute(URI location) implements FxmlAttribute.SpecialAttribute {
    public DefaultNameSpaceAttribute {
        Objects.requireNonNull(location, "location cannot be null");
    }
}
