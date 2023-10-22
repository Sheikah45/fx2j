plugins {
    publishing
    signing
}

group = "io.github.sheikah45.fx2j"

publishing {
    repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.properties["sonatypeUsername"].toString()
                password = project.properties["sonatypePassword"].toString()
            }
        }
    }
}


signing {
    publishing.publications.configureEach {
        sign(this)
    }
    sign(configurations.archives.get())
}