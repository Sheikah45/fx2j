plugins {
    id("io.github.sheikah45.fx2j.conventions-publish")
    id("io.github.sheikah45.fx2j.conventions-library")
}

dependencies {
    implementation(project(":fx2j-api"))
    implementation(project(":fx2j-parser"))
    implementation("com.squareup:javapoet:1.13.0")
}

javafx {
    modules = mutableListOf("javafx.fxml", "javafx.controls", "javafx.graphics")
    configuration = "testImplementation"
}