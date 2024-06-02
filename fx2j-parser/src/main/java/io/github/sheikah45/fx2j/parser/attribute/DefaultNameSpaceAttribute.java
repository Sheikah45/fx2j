package io.github.sheikah45.fx2j.parser.attribute;

import java.net.URI;
import java.util.Objects;

public record DefaultNameSpaceAttribute(URI location) implements SpecialAttribute {
    public DefaultNameSpaceAttribute {
        Objects.requireNonNull(location, "location cannot be null");
    }
}
