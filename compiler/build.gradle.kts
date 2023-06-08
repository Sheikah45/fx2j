plugins {
    id("java-library")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.adarshr.test-logger") version "3.2.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":api"))
    implementation("com.squareup:javapoet:1.13.0")

    val junitVersion = "5.10.0"
    testImplementation("org.junit.jupiter:junit-jupiter-params:${junitVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

javafx {
    version = "21"
    modules = mutableListOf("javafx.fxml", "javafx.controls", "javafx.graphics")
    configuration = "testImplementation"
}

tasks.jar {
    archiveBaseName = project.ext["jarName"].toString()
}

tasks.test {
    useJUnitPlatform()
    systemProperties = mapOf("junit.jupiter.execution.parallel.enabled" to true)
}

testlogger {
    showSimpleNames = true
    showPassed = false
    showSkipped = false
}
