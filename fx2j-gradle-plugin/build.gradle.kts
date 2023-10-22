plugins {
    id("com.gradle.plugin-publish") version "1.2.1"

    id("io.github.sheikah45.fx2j.conventions-repository")
    id("io.github.sheikah45.fx2j.conventions-java")
}

group = "io.github.sheikah45.fx2j"

dependencies {
    implementation(project(":fx2j-processor"))
    implementation(gradleApi())
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
            tags.set(listOf("fx2j", "javafx", "fxml", "aot"))
        }
    }
}
