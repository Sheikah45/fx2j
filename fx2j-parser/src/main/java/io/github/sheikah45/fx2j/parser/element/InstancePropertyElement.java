package io.github.sheikah45.fx2j.parser.element;

import io.github.sheikah45.fx2j.parser.property.FxmlProperty;
import io.github.sheikah45.fx2j.parser.property.Value;
import io.github.sheikah45.fx2j.parser.utils.StringUtils;

public record InstancePropertyElement(String property, Value value) implements FxmlElement, FxmlProperty.Instance {
    public InstancePropertyElement {
        if (StringUtils.isNullOrBlank(property)) {
            throw new IllegalArgumentException("property cannot be blank or null");
        }
    }
}
