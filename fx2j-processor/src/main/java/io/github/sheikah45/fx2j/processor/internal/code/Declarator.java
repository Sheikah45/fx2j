package io.github.sheikah45.fx2j.processor.internal.code;

sealed public interface Declarator permits Expression.Variable, StatementExpression.Assignment {}
