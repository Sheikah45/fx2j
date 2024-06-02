package io.github.sheikah45.fx2j.parser.property;

import io.github.sheikah45.fx2j.parser.utils.StringUtils;

sealed public interface Handler {
    record Empty() implements Handler {}
    record Script(String value) implements Handler {
        public Script {
            if (StringUtils.isNullOrBlank(value)) {
                throw new IllegalArgumentException("value cannot be blank or null");
            }
        }
    }
    record Method(String name) implements Handler {
        public Method {
            if (StringUtils.isNullOrBlank(name)) {
                throw new IllegalArgumentException("name cannot be blank or null");
            }
        }
    }
    record Reference(String value) implements Handler {
        public Reference {
            if (StringUtils.isNullOrBlank(value)) {
                throw new IllegalArgumentException("value cannot be blank or null");
            }
        }
    }
}
