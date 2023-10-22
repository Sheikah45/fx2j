plugins {
    java
    jacoco
    id("com.adarshr.test-logger")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    val junitVersion = "5.10.0"
    testImplementation("org.junit.jupiter:junit-jupiter-params:${junitVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.test {
    useJUnitPlatform()
    systemProperties = mapOf("junit.jupiter.execution.parallel.enabled" to true)
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}

tasks.withType(Javadoc::class.java) {
    options.outputLevel = JavadocOutputLevel.QUIET
}

testlogger {
    showSimpleNames = true
    showPassed = false
    showSkipped = false
}