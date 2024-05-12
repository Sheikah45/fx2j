package io.github.sheikah45.fx2j.parser;

import java.util.List;
import java.util.Objects;


public record FxmlNode(FxmlElement element,
                       List<FxmlAttribute> attributes,
                       String innerText,
                       List<FxmlNode> children) {

    public FxmlNode {
        Objects.requireNonNull(element, "element cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");
        Objects.requireNonNull(innerText, "innerText cannot be null");
        Objects.requireNonNull(children, "children cannot be null");
        attributes = List.copyOf(attributes);
        children = List.copyOf(children);
    }
}
