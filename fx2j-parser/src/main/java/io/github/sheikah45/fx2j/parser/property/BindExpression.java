package io.github.sheikah45.fx2j.parser.property;

import io.github.sheikah45.fx2j.parser.internal.antlr.BindExpressionLexer;
import io.github.sheikah45.fx2j.parser.internal.antlr.BindExpressionParser;
import io.github.sheikah45.fx2j.parser.internal.antlr.BindExpressionVisitorImpl;
import io.github.sheikah45.fx2j.parser.internal.utils.StringUtils;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.List;
import java.util.Objects;

sealed public interface BindExpression extends Value {
    static BindExpression parse(java.lang.String expression) {
        CodePointCharStream charStream = CharStreams.fromString(expression);
        BindExpressionLexer expressionLexer = new BindExpressionLexer(charStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(expressionLexer);
        BindExpressionParser expressionParser = new BindExpressionParser(commonTokenStream);
        return expressionParser.expression().accept(new BindExpressionVisitorImpl());
    }

    record PropertyRead(BindExpression bindExpression, java.lang.String property) implements BindExpression {
        public PropertyRead {
            Objects.requireNonNull(bindExpression, "expression cannot be null");
            if (StringUtils.isNullOrBlank(property)) {
                throw new IllegalArgumentException("property cannot be blank or null");
            }
        }
    }
    record MethodCall(BindExpression bindExpression, java.lang.String methodName, List<BindExpression> args) implements
            BindExpression {
        public MethodCall {
            Objects.requireNonNull(bindExpression, "expression cannot be null");
            Objects.requireNonNull(args, "args cannot be null");
            if (StringUtils.isNullOrBlank(methodName)) {
                throw new IllegalArgumentException("methodName cannot be blank or null");
            }
        }
    }
    record CollectionAccess(BindExpression bindExpression, BindExpression key) implements BindExpression {
        public CollectionAccess {
            Objects.requireNonNull(bindExpression, "expression cannot be null");
            Objects.requireNonNull(key, "key cannot be null");
        }
    }
    record Negate(BindExpression bindExpression) implements BindExpression {
        public Negate {
            Objects.requireNonNull(bindExpression, "expression cannot be null");
        }
    }
    record Invert(BindExpression bindExpression) implements BindExpression {
        public Invert {
            Objects.requireNonNull(bindExpression, "expression cannot be null");
        }
    }
    record Multiply(BindExpression left, BindExpression right) implements BindExpression {
        public Multiply {
            Objects.requireNonNull(left, "left cannot be null");
            Objects.requireNonNull(right, "right cannot be null");
        }
    }
    record Divide(BindExpression left, BindExpression right) implements BindExpression {
        public Divide {
            Objects.requireNonNull(left, "left cannot be null");
            Objects.requireNonNull(right, "right cannot be null");
        }
    }
    record Modulo(BindExpression left, BindExpression right) implements BindExpression {
        public Modulo {
            Objects.requireNonNull(left, "left cannot be null");
            Objects.requireNonNull(right, "right cannot be null");
        }
    }
    record Add(BindExpression left, BindExpression right) implements BindExpression {
        public Add {
            Objects.requireNonNull(left, "left cannot be null");
            Objects.requireNonNull(right, "right cannot be null");
        }
    }
    record Subtract(BindExpression left, BindExpression right) implements BindExpression {
        public Subtract {
            Objects.requireNonNull(left, "left cannot be null");
            Objects.requireNonNull(right, "right cannot be null");
        }
    }
    record GreaterThan(BindExpression left, BindExpression right) implements BindExpression {
        public GreaterThan {
            Objects.requireNonNull(left, "left cannot be null");
            Objects.requireNonNull(right, "right cannot be null");
        }
    }
    record GreaterThanEqual(BindExpression left, BindExpression right) implements BindExpression {
        public GreaterThanEqual {
            Objects.requireNonNull(left, "left cannot be null");
            Objects.requireNonNull(right, "right cannot be null");
        }
    }
    record LessThan(BindExpression left, BindExpression right) implements BindExpression {
        public LessThan {
            Objects.requireNonNull(left, "left cannot be null");
            Objects.requireNonNull(right, "right cannot be null");
        }
    }
    record LessThanEqual(BindExpression left, BindExpression right) implements BindExpression {
        public LessThanEqual {
            Objects.requireNonNull(left, "left cannot be null");
            Objects.requireNonNull(right, "right cannot be null");
        }
    }
    record Equal(BindExpression left, BindExpression right) implements BindExpression {
        public Equal {
            Objects.requireNonNull(left, "left cannot be null");
            Objects.requireNonNull(right, "right cannot be null");
        }
    }
    record NotEqual(BindExpression left, BindExpression right) implements BindExpression {
        public NotEqual {
            Objects.requireNonNull(left, "left cannot be null");
            Objects.requireNonNull(right, "right cannot be null");
        }
    }
    record And(BindExpression left, BindExpression right) implements BindExpression {
        public And {
            Objects.requireNonNull(left, "left cannot be null");
            Objects.requireNonNull(right, "right cannot be null");
        }
    }
    record Or(BindExpression left, BindExpression right) implements BindExpression {
        public Or {
            Objects.requireNonNull(left, "left cannot be null");
            Objects.requireNonNull(right, "right cannot be null");
        }
    }
    record Variable(java.lang.String value) implements BindExpression {
        public Variable {
            if (StringUtils.isNullOrBlank(value)) {
                throw new IllegalArgumentException("value cannot be null or blank");
            }
        }
    }
    record String(java.lang.String value) implements BindExpression {
        public String {
            Objects.requireNonNull(value, "value cannot be null");
        }
    }
    record Whole(long value) implements BindExpression {}
    record Fraction(double value) implements BindExpression {}
    record Boolean(boolean value) implements BindExpression {}
    record Null() implements BindExpression {}
}
