package io.github.sheikah45.fx2j.parser.attribute;

import io.github.sheikah45.fx2j.parser.utils.StringUtils;

public record ControllerAttribute(String className) implements SpecialAttribute {
    public ControllerAttribute {
        if (StringUtils.isNullOrBlank(className)) {
            throw new IllegalArgumentException("className cannot be blank or null");
        }
    }
}
