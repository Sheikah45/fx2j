package io.github.sheikah45.fx2j.parser.element;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;

public record IncludeElement(Path source, Path resources, Charset charset, ElementContent<?, ?> content)
        implements ClassInstanceElement {
    public IncludeElement {
        Objects.requireNonNull(source, "source cannot be null");
        Objects.requireNonNull(content, "content cannot be null");
        Objects.requireNonNull(charset, "charset cannot be null");
    }
}
