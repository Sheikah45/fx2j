plugins {
    `maven-publish`

    id("com.gradle.plugin-publish") version "1.2.1"

    id("io.github.sheikah45.fx2j.conventions-publish")
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

afterEvaluate {
    tasks.named<Sign>("signPluginMavenPublication") {
        this.enabled = false
    }
    tasks.named<PublishToMavenRepository>("publishPluginMavenPublicationToMavenRepository").configure {
        this.enabled = false
    }
    tasks.named<PublishToMavenLocal>("publishPluginMavenPublicationToMavenLocal").configure {
        this.enabled = false
    }

    publishing.publications {
        this.named<MavenPublication>("fx2jPluginPluginMarkerMaven").configure {
            this.pom {
                url.set(project.properties["project_website"].toString())
                issueManagement {
                    system.set("GitHub")
                    url.set(project.properties["project_issues"].toString())
                }
                scm {
                    url.set(project.properties["project_website"].toString())
                    connection.set("scm:git:${project.properties["project_vcs"].toString()}")
                    developerConnection.set("scm:git:git@github.com:sheikah45/fx2j.git")
                }
                licenses {
                    license {
                        name.set("The MIT License (MIT)")
                        url.set("http://www.opensource.org/licenses/mit-license.php")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("Sheikah45")
                        name.set("Sheikah45")
                        organization {
                            name.set("Personal")
                            url.set("https://github.com/Sheikah45")
                        }
                    }
                }
            }
        }
    }

}