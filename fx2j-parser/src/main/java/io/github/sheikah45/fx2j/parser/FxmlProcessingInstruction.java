package io.github.sheikah45.fx2j.parser;

import io.github.sheikah45.fx2j.parser.utils.StringUtils;

public sealed interface FxmlProcessingInstruction {
    record Import(String className) implements FxmlProcessingInstruction {
        public Import {
            if (StringUtils.isNullOrBlank(className)) {
                throw new IllegalArgumentException("className cannot be blank or null");
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
            if (StringUtils.isNullOrBlank(name)) {
                throw new IllegalArgumentException("name cannot be blank or null");
            }
        }
    }
}
