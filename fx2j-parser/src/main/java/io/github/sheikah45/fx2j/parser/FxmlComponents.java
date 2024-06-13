package io.github.sheikah45.fx2j.parser;

import io.github.sheikah45.fx2j.parser.element.DeclarationElement;
import io.github.sheikah45.fx2j.parser.element.FxmlProcessingInstruction;

import java.util.List;
import java.util.Objects;

public record FxmlComponents(DeclarationElement rootNode, List<FxmlProcessingInstruction> rootProcessingInstructions) {
    public FxmlComponents {
        Objects.requireNonNull(rootNode, "rootNode cannot be null");
        Objects.requireNonNull(rootProcessingInstructions, "rootProcessingInstructions cannot be null");
        rootProcessingInstructions = List.copyOf(rootProcessingInstructions);
    }
}
