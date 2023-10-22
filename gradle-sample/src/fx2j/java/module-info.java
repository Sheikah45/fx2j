module fx2j.builder {
    requires io.github.sheikah45.fx2j.api;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires com.example.sample;


    provides io.github.sheikah45.fx2j.api.Fx2jBuilderFinder with fx2j.builder.Fx2jBuilderFinder;
}
