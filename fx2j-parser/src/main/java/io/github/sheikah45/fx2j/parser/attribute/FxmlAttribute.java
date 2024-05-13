package io.github.sheikah45.fx2j.parser.attribute;

public sealed interface FxmlAttribute {

    sealed interface FxAttribute extends FxmlAttribute permits ControllerAttribute, IdAttribute {}

    sealed interface CommonAttribute extends FxmlAttribute
            permits EventHandlerAttribute, InstancePropertyAttribute, StaticPropertyAttribute {}

}
