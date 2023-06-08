plugins {
    id("java-library")
    id("org.openjfx.javafxplugin") version "0.1.0"
}


repositories {
    mavenCentral()
}

tasks.jar {
    archiveBaseName = project.ext["jarName"].toString()
}

tasks.test {
    useJUnitPlatform()
}

javafx {
    version = "21"
    modules = mutableListOf("javafx.fxml")
    configuration = "compileOnly"
}

dependencies {
    val junitVersion = "5.10.0"
    testImplementation("org.junit.jupiter:junit-jupiter-params:${junitVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

