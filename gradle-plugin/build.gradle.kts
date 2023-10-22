plugins {
    id("java-gradle-plugin")
    id("com.github.johnrengelman.shadow") version "8.1.1"

    id("io.github.sheikah45.fx2j.conventions-publish")
    id("io.github.sheikah45.fx2j.conventions-java")
}
dependencies {
    implementation(project(":processor"))
}

tasks.jar {
    enabled = false
}

tasks.shadowJar {
    archiveBaseName.set("${rootProject.name}-${project.name}")
    archiveClassifier.set("")
}

gradlePlugin {
    website.set(properties["project_website"].toString())
    vcsUrl.set(properties["project_vcs"].toString())

    plugins {
        create("fx2jPlugin") {
            id = "io.github.sheikah45.fx2j"
            implementationClass = "io.github.sheikah45.fx2j.gradle.plugin.Fx2jPlugin"
            displayName = properties["project_display_name"].toString()
            description = properties["project_description"].toString()
            tags.set(listOf("fx2j", "javafx", "fxml"))
        }
    }
}