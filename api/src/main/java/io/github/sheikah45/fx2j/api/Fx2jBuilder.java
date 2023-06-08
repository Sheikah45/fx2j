package io.github.sheikah45.fx2j.api;

import java.util.ResourceBundle;
import java.util.function.Function;

public interface Fx2jBuilder<C, R> {

    void build(C providedController, R providedRoot, ResourceBundle resources,
               Function<Class<?>, Object> controllerFactory);

    C getController();

    R getRoot();
}
