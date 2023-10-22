package com.example.sample;

import javafx.scene.control.Label;

public class HelloController {
    public Label welcomeText;

    public void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!\nLoaded by Fx2j!");
    }
}