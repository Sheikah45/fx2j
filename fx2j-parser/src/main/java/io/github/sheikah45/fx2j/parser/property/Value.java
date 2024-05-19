package io.github.sheikah45.fx2j.parser.property;

import java.util.List;
import java.util.Objects;

sealed public interface Value permits Handler, Value.Multi, Value.Single {

    record Multi(List<? extends Value.Single> values) implements Value {
        public Multi {
            Objects.requireNonNull(values, "values cannot be null");
            values = List.copyOf(values);
        }
    }

    sealed interface Single extends Value permits Expression, Concrete {}
}
