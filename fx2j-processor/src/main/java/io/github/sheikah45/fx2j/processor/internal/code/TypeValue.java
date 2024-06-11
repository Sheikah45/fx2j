package io.github.sheikah45.fx2j.processor.internal.code;

import java.util.List;
import java.util.Objects;

public sealed interface TypeValue {

    sealed interface Declarable extends TypeValue {}

    sealed interface Raw extends Declarable {
        record Nested(TypeValue.Raw ownerType, String simpleName) implements Raw {
            public Nested {
                Objects.requireNonNull(ownerType, "ownerType cannot be null");
                Objects.requireNonNull(simpleName, "simpleName cannot be null");
            }
        }
        record Top(String packageName, String simpleName) implements Raw {
            public Top {
                Objects.requireNonNull(packageName, "packageName cannot be null");
                Objects.requireNonNull(simpleName, "simpleName cannot be null");
            }
        }
        record Primitive(String name) implements Raw {}
        record Array(TypeValue.Declarable componentType) implements Raw {}
    }
    record Parameterized(TypeValue.Raw rawType, List<TypeValue> typeArguments) implements Declarable {
        public Parameterized {
            Objects.requireNonNull(rawType, "rawType cannot be null");
            Objects.requireNonNull(typeArguments, "typeArguments cannot be null");
            typeArguments = List.copyOf(typeArguments);
        }
    }
    record Wildcard(List<TypeValue> lowerBounds, List<TypeValue> upperBounds) implements TypeValue {
        public Wildcard {
            Objects.requireNonNull(upperBounds, "upperBounds cannot be null");
            Objects.requireNonNull(lowerBounds, "lowerBounds cannot be null");
            upperBounds = List.copyOf(upperBounds);
            lowerBounds = List.copyOf(lowerBounds);
        }
    }
    record Variable(String name, List<TypeValue> upperBounds) implements TypeValue {
        public Variable {
            Objects.requireNonNull(name, "name cannot be null");
            Objects.requireNonNull(upperBounds, "upperBounds cannot be null");
            upperBounds = List.copyOf(upperBounds);
        }
    }
}
