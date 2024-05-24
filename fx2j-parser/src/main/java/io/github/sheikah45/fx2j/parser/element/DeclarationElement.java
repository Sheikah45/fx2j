package io.github.sheikah45.fx2j.parser.element;

sealed public interface DeclarationElement extends ClassInstanceElement
        permits InstanceElement, ConstantElement, FactoryElement, RootElement, ValueElement {}
