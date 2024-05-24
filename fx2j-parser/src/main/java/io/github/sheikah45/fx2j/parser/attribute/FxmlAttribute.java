package io.github.sheikah45.fx2j.parser.attribute;

public sealed interface FxmlAttribute {

    sealed interface SpecialAttribute extends FxmlAttribute
            permits ControllerAttribute, DefaultNameSpaceAttribute, IdAttribute, NameSpaceAttribute {}

    sealed interface CommonAttribute extends FxmlAttribute
            permits EventHandlerAttribute, InstancePropertyAttribute, StaticPropertyAttribute {}

}
