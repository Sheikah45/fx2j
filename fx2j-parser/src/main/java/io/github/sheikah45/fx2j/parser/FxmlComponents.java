package io.github.sheikah45.fx2j.parser;

import java.util.List;
import java.util.Objects;

public record FxmlComponents(FxmlNode rootNode, List<FxmlProcessingInstruction> processingInstructions) {
    public FxmlComponents {
        Objects.requireNonNull(rootNode, "rootNode cannot be null");
        Objects.requireNonNull(processingInstructions, "processingInstructions cannot be null");
        processingInstructions = List.copyOf(processingInstructions);
    }
}
