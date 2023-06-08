package io.github.sheikah45.fx2j.compiler.internal.model;

import java.util.Set;

public record FxmlComponents(FxmlNode rootNode, Set<String> imports, String controllerType) {
    public FxmlComponents {
        imports = Set.copyOf(imports);
    }
}
