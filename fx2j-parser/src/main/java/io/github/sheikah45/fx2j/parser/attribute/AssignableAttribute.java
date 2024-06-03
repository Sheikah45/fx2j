package io.github.sheikah45.fx2j.parser.attribute;

sealed public interface AssignableAttribute extends CommonAttribute
        permits EventHandlerAttribute, InstancePropertyAttribute {}
