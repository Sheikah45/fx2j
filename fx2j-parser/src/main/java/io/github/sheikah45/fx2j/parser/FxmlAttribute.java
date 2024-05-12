package io.github.sheikah45.fx2j.parser;

import java.nio.file.Path;

public sealed interface FxmlAttribute {
    sealed interface Property extends FxmlAttribute {
        sealed interface Value {}
        record Instance(String property, Value value) implements Property {}
        record Static(String className, String property, Value value) implements Property {}
        record Literal(String value) implements Value {}
        record Location(Path location) implements Value {}
        record Resource(String resource) implements Value {}
        record Reference(String variable) implements Value {}
        record BindExpression(String expression) implements Value {}
    }
    record Controller(String className) implements FxmlAttribute {}
    record EventHandler(String eventName, Value value) implements FxmlAttribute {
        sealed interface Value {}

        record Script(String value) implements Value {}
        record Method(String method) implements Value {}
        record Reference(String reference) implements Value {}
    }
}
