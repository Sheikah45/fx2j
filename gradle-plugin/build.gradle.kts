plugins {
    id("java-gradle-plugin")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":compiler"))

    val junitVersion = "5.10.0"
    testImplementation("org.junit.jupiter:junit-jupiter-params:${junitVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.jar {
    enabled = false
}

tasks.shadowJar {
    archiveBaseName = project.ext["jarName"].toString()
    archiveClassifier = ""
}

tasks.test {
    useJUnitPlatform()
    systemProperties = mapOf("junit.jupiter.execution.parallel.enabled" to true)
}

gradlePlugin {
    website = properties["project_website"].toString()
    vcsUrl = properties["project_vcs"].toString()

    plugins {
        create("fx2jPlugin") {
            id = "io.github.sheikah45.fx2j"
            implementationClass = "io.github.sheikah45.fx2j.gradle.plugin.Fx2jPlugin"
            displayName = properties["project_display_name"].toString()
            description = properties["project_description"].toString()
            tags = listOf("fx2j", "javafx", "fxml")
        }
    }
}