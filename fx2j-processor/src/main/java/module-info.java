module io.github.sheikah45.fx2j.processor {
    requires java.xml;
    requires com.squareup.javapoet;
    requires java.compiler;
    requires io.github.sheikah45.fx2j.api;
    requires io.github.sheikah45.fx2j.parser;

    exports io.github.sheikah45.fx2j.processor;
}