package io.github.sheikah45.fx2j.processor.internal.model;

import com.squareup.javapoet.CodeBlock;

import java.lang.reflect.Type;

public record ExpressionResult(Type type, String identifier, CodeBlock initializationCode) {}
