package io.github.sheikah45.fx2j.parser.element;

import io.github.sheikah45.fx2j.parser.attribute.AssignableAttribute;
import io.github.sheikah45.fx2j.parser.property.FxmlProperty;
import io.github.sheikah45.fx2j.parser.utils.StringUtils;

public record InstancePropertyElement(String propertyName,
                                      ElementContent<AssignableAttribute, AssignableElement> content)
        implements AssignableElement, FxmlProperty.Instance {
    public InstancePropertyElement {
        if (StringUtils.isNullOrBlank(propertyName)) {
            throw new IllegalArgumentException("propertyName cannot be blank or null");
        }
    }
}
