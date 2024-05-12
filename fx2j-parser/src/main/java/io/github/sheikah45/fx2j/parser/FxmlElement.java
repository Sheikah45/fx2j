package io.github.sheikah45.fx2j.parser;

import io.github.sheikah45.fx2j.parser.utils.StringUtils;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;

public sealed interface FxmlElement {
    sealed interface Declaration extends FxmlElement {
        record Class(String className) implements Declaration {
            public Class {
                if (StringUtils.isNullOrBlank(className)) {
                    throw new IllegalArgumentException("className cannot be blank or null");
                }
            }
        }
        record Value(String className, String value) implements Declaration {
            public Value {
                if (StringUtils.isNullOrBlank(className)) {
                    throw new IllegalArgumentException("className cannot be blank or null");
                }

                if (StringUtils.isNullOrBlank(value)) {
                    throw new IllegalArgumentException("value cannot be blank or null");
                }
            }
        }
        record Constant(String className, String value) implements Declaration {
            public Constant {
                if (StringUtils.isNullOrBlank(className)) {
                    throw new IllegalArgumentException("className cannot be blank or null");
                }

                if (StringUtils.isNullOrBlank(value)) {
                    throw new IllegalArgumentException("value cannot be blank or null");
                }
            }
        }
        record Factory(String factoryClassName, String factoryMethod) implements Declaration {
            public Factory {
                if (StringUtils.isNullOrBlank(factoryClassName)) {
                    throw new IllegalArgumentException("factoryClassName cannot be blank or null");
                }

                if (StringUtils.isNullOrBlank(factoryMethod)) {
                    throw new IllegalArgumentException("factoryMethod cannot be blank or null");
                }
            }
        }
    }
    sealed interface Property extends FxmlElement {
        record Instance(String property) implements Property {
            public Instance {
                if (StringUtils.isNullOrBlank(property)) {
                    throw new IllegalArgumentException("property cannot be blank or null");
                }
            }
        }

        record Static(String className, String property) implements Property {
            public Static {
                if (StringUtils.isNullOrBlank(className)) {
                    throw new IllegalArgumentException("className cannot be blank or null");
                }
                if (StringUtils.isNullOrBlank(property)) {
                    throw new IllegalArgumentException("property cannot be blank or null");
                }
            }
        }
    }
    record Include(Path source, Path resources, Charset charset) implements FxmlElement {
        public Include {
            Objects.requireNonNull(source, "source cannot be null");
            Objects.requireNonNull(charset, "charset cannot be null");
        }
    }
    record Copy(String source) implements FxmlElement {
        public Copy {
            if (StringUtils.isNullOrBlank(source)) {
                throw new IllegalArgumentException("source cannot be blank or null");
            }
        }
    }
    record Reference(String source) implements FxmlElement {
        public Reference {
            if (StringUtils.isNullOrBlank(source)) {
                throw new IllegalArgumentException("source cannot be blank or null");
            }
        }
    }
    record Define() implements FxmlElement {}
    record Script() implements FxmlElement {}
    record Root(String type) implements FxmlElement {
        public Root {
            if (StringUtils.isNullOrBlank(type)) {
                throw new IllegalArgumentException("type cannot be blank or null");
            }
        }
    }

}
