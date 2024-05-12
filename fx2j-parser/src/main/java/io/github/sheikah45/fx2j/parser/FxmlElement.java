package io.github.sheikah45.fx2j.parser;

import java.nio.charset.Charset;
import java.nio.file.Path;

public sealed interface FxmlElement {
    sealed interface Instance extends FxmlElement {
        String id();

        record Simple(String id, String className) implements Instance {}
        record Value(String id, String className, String value) implements Instance {}
        record Constant(String id, String className, String constant) implements Instance {}
        record Factory(String id, String factoryClassName, String factoryMethod) implements Instance {}
    }
    record Include(String id, Path source, Path resources, Charset charset) implements FxmlElement {}
    record Copy(String id, String source) implements FxmlElement {}
    record Reference(String id, String source) implements FxmlElement {}
    record Root(String id, String type) implements FxmlElement {}
    record Define() implements FxmlElement {}
    record Script() implements FxmlElement {}
    record Property(String property) implements FxmlElement {}
    record StaticProperty(String className, String property) implements FxmlElement {}
}
