package io.github.sheikah45.fx2j.parser.element;

import io.github.sheikah45.fx2j.parser.attribute.FxmlAttribute;
import io.github.sheikah45.fx2j.parser.property.Value;

import java.util.List;
import java.util.Objects;

sealed public interface ClassInstanceElement extends FxmlElement
        permits CopyElement, DeclarationElement, IncludeElement, ReferenceElement {
    Content content();

    record Content(List<FxmlAttribute> attributes, List<FxmlElement> children, Value.Single body) {
        public Content {
            Objects.requireNonNull(attributes, "attributes cannot be null");
            Objects.requireNonNull(children, "children cannot be null");
            Objects.requireNonNull(body, "text cannot be null");
            attributes = List.copyOf(attributes);
            children = List.copyOf(children);
        }
    }
}
