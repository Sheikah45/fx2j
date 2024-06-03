package io.github.sheikah45.fx2j.parser.property;

import io.github.sheikah45.fx2j.parser.antlr.BindExpressionLexer;
import io.github.sheikah45.fx2j.parser.antlr.BindExpressionParser;
import io.github.sheikah45.fx2j.parser.antlr.BindExpressionVisitorImpl;
import io.github.sheikah45.fx2j.parser.utils.StringUtils;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.List;
import java.util.Objects;

sealed public interface Expression extends Value {
    static Expression parse(java.lang.String expression) {
        CodePointCharStream charStream = CharStreams.fromString(expression);
        BindExpressionLexer expressionLexer = new BindExpressionLexer(charStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(expressionLexer);
        BindExpressionParser expressionParser = new BindExpressionParser(commonTokenStream);
        return expressionParser.expression().accept(new BindExpressionVisitorImpl());
    }

    record PropertyRead(Expression expression, java.lang.String property) implements Expression {
        public PropertyRead {
            Objects.requireNonNull(expression, "expression cannot be null");
            if (StringUtils.isNullOrBlank(property)) {
                throw new IllegalArgumentException("propertyName cannot be blank or null");
            }
        }
    }
    record MethodCall(Expression expression, java.lang.String methodName, List<Expression> args) implements Expression {
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
    record Modulo(Expression left, Expression right) implements Expression {
        public Modulo {
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
    record Equal(Expression left, Expression right) implements Expression {
        public Equal {
            Objects.requireNonNull(left, "left cannot be null");
            Objects.requireNonNull(right, "right cannot be null");
        }
    }
    record NotEqual(Expression left, Expression right) implements Expression {
        public NotEqual {
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
    record Variable(java.lang.String value) implements Expression {
        public Variable {
            if (StringUtils.isNullOrBlank(value)) {
                throw new IllegalArgumentException("value cannot be null or blank");
            }
        }
    }
    record String(java.lang.String value) implements Expression {
        public String {
            Objects.requireNonNull(value, "left cannot be null");
        }
    }
    record Whole(long value) implements Expression {}
    record Fraction(double value) implements Expression {}
    record Boolean(boolean value) implements Expression {}
    record Null() implements Expression {}
}
