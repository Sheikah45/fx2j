package io.github.sheikah45.fx2j.parser.element;

public sealed interface FxmlElement
        permits AssignableElement, DefineElement, ScriptElement,
        StaticPropertyElement {}
