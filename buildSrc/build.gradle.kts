plugins {
    `kotlin-dsl`
}

group = "io.github.sheikah45.fx2j"

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("com.adarshr:gradle-test-logger-plugin:4.0.0")
    implementation("org.openjfx:javafx-plugin:0.1.0")
}