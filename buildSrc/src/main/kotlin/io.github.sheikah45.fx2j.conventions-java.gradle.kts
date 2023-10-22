plugins {
    java
    jacoco
    id("com.adarshr.test-logger")
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

testlogger {
    showSimpleNames = true
    showPassed = false
    showSkipped = false
}