package io.github.sheikah45.fx2j.parser.attribute;

sealed public interface CommonAttribute extends FxmlAttribute
        permits AssignableAttribute, StaticPropertyAttribute {}
