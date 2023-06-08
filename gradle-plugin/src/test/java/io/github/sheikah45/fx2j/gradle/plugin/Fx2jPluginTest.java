package io.github.sheikah45.fx2j.gradle.plugin;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class Fx2jPluginTest {

    @TempDir
    private Path projectPath;
    private Path buildGradlePath;

    @BeforeEach
    public void setup() throws IOException {
        buildGradlePath = projectPath.resolve("build.gradle");
        Files.writeString(buildGradlePath, """
                                            plugins {
                                                id 'java'
                                                id 'io.github.sheikah45.fx2j'
                                                id 'org.openjfx.javafxplugin' version '0.1.0'
                                            }
                                           """, StandardOpenOption.CREATE_NEW);
    }

    @Test
    public void testPluginNoSource() {
        BuildResult result = GradleRunner.create()
                                         .withProjectDir(projectPath.toFile())
                                         .withArguments("compileFx2j")
                                         .withPluginClasspath()
                                         .forwardOutput()
                                         .build();

        BuildTask compileFxml = result.task(":compileFx2j");
        assertNotNull(compileFxml);
        assertEquals(TaskOutcome.NO_SOURCE, compileFxml.getOutcome());
    }

    @Test
    public void testPluginNonStrictAllFail() throws IOException {
        copyResourcesToProject("main", "/fxml/bad.fxml");

        BuildResult result = GradleRunner.create()
                                         .withProjectDir(projectPath.toFile())
                                         .withArguments("compileFx2j")
                                         .withPluginClasspath()
                                         .forwardOutput()
                                         .build();

        BuildTask compileFxml = result.task(":compileFx2j");
        assertNotNull(compileFxml);
        assertEquals(TaskOutcome.SUCCESS, compileFxml.getOutcome());
        assertFalse(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder/fxml/BadBuilder.java")));
        assertFalse(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder/Fx2jBuilderFinder.java")));
    }

    @Test
    public void testPluginNonStrictSomeFail() throws IOException {
        copyResourcesToProject("main", "/fxml/bad.fxml", "/fxml/double.fxml");

        BuildResult result = GradleRunner.create()
                                         .withProjectDir(projectPath.toFile())
                                         .withArguments("compileFx2j")
                                         .withPluginClasspath()
                                         .forwardOutput()
                                         .build();

        BuildTask compileFxml = result.task(":compileFx2j");
        assertNotNull(compileFxml);
        assertEquals(TaskOutcome.SUCCESS, compileFxml.getOutcome());
        assertFalse(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder/fxml/BadBuilder.java")));
        assertTrue(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder//fxml/DoubleBuilder.java")));
        assertTrue(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder/Fx2jBuilderFinder.java")));
    }

    private void copyResourcesToProject(String sourceSet, String... resources) throws IOException {
        for (String resource : resources) {
            InputStream inputStream = Objects.requireNonNull(Fx2jPluginTest.class.getResourceAsStream(resource));
            Path projectResourcePath = projectPath.resolve("src")
                                                  .resolve(sourceSet)
                                                  .resolve("resources")
                                                  .resolve(resource.substring(1));
            Files.createDirectories(projectResourcePath.getParent());
            Files.copy(inputStream, projectResourcePath);
        }
    }

    @Test
    public void testPluginStrict() throws IOException {
        Files.writeString(buildGradlePath, """
                                            fx2j {
                                                strict = true
                                            }
                                           """, StandardOpenOption.APPEND);

        copyResourcesToProject("main", "/fxml/bad.fxml");

        BuildResult result = GradleRunner.create()
                                         .withProjectDir(projectPath.toFile())
                                         .withArguments("compileFx2j")
                                         .withPluginClasspath()
                                         .forwardOutput()
                                         .buildAndFail();

        BuildTask compileFxml = result.task(":compileFx2j");
        assertNotNull(compileFxml);
        assertEquals(TaskOutcome.FAILED, compileFxml.getOutcome());
        assertFalse(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder/fxml/BadBuilder.java")));
        assertFalse(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder/Fx2jBuilderFinder.java")));
    }

    @Test
    public void testPluginBuilderNameCollision() throws IOException {
        Files.writeString(buildGradlePath, """
                                            fx2j {
                                                strict = true
                                            }
                                           """, StandardOpenOption.APPEND);

        copyResourcesToProject("main", "/fxml/file-1.fxml", "/fxml/file_1.fxml");

        BuildResult result = GradleRunner.create()
                                         .withProjectDir(projectPath.toFile())
                                         .withArguments("compileFx2j")
                                         .withPluginClasspath()
                                         .forwardOutput()
                                         .buildAndFail();

        BuildTask compileFxml = result.task(":compileFx2j");
        assertNotNull(compileFxml);
        assertEquals(TaskOutcome.FAILED, compileFxml.getOutcome());
        assertFalse(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder/fxml/File1Builder.java")));
    }

    @Test
    public void testPluginSource() throws IOException {
        copyResourcesToProject("main", "/fxml/integer.fxml");

        BuildResult result = GradleRunner.create()
                                         .withProjectDir(projectPath.toFile())
                                         .withArguments("compileFx2j")
                                         .withPluginClasspath()
                                         .forwardOutput()
                                         .build();

        BuildTask compileFxml = result.task(":compileFx2j");
        assertNotNull(compileFxml);
        assertEquals(TaskOutcome.SUCCESS, compileFxml.getOutcome());
        assertTrue(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder/fxml/IntegerBuilder.java")));
        assertTrue(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder/Fx2jBuilderFinder.java")));
    }

    @Test
    public void testExcludes() throws IOException {
        Files.writeString(buildGradlePath, """
                                            fx2j {
                                                excludes.add("fxml/double.fxml")
                                            }
                                           """, StandardOpenOption.APPEND);

        copyResourcesToProject("main", "/fxml/integer.fxml", "/fxml/double.fxml");

        BuildResult result = GradleRunner.create()
                                         .withProjectDir(projectPath.toFile())
                                         .withArguments("compileFx2j")
                                         .withPluginClasspath()
                                         .forwardOutput()
                                         .build();

        BuildTask compileFxml = result.task(":compileFx2j");
        assertNotNull(compileFxml);
        assertEquals(TaskOutcome.SUCCESS, compileFxml.getOutcome());
        assertTrue(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder/fxml/IntegerBuilder.java")));
        assertFalse(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder/fxml/DoubleBuilder.java")));
        assertTrue(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder/Fx2jBuilderFinder.java")));
    }

    @Test
    public void testIncludes() throws IOException {
        Files.writeString(buildGradlePath, """
                                            fx2j {
                                                includes.add("fxml/integer.fxml")
                                            }
                                           """, StandardOpenOption.APPEND);

        copyResourcesToProject("main", "/fxml/integer.fxml", "/fxml/double.fxml");

        BuildResult result = GradleRunner.create()
                                         .withProjectDir(projectPath.toFile())
                                         .withArguments("compileFx2j")
                                         .withPluginClasspath()
                                         .forwardOutput()
                                         .build();

        BuildTask compileFxml = result.task(":compileFx2j");
        assertNotNull(compileFxml);
        assertEquals(TaskOutcome.SUCCESS, compileFxml.getOutcome());
        assertTrue(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder/fxml/IntegerBuilder.java")));
        assertFalse(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder/fxml/DoubleBuilder.java")));
        assertTrue(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder/Fx2jBuilderFinder.java")));
    }

    @Test
    public void testSourceSet() throws IOException {
        Files.writeString(buildGradlePath, """
                                            sourceSets {
                                                other
                                            }
                                            
                                            fx2j {
                                                baseSourceSetName = "other"
                                            }
                                           """, StandardOpenOption.APPEND);

        copyResourcesToProject("main", "/fxml/double.fxml");
        copyResourcesToProject("other", "/fxml/integer.fxml");

        BuildResult result = GradleRunner.create()
                                         .withProjectDir(projectPath.toFile())
                                         .withArguments("compileFx2j")
                                         .withPluginClasspath()
                                         .forwardOutput()
                                         .build();

        BuildTask compileFxml = result.task(":compileFx2j");
        assertNotNull(compileFxml);
        assertEquals(TaskOutcome.SUCCESS, compileFxml.getOutcome());
        assertTrue(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder/fxml/IntegerBuilder.java")));
        assertFalse(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder/fxml/DoubleBuilder.java")));
        assertTrue(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/builder/Fx2jBuilderFinder.java")));
    }

    @Test
    public void testBasePackage() throws IOException {
        Files.writeString(buildGradlePath, """
                                            fx2j {
                                                basePackage = "fx2j.extended.builder"
                                            }
                                           """, StandardOpenOption.APPEND);

        copyResourcesToProject("main", "/fxml/integer.fxml");

        BuildResult result = GradleRunner.create()
                                         .withProjectDir(projectPath.toFile())
                                         .withArguments("compileFx2j")
                                         .withPluginClasspath()
                                         .forwardOutput()
                                         .build();

        BuildTask compileFxml = result.task(":compileFx2j");
        assertNotNull(compileFxml);
        assertEquals(TaskOutcome.SUCCESS, compileFxml.getOutcome());
        assertTrue(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/extended/builder/fxml/IntegerBuilder.java")));
        assertTrue(Files.exists(projectPath.resolve("src/fx2j/java/fx2j/extended/builder/Fx2jBuilderFinder.java")));
    }

    @AfterEach
    public void teardown() throws IOException {
        try (Stream<Path> files = Files.walk(projectPath)) {
            files.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

}
