@SuppressWarnings("JavaModuleNaming")
module io.github.sheikah45.fx2j.parser {
    requires java.xml;
    requires org.antlr.antlr4.runtime;

    exports io.github.sheikah45.fx2j.parser;
    exports io.github.sheikah45.fx2j.parser.attribute;
    exports io.github.sheikah45.fx2j.parser.element;
    exports io.github.sheikah45.fx2j.parser.property;
}