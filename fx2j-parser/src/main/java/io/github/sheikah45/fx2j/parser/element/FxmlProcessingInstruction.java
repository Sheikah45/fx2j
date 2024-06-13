package io.github.sheikah45.fx2j.parser.element;

import io.github.sheikah45.fx2j.parser.internal.utils.StringUtils;

import java.util.Objects;

public sealed interface FxmlProcessingInstruction {
    record Import(String value) implements FxmlProcessingInstruction {
        public Import {
            if (StringUtils.isNullOrBlank(value)) {
                throw new IllegalArgumentException("value cannot be blank or null");
            }
        }
    }
    record Language(String language) implements FxmlProcessingInstruction {
        public Language {
            if (StringUtils.isNullOrBlank(language)) {
                throw new IllegalArgumentException("language cannot be blank or null");
            }
        }
    }
    record Compile(boolean enabled) implements FxmlProcessingInstruction {}
    record Custom(String name, String value) implements FxmlProcessingInstruction {
        public Custom {
            Objects.requireNonNull(value, "value cannot be null");
            if (StringUtils.isNullOrBlank(name)) {
                throw new IllegalArgumentException("name cannot be blank or null");
            }
        }
    }
}
