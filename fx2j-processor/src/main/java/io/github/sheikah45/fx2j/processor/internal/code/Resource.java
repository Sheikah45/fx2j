package io.github.sheikah45.fx2j.processor.internal.code;

sealed public interface Resource permits Expression.Variable, Resource.ResourceDeclaration {
    record ResourceDeclaration(TypeValue.Declarable type, String identifier, Expression initializer)
            implements Resource {}
}
