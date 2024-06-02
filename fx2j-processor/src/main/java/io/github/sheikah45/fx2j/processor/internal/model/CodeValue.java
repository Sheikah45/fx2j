package io.github.sheikah45.fx2j.processor.internal.model;

import java.util.List;
import java.util.Objects;

public sealed interface CodeValue {

    sealed interface ArrayInitialization extends CodeValue {
        record Declared(java.lang.reflect.Type componentType, List<CodeValue> values) implements ArrayInitialization {
            public Declared {
                Objects.requireNonNull(componentType, "componentType cannot be null");
                Objects.requireNonNull(values, "values cannot be null");
                values = List.copyOf(values);
            }
        }
        record Sized(java.lang.reflect.Type componentType, int size) implements ArrayInitialization {
            public Sized {
                Objects.requireNonNull(componentType, "componentType cannot be null");
            }
        }
    }
    record Null() implements CodeValue {}
    record Char(char value) implements CodeValue {}
    record Literal(java.lang.String value) implements CodeValue {
        public Literal {
            Objects.requireNonNull(value, "value cannot be null");
        }
    }
    record String(java.lang.String value) implements CodeValue {
        public String {
            Objects.requireNonNull(value, "value cannot be null");
        }
    }
    record Type(java.lang.reflect.Type type) implements CodeValue {
        public Type {
            Objects.requireNonNull(type, "type cannot be null");
        }
    }
    record Enum(java.lang.Enum<?> value) implements CodeValue {
        public Enum {
            Objects.requireNonNull(value, "value cannot be null");
        }
    }
    record MethodCall(CodeValue receiver, java.lang.String method, List<CodeValue> args) implements CodeValue {
        public MethodCall {
            Objects.requireNonNull(receiver, "receiver cannot be null");
            Objects.requireNonNull(method, "method cannot be null");
            Objects.requireNonNull(args, "args cannot be null");
            args = List.copyOf(args);
        }
    }
    record FieldAccess(CodeValue receiver, java.lang.String field) implements CodeValue {
        public FieldAccess {
            Objects.requireNonNull(receiver, "receiver cannot be null");
            Objects.requireNonNull(field, "field cannot be null");
        }
    }
    record Assignment(java.lang.reflect.Type type, java.lang.String identifier, CodeValue value) implements CodeValue {
        public Assignment {
            Objects.requireNonNull(type, "type cannot be null");
            Objects.requireNonNull(identifier, "identifier cannot be null");
            Objects.requireNonNull(value, "value cannot be null");
        }
    }
}
