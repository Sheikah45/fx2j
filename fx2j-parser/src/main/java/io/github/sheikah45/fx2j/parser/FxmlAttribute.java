package io.github.sheikah45.fx2j.parser;

import io.github.sheikah45.fx2j.parser.utils.StringUtils;

import java.nio.file.Path;
import java.util.Objects;

public sealed interface FxmlAttribute {
    sealed interface Property extends FxmlAttribute {
        record Instance(String property, Value value) implements Property {
            public Instance {
                if (StringUtils.isNullOrBlank(property)) {
                    throw new IllegalArgumentException("property cannot be blank or null");
                }
                Objects.requireNonNull(value, "value cannot be null");
            }
        }
        record Static(String className, String property, Value value) implements Property {
            public Static {
                if (StringUtils.isNullOrBlank(className)) {
                    throw new IllegalArgumentException("className cannot be blank or null");
                }
                if (StringUtils.isNullOrBlank(property)) {
                    throw new IllegalArgumentException("property cannot be blank or null");
                }
                Objects.requireNonNull(value, "value cannot be null");
            }
        }

        sealed interface Value {}

        record Empty() implements Value {}
        record Literal(String value) implements Value {
            public Literal {
                if (StringUtils.isNullOrBlank(value)) {
                    throw new IllegalArgumentException("value cannot be blank or null");
                }
            }
        }
        record Location(Path location) implements Value {
            public Location {
                Objects.requireNonNull(location, "location cannot be null");
            }
        }
        record Resource(String value) implements Value {
            public Resource {
                if (StringUtils.isNullOrBlank(value)) {
                    throw new IllegalArgumentException("value cannot be blank or null");
                }
            }
        }
        record Reference(String value) implements Value {
            public Reference {
                if (StringUtils.isNullOrBlank(value)) {
                    throw new IllegalArgumentException("value cannot be blank or null");
                }
            }
        }
        record Expression(String value) implements Value {
            public Expression {
                if (StringUtils.isNullOrBlank(value)) {
                    throw new IllegalArgumentException("value cannot be blank or null");
                }
            }
        }
    }
    record Id(String value) implements FxmlAttribute {
        public Id {
            if (StringUtils.isNullOrBlank(value)) {
                throw new IllegalArgumentException("id cannot be blank or null");
            }
        }
    }
    record Controller(String className) implements FxmlAttribute {
        public Controller {
            if (StringUtils.isNullOrBlank(className)) {
                throw new IllegalArgumentException("className cannot be blank or null");
            }
        }
    }
    record EventHandler(String eventName, Value value) implements FxmlAttribute {
        sealed interface Value {}

        record Script(String value) implements Value {
            public Script {
                if (StringUtils.isNullOrBlank(value)) {
                    throw new IllegalArgumentException("value cannot be blank or null");
                }
            }
        }
        record Method(String method) implements Value {
            public Method {
                if (StringUtils.isNullOrBlank(method)) {
                    throw new IllegalArgumentException("method cannot be blank or null");
                }
            }
        }
        record Reference(String reference) implements Value {
            public Reference {
                if (StringUtils.isNullOrBlank(reference)) {
                    throw new IllegalArgumentException("reference cannot be blank or null");
                }
            }
        }
    }
}
