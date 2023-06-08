package io.github.sheikah45.fx2j.gradle.plugin.exception;

import org.gradle.api.GradleException;

public class ClassNameCollisionException extends GradleException {

    public ClassNameCollisionException(String message) {
        super(message);
    }

}
