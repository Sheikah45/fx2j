package io.github.sheikah45.fx2j.parser.attribute;

import io.github.sheikah45.fx2j.parser.utils.StringUtils;

public record IdAttribute(String value) implements FxmlAttribute.FxAttribute {
    public IdAttribute {
        if (StringUtils.isNullOrBlank(value)) {
            throw new IllegalArgumentException("id cannot be blank or null");
        }
    }
}
