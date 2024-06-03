package io.github.sheikah45.fx2j.parser.element;

sealed public interface ClassInstanceElement extends AssignableElement
        permits CopyElement, DeclarationElement, IncludeElement, ReferenceElement {
    ElementContent<?, ?> content();

}
