package io.github.sheikah45.fx2j.processor.internal.code;

import java.util.List;
import java.util.Objects;

public sealed interface CodeType {

    sealed interface Declarable extends CodeType {}

    sealed interface Raw extends Declarable {
        record Nested(CodeType.Raw ownerType, String simpleName) implements Raw {
            public Nested {
                Objects.requireNonNull(ownerType, "ownerType cannot be null");
                Objects.requireNonNull(simpleName, "simpleName cannot be null");
            }
        }
        record TopLevel(String packageName, String simpleName) implements Raw {
            public TopLevel {
                Objects.requireNonNull(packageName, "packageName cannot be null");
                Objects.requireNonNull(simpleName, "simpleName cannot be null");
            }
        }
        record Primitive(String name) implements Raw {}
        record Array(CodeType.Declarable componentType) implements Raw {}
    }
    record Parameterized(CodeType.Raw rawType, List<CodeType> typeArguments) implements Declarable {
        public Parameterized {
            Objects.requireNonNull(rawType, "rawType cannot be null");
            Objects.requireNonNull(typeArguments, "typeArguments cannot be null");
            typeArguments = List.copyOf(typeArguments);
        }
    }
    record Wildcard(List<CodeType> lowerBounds, List<CodeType> upperBounds) implements CodeType {
        public Wildcard {
            Objects.requireNonNull(upperBounds, "upperBounds cannot be null");
            Objects.requireNonNull(lowerBounds, "lowerBounds cannot be null");
            upperBounds = List.copyOf(upperBounds);
            lowerBounds = List.copyOf(lowerBounds);
        }
    }
    record Variable(String name, List<CodeType> upperBounds) implements CodeType {
        public Variable {
            Objects.requireNonNull(name, "name cannot be null");
            Objects.requireNonNull(upperBounds, "upperBounds cannot be null");
            upperBounds = List.copyOf(upperBounds);
        }
    }
}
