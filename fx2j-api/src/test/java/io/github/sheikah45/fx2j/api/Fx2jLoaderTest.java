package io.github.sheikah45.fx2j.api;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertThrows;

class Fx2jLoaderTest {

    @Test
    void testLoadNoJavaFxNoBuilder() throws Exception {
        Fx2jLoader fx2jLoader = new Fx2jLoader();
        fx2jLoader.setLocation(URI.create("file://test.fxml").toURL());
        assertThrows(IllegalArgumentException.class, fx2jLoader::load);
    }
}