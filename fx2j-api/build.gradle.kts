plugins {
    id("io.github.sheikah45.fx2j.conventions-publish")
    id("io.github.sheikah45.fx2j.conventions-library")
}

javafx {
    modules = listOf("javafx.fxml")
    configuration = "compileOnly"
}

