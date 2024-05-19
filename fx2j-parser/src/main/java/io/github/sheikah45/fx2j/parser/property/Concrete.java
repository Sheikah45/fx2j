package io.github.sheikah45.fx2j.parser.property;

import io.github.sheikah45.fx2j.parser.attribute.FxmlAttribute;
import io.github.sheikah45.fx2j.parser.element.FxmlElement;
import io.github.sheikah45.fx2j.parser.utils.StringUtils;

import java.nio.file.Path;
import java.util.Objects;

sealed public interface Concrete extends Value.Single {
    record Empty() implements Concrete {}
    record Literal(String value) implements Concrete {
        public Literal {
            if (StringUtils.isNullOrBlank(value)) {
                throw new IllegalArgumentException("value cannot be blank or null");
            }
        }
    }
    record Location(Path value) implements Concrete {
        public Location {
            Objects.requireNonNull(value, "location cannot be null");
        }
    }
    record Resource(String value) implements Concrete {
        public Resource {
            if (StringUtils.isNullOrBlank(value)) {
                throw new IllegalArgumentException("value cannot be blank or null");
            }
        }
    }
    record Element(FxmlElement value) implements Concrete {}
    record Attribute(FxmlAttribute.CommonAttribute value) implements Concrete {}
    record Reference(String value) implements Concrete {
        public Reference {
            if (StringUtils.isNullOrBlank(value)) {
                throw new IllegalArgumentException("value cannot be blank or null");
            }
        }
    }
}
