package io.github.sheikah45.fx2j.parser.attribute;

import io.github.sheikah45.fx2j.parser.property.FxmlProperty;
import io.github.sheikah45.fx2j.parser.property.Value;
import io.github.sheikah45.fx2j.parser.internal.utils.StringUtils;

import java.util.Objects;

public record InstancePropertyAttribute(String property, Value value) implements FxmlProperty.Instance,
        AssignableAttribute {
    public InstancePropertyAttribute {
        if (StringUtils.isNullOrBlank(property)) {
            throw new IllegalArgumentException("property cannot be blank or null");
        }
        Objects.requireNonNull(value, "value cannot be null");
    }
}
