package io.github.sheikah45.fx2j.parser.element;

import java.util.List;
import java.util.Objects;

public record DefineElement(List<ClassInstanceElement> elements) implements FxmlElement {

    public DefineElement {
        Objects.requireNonNull(elements, "elements cannot be null");
        elements = List.copyOf(elements);
    }
}
