import io.github.sheikah45.fx2j.api.Fx2jBuilderFinder;

module io.github.sheikah45.fx2j.api {
    requires static javafx.fxml;
    exports io.github.sheikah45.fx2j.api;
    uses Fx2jBuilderFinder;
}