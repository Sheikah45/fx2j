package io.github.sheikah45.fx2j.parser.property;

import io.github.sheikah45.fx2j.parser.attribute.EventHandlerAttribute;
import io.github.sheikah45.fx2j.parser.attribute.InstancePropertyAttribute;
import io.github.sheikah45.fx2j.parser.attribute.StaticPropertyAttribute;
import io.github.sheikah45.fx2j.parser.element.InstancePropertyElement;
import io.github.sheikah45.fx2j.parser.element.StaticPropertyElement;

public sealed interface FxmlProperty {

    sealed interface Static extends FxmlProperty permits StaticPropertyAttribute, StaticPropertyElement {
        String className();

        String property();
    }

    sealed interface Instance extends FxmlProperty
            permits InstancePropertyAttribute, InstancePropertyElement {
        String property();
    }

    sealed interface EventHandler permits EventHandlerAttribute {
        Handler handler();

        String eventName();
    }
}
