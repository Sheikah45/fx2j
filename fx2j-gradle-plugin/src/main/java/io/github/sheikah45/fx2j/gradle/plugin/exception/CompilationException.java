package io.github.sheikah45.fx2j.gradle.plugin.exception;

import org.gradle.api.GradleException;

public class CompilationException extends GradleException {

    public CompilationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompilationException(String message) {
        super(message);
    }

}
