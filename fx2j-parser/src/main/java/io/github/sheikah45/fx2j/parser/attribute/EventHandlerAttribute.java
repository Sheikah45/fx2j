package io.github.sheikah45.fx2j.parser.attribute;

import io.github.sheikah45.fx2j.parser.property.FxmlProperty;
import io.github.sheikah45.fx2j.parser.property.Handler;

import java.util.Objects;

public record EventHandlerAttribute(String eventName, Handler handler) implements FxmlProperty.EventHandler,
        FxmlAttribute.CommonAttribute {
    public EventHandlerAttribute {
        Objects.requireNonNull(eventName, "eventName cannot be null");
        Objects.requireNonNull(handler, "handler cannot be null");
    }
}
