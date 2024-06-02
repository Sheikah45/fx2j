package io.github.sheikah45.fx2j.parser.element;

import java.util.List;

public record DefineElement(List<ClassInstanceElement> elements) implements FxmlElement {}
