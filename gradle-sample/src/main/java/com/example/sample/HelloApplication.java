package com.example.sample;

import io.github.sheikah45.fx2j.api.Fx2jLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Fx2jLoader fx2jLoaderLoader = new Fx2jLoader();
        fx2jLoaderLoader.setLocation(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fx2jLoaderLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}