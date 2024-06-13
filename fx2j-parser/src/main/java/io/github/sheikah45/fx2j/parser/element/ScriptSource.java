package io.github.sheikah45.fx2j.parser.element;

import io.github.sheikah45.fx2j.parser.internal.utils.StringUtils;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;

public sealed interface ScriptSource {

    Charset charset();

    record Inline(String value, Charset charset) implements ScriptSource {
        public Inline {
            Objects.requireNonNull(charset, "charset cannot be null");
            if (StringUtils.isNullOrBlank(value)) {
                throw new IllegalArgumentException("source cannot be blank or null");
            }
        }
    }
    record Reference(Path source, Charset charset) implements ScriptSource {
        public Reference {
            Objects.requireNonNull(charset, "charset cannot be null");
            Objects.requireNonNull(source, "source cannot be null");
        }
    }
}
