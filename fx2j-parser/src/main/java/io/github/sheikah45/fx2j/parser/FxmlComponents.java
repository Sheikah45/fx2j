package io.github.sheikah45.fx2j.parser;

import java.util.Map;
import java.util.Set;

public record FxmlComponents(FxmlNode rootNode, Map<String, Set<String>> processingInstructions) {
    public FxmlComponents {
        processingInstructions = Map.copyOf(processingInstructions);
    }
}
