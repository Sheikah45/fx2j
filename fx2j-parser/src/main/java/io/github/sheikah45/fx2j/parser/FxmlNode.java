package io.github.sheikah45.fx2j.parser;

import java.util.List;


public record FxmlNode(FxmlElement element,
                       List<FxmlAttribute> attributes,
                       String innerText,
                       List<FxmlNode> children) {}
