package io.github.sheikah45.fx2j.processor.internal.code;

import java.util.Objects;

sealed public interface Literal extends Expression {
    record Null() implements Literal {}
    record Bool(boolean value) implements Literal {}
    record Char(char value) implements Literal {}
    record Byte(byte value) implements Literal {}
    record Short(short value) implements Literal {}
    record Int(int value) implements Literal {}
    record Long(long value) implements Literal {}
    record Float(float value) implements Literal {}
    record Double(double value) implements Literal {}
    record Str(String value) implements Literal {
        public Str {
            Objects.requireNonNull(value, "value cannot be null");
        }
    }
}
