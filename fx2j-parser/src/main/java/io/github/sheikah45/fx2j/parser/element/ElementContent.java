package io.github.sheikah45.fx2j.parser.element;

import io.github.sheikah45.fx2j.parser.attribute.FxmlAttribute;
import io.github.sheikah45.fx2j.parser.property.Value;

import java.util.List;
import java.util.Objects;

public record ElementContent<A extends FxmlAttribute, E extends FxmlElement>(List<A> attributes,
                                                                             List<E> elements,
                                                                             Value value) {
    public ElementContent {
        Objects.requireNonNull(attributes, "attributes cannot be null");
        Objects.requireNonNull(elements, "elements cannot be null");
        Objects.requireNonNull(value, "value cannot be null");
        attributes = List.copyOf(attributes);
        elements = List.copyOf(elements);
    }
}
