package io.github.sheikah45.fx2j.parser.attribute;

import io.github.sheikah45.fx2j.parser.property.FxmlProperty;
import io.github.sheikah45.fx2j.parser.property.Value;
import io.github.sheikah45.fx2j.parser.utils.StringUtils;

import java.util.Objects;

public record InstancePropertyAttribute(String property, Value.Single value) implements FxmlProperty.Instance,
        FxmlAttribute.CommonAttribute {
    public InstancePropertyAttribute {
        if (StringUtils.isNullOrBlank(property)) {
            throw new IllegalArgumentException("property cannot be blank or null");
        }
        Objects.requireNonNull(value, "value cannot be null");
    }
}
