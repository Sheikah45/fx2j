package io.github.sheikah45.fx2j.processor.internal.code;

import java.util.List;
import java.util.Objects;

sealed public interface Expression
        permits Expression.Array, Expression.Assignable, Expression.Enum, Expression.Lambda,
        Expression.Type, Literal, StatementExpression {
    sealed interface Assignable extends Expression {}
    sealed interface Array extends Expression {
        record Declared(TypeValue.Declarable componentType, List<? extends Expression> values) implements
                Array {
            public Declared {
                Objects.requireNonNull(componentType, "componentType cannot be null");
                Objects.requireNonNull(values, "values cannot be null");
                values = List.copyOf(values);
            }
        }
        record Sized(TypeValue.Declarable componentType, int size) implements Array {
            public Sized {
                Objects.requireNonNull(componentType, "componentType cannot be null");
            }
        }
    }
    sealed interface Lambda extends Expression {
        sealed interface Arrow extends Lambda {
            record Typed(List<Parameter> parameters, BlockStatement.Block body) implements Arrow {
                public Typed {
                    Objects.requireNonNull(parameters, "parameters cannot be null");
                    Objects.requireNonNull(body, "body cannot be null");
                    parameters = List.copyOf(parameters);
                }
            }
            record Untyped(List<String> parameters, BlockStatement.Block body) implements Arrow {
                public Untyped {
                    Objects.requireNonNull(parameters, "receiver cannot be null");
                    Objects.requireNonNull(body, "body cannot be null");
                    parameters = List.copyOf(parameters);
                }
            }
        }
        record MethodReference(Expression receiver, String methodName) implements Lambda {
            public MethodReference {
                Objects.requireNonNull(receiver, "receiver cannot be null");
                Objects.requireNonNull(methodName, "methodName cannot be null");
            }
        }

    }
    record Variable(String identifier) implements Assignable, Resource, Declarator {
        public Variable {
            Objects.requireNonNull(identifier, "identifier cannot be null");
        }
    }
    record Type(TypeValue type) implements Expression {
        public Type {
            Objects.requireNonNull(type, "type cannot be null");
        }
    }
    record Enum(java.lang.Enum<?> value) implements Expression {
        public Enum {
            Objects.requireNonNull(value, "value cannot be null");
        }
    }
    record FieldAccess(Expression receiver, String field) implements Assignable {
        public FieldAccess {
            Objects.requireNonNull(receiver, "receiver cannot be null");
            Objects.requireNonNull(field, "field cannot be null");
        }
    }
    record ArrayAccess(Expression receiver, Expression accessor) implements Assignable {
        public ArrayAccess {
            Objects.requireNonNull(receiver, "receiver cannot be null");
            Objects.requireNonNull(accessor, "accessor cannot be null");
        }
    }
}
