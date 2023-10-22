plugins {
    id("io.github.sheikah45.fx2j.conventions-publish")
    id("io.github.sheikah45.fx2j.conventions-library")
    id("io.github.sheikah45.fx2j.conventions-javafx")
}

dependencies {
    implementation(project(":api"))
    implementation("com.squareup:javapoet:1.13.0")
}

javafx {
    modules = mutableListOf("javafx.fxml", "javafx.controls", "javafx.graphics")
    configuration = "testImplementation"
}