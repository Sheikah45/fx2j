plugins {
    id("io.github.sheikah45.fx2j.conventions-publish")
    id("io.github.sheikah45.fx2j.conventions-library")
    id("antlr")
}

dependencies {
    antlr("org.antlr:antlr4:4.13.1")
}

tasks.named("generateGrammarSource", AntlrTask::class) {
    arguments = arguments + listOf("-visitor", "-no-listener")
}

tasks.named("sourcesJar") {
    dependsOn += "generateGrammarSource"
}