package io.github.sheikah45.fx2j.parser.element;

import io.github.sheikah45.fx2j.parser.attribute.AssignableAttribute;
import io.github.sheikah45.fx2j.parser.property.FxmlProperty;
import io.github.sheikah45.fx2j.parser.internal.utils.StringUtils;

public record InstancePropertyElement(String property,
                                      ElementContent<AssignableAttribute, AssignableElement> content)
        implements AssignableElement, FxmlProperty.Instance {
    public InstancePropertyElement {
        if (StringUtils.isNullOrBlank(property)) {
            throw new IllegalArgumentException("property cannot be blank or null");
        }
    }
}
