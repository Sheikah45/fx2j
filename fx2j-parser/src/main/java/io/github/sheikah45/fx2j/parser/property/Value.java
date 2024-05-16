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
    record Element(FxmlElement value) implements Value.Single {}
    record Attribute(FxmlAttribute.CommonAttribute value) implements Value.Single {}
    sealed interface Expression extends Single {
        record PropertyRead(Expression expression, String property) implements Expression {
            public PropertyRead {
                Objects.requireNonNull(expression, "expression cannot be null");
                if (StringUtils.isNullOrBlank(property)) {
                    throw new IllegalArgumentException("property cannot be blank or null");
                }
            }
        }
        record MethodCall(Expression expression, String methodName, List<Expression> args) implements Expression {
            public MethodCall {
                Objects.requireNonNull(expression, "expression cannot be null");
                Objects.requireNonNull(args, "args cannot be null");
                if (StringUtils.isNullOrBlank(methodName)) {
                    throw new IllegalArgumentException("methodName cannot be blank or null");
                }
            }
        }
        record CollectionAccess(Expression expression, Expression key) implements Expression {
            public CollectionAccess {
                Objects.requireNonNull(expression, "expression cannot be null");
                Objects.requireNonNull(key, "key cannot be null");
            }
        }
        record Negate(Expression expression) implements Expression {
            public Negate {
                Objects.requireNonNull(expression, "expression cannot be null");
            }
        }
        record Invert(Expression expression) implements Expression {
            public Invert {
                Objects.requireNonNull(expression, "expression cannot be null");
            }
        }
        record Multiply(Expression left, Expression right) implements Expression {
            public Multiply {
                Objects.requireNonNull(left, "left cannot be null");
                Objects.requireNonNull(right, "right cannot be null");
            }
        }
        record Divide(Expression left, Expression right) implements Expression {
            public Divide {
                Objects.requireNonNull(left, "left cannot be null");
                Objects.requireNonNull(right, "right cannot be null");
            }
        }
        record Remainder(Expression left, Expression right) implements Expression {
            public Remainder {
                Objects.requireNonNull(left, "left cannot be null");
                Objects.requireNonNull(right, "right cannot be null");
            }
        }
        record Add(Expression left, Expression right) implements Expression {
            public Add {
                Objects.requireNonNull(left, "left cannot be null");
                Objects.requireNonNull(right, "right cannot be null");
            }
        }
        record Subtract(Expression left, Expression right) implements Expression {
            public Subtract {
                Objects.requireNonNull(left, "left cannot be null");
                Objects.requireNonNull(right, "right cannot be null");
            }
        }
        record GreaterThan(Expression left, Expression right) implements Expression {
            public GreaterThan {
                Objects.requireNonNull(left, "left cannot be null");
                Objects.requireNonNull(right, "right cannot be null");
            }
        }
        record GreaterThanEqual(Expression left, Expression right) implements Expression {
            public GreaterThanEqual {
                Objects.requireNonNull(left, "left cannot be null");
                Objects.requireNonNull(right, "right cannot be null");
            }
        }
        record LessThan(Expression left, Expression right) implements Expression {
            public LessThan {
                Objects.requireNonNull(left, "left cannot be null");
                Objects.requireNonNull(right, "right cannot be null");
            }
        }
        record LessThanEqual(Expression left, Expression right) implements Expression {
            public LessThanEqual {
                Objects.requireNonNull(left, "left cannot be null");
                Objects.requireNonNull(right, "right cannot be null");
            }
        }
        record Equality(Expression left, Expression right) implements Expression {
            public Equality {
                Objects.requireNonNull(left, "left cannot be null");
                Objects.requireNonNull(right, "right cannot be null");
            }
        }
        record Inequality(Expression left, Expression right) implements Expression {
            public Inequality {
                Objects.requireNonNull(left, "left cannot be null");
                Objects.requireNonNull(right, "right cannot be null");
            }
        }
        record And(Expression left, Expression right) implements Expression {
            public And {
                Objects.requireNonNull(left, "left cannot be null");
                Objects.requireNonNull(right, "right cannot be null");
            }
        }
        record Or(Expression left, Expression right) implements Expression {
            public Or {
                Objects.requireNonNull(left, "left cannot be null");
                Objects.requireNonNull(right, "right cannot be null");
            }
        }
        record Variable(String value) implements Expression {
            public Variable {
                if (StringUtils.isNullOrBlank(value)) {
                    throw new IllegalArgumentException("value cannot be null or blank");
                }
            }
        }
        record Str(String value) implements Expression {
            public Str {
                Objects.requireNonNull(value, "left cannot be null");
            }
        }
        record Whole(long value) implements Expression {}
        record Fraction(double value) implements Expression {}
        record Null() implements Expression {}
        record True() implements Expression {}
        record False() implements Expression {}
    }

    sealed interface Handler extends Value {}
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
    record Reference(String value) implements Single, Handler {
        public Reference {
            if (StringUtils.isNullOrBlank(value)) {
                throw new IllegalArgumentException("value cannot be blank or null");
            }
        }
    }
    record Empty() implements Single, Handler {}
}
