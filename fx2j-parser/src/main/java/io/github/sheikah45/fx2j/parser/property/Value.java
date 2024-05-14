package io.github.sheikah45.fx2j.parser.property;

import io.github.sheikah45.fx2j.parser.attribute.FxmlAttribute;
import io.github.sheikah45.fx2j.parser.element.FxmlElement;
import io.github.sheikah45.fx2j.parser.utils.StringUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

sealed public interface Value {

    record Multi(List<? extends Value.Single> values) implements Value {
        public Multi {
            Objects.requireNonNull(values, "values cannot be null");
            values = List.copyOf(values);
        }
    }

    sealed interface Single extends Value {}
    record Literal(String value) implements Single {
        public Literal {
            if (StringUtils.isNullOrBlank(value)) {
                throw new IllegalArgumentException("value cannot be blank or null");
            }
        }
    }
    sealed interface Handler extends Value {}
    record Location(Path value) implements Single {
        public Location {
            Objects.requireNonNull(value, "location cannot be null");
        }
    }
    record Resource(String value) implements Single {
        public Resource {
            if (StringUtils.isNullOrBlank(value)) {
                throw new IllegalArgumentException("value cannot be blank or null");
            }
        }
    }
    record Reference(String value) implements Single, Handler {
        public Reference {
            if (StringUtils.isNullOrBlank(value)) {
                throw new IllegalArgumentException("value cannot be blank or null");
            }
        }
    }
    record Element(FxmlElement value) implements Value.Single {}
    record Attribute(FxmlAttribute.CommonAttribute value) implements Value.Single {}
    record Expression(String value) implements Single {
        public Expression {
            if (StringUtils.isNullOrBlank(value)) {
                throw new IllegalArgumentException("value cannot be blank or null");
            }
        }
    }
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
    record Empty() implements Single, Handler {}
}
