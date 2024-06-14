package io.github.sheikah45.fx2j.parser.element;

import io.github.sheikah45.fx2j.parser.attribute.AssignableAttribute;
import io.github.sheikah45.fx2j.parser.internal.utils.StringUtils;
import io.github.sheikah45.fx2j.parser.property.FxmlProperty;

import java.util.Objects;

public record StaticPropertyElement(String className,
                                    String property,
                                    ElementContent<AssignableAttribute, AssignableElement> content)
        implements FxmlElement, FxmlProperty.Static {
    public StaticPropertyElement {
        Objects.requireNonNull(content, "content cannot be null");
        if (StringUtils.isNullOrBlank(className)) {
            throw new IllegalArgumentException("className cannot be blank or null");
        }
        if (StringUtils.isNullOrBlank(property)) {
            throw new IllegalArgumentException("property cannot be blank or null");
        }
    }
}
