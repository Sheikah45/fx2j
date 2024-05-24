package io.github.sheikah45.fx2j.parser.element;

import java.util.Objects;

public record ScriptElement(ScriptSource source) implements FxmlElement {

    public ScriptElement {
        Objects.requireNonNull(source, "content cannot be null");
    }
}
