package io.github.sheikah45.fx2j.compiler.internal.model;

import com.squareup.javapoet.CodeBlock;

public record ObjectNodeCode(String nodeIdentifier,
                             Class<?> nodeClass,
                             CodeBlock objectInitializationCode) {
}
