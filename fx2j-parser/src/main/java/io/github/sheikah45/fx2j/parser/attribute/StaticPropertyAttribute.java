package io.github.sheikah45.fx2j.parser.attribute;

import io.github.sheikah45.fx2j.parser.property.FxmlProperty;
import io.github.sheikah45.fx2j.parser.property.Value;
import io.github.sheikah45.fx2j.parser.internal.utils.StringUtils;

import java.util.Objects;

public record StaticPropertyAttribute(String className, String property, Value value)
        implements CommonAttribute, FxmlProperty.Static {
    public StaticPropertyAttribute {
        if (StringUtils.isNullOrBlank(className)) {
            throw new IllegalArgumentException("className cannot be blank or null");
        }
        if (StringUtils.isNullOrBlank(property)) {
            throw new IllegalArgumentException("propertyName cannot be blank or null");
        }
        Objects.requireNonNull(value, "value cannot be null");
    }
}
