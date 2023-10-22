module com.example.sample {
    requires javafx.controls;
    requires io.github.sheikah45.fx2j.api;


    opens com.example.sample to javafx.fxml;
    exports com.example.sample;
}