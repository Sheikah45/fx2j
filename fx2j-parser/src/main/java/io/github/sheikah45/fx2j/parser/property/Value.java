package io.github.sheikah45.fx2j.parser.property;

import io.github.sheikah45.fx2j.parser.internal.utils.StringUtils;

import java.nio.file.Path;
import java.util.Objects;

sealed public interface Value
        permits BindExpression, Value.Empty, Value.Literal, Value.Location, Value.Reference, Value.Resource {
    record Empty() implements Value {}
    record Literal(String value) implements Value {}
    record Location(Path value) implements Value {
        public Location {
            Objects.requireNonNull(value, "location cannot be null");
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
}
