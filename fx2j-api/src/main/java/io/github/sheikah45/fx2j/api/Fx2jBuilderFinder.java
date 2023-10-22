package io.github.sheikah45.fx2j.api;

import java.net.URL;

public interface Fx2jBuilderFinder {

    Fx2jBuilder<?, ?> findBuilder(URL location);

}
