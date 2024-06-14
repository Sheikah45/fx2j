package io.github.sheikah45.fx2j.parser.element;

import io.github.sheikah45.fx2j.parser.internal.utils.FxmlFormatUtils;

public sealed interface FxmlElement
        permits AssignableElement, DefineElement, ScriptElement,
        StaticPropertyElement {

    default String toFxml() {
        return FxmlFormatUtils.toElementString(this);
    }
}
