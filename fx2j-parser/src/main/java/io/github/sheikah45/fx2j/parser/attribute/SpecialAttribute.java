package io.github.sheikah45.fx2j.parser.attribute;

sealed public interface SpecialAttribute extends FxmlAttribute
        permits ControllerAttribute, DefaultNameSpaceAttribute, IdAttribute, NameSpaceAttribute {}
