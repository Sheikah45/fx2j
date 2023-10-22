import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

plugins {
    `maven-publish`
    java

    id("io.github.sheikah45.fx2j.conventions-repository")
}

val buildTimeAndDate: OffsetDateTime by lazy {
    OffsetDateTime.now()
}
val buildDate: String by lazy {
    DateTimeFormatter.ofPattern("yyyy-MM-dd").format(buildTimeAndDate)
}
val buildTime: String by lazy {
    DateTimeFormatter.ofPattern("HH:mm:ss.SSSZ").format(buildTimeAndDate)
}

tasks.withType(Jar::class.java) {
    manifest {
        attributes(
            "Created-By" to "${System.getProperty("java.version")} (${System.getProperty("java.vendor")} ${
                System.getProperty(
                    "java.vm.version"
                )
            })",
            "Build-Date" to buildDate,
            "Build-Time" to buildTime,
            "Specification-Title" to project.name,
            "Specification-Version" to project.version,
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
        )
    }
    metaInf {
        from(files(rootProject.rootDir)) {
            include("LICENSE*")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set(project.properties["project_display_name"].toString())
                description.set(project.properties["project_description"].toString())
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