plugins {
    id("io.github.sheikah45.fx2j.conventions-publish")
    id("io.github.sheikah45.fx2j.conventions-library")
    id("io.github.sheikah45.fx2j.conventions-javafx")
}

javafx {
    modules = mutableListOf("javafx.fxml")
    configuration = "compileOnly"
}

