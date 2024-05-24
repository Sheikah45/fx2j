package io.github.sheikah45.fx2j.processor.processor;

import io.github.sheikah45.fx2j.api.Fx2jBuilder;
import io.github.sheikah45.fx2j.api.Fx2jBuilderFinder;
import io.github.sheikah45.fx2j.processor.Fx2jProcessor;
import io.github.sheikah45.fx2j.processor.FxmlProcessor;
import io.github.sheikah45.fx2j.processor.testutils.TestCompiler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.module.ModuleDescriptor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
class Fx2jProcessorTest {

    @TempDir
    private Path compileOutput;
    @TempDir
    private Path sourceOutput;

    @Test
    void testCompileFx2j() throws Exception {
        FxmlProcessor fxmlProcessor = new FxmlProcessor(Path.of("src/test/resources/fxml/read/test.fxml"),
                                                        Path.of("src/test/resources"), "test",
                                                        Fx2jProcessorTest.class.getClassLoader()
        );
        Fx2jProcessor fx2jProcessor = new Fx2jProcessor(List.of(fxmlProcessor), false);

        Class<Fx2jBuilderFinder> fx2jFinderBuilderClass = TestCompiler.compileAndLoadClass(compileOutput, fx2jProcessor,
                                                                                           fx2jProcessor.getBuilderFinderCanonicalClassName());
        Fx2jBuilderFinder finder = fx2jFinderBuilderClass.getConstructor().newInstance();

        Fx2jBuilder<?, ?> builder = finder.findBuilder(new URL("file", "", "/fxml/read/test.fxml"));
        assertNotNull(builder);
        assertEquals("test.fxml.read.TestBuilder", builder.getClass().getCanonicalName());
        assertNull(finder.findBuilder(new URL("file", "", "/test.fxml")));
    }

    @Test
    void testCompileFx2jAbsolutePath() throws Exception {
        FxmlProcessor fxmlProcessor = new FxmlProcessor(
                Path.of("src/test/resources/fxml/read/test.fxml").toAbsolutePath(),
                Path.of("src/test/resources"), "test",
                Fx2jProcessorTest.class.getClassLoader()
        );
        Fx2jProcessor fx2jProcessor = new Fx2jProcessor(List.of(fxmlProcessor), false);

        Class<Fx2jBuilderFinder> fx2jFinderBuilderClass = TestCompiler.compileAndLoadClass(compileOutput, fx2jProcessor,
                                                                                           fx2jProcessor.getBuilderFinderCanonicalClassName());
        Fx2jBuilderFinder finder = fx2jFinderBuilderClass.getConstructor().newInstance();

        Fx2jBuilder<?, ?> builder = finder.findBuilder(new URL("file", "", "/fxml/read/test.fxml"));
        assertNotNull(builder);
        assertEquals("test.fxml.read.TestBuilder", builder.getClass().getCanonicalName());
        assertNull(finder.findBuilder(new URL("file", "", "/test.fxml")));
    }

    @Test
    void testCompileFx2jModularized() throws Exception {
        ClassLoader loader = createApiModuleClassLoader();

        FxmlProcessor fxmlProcessor = new FxmlProcessor(Path.of("src/test/resources/fxml/read/test.fxml"),
                                                        Path.of("src/test/resources"), "test", loader
        );
        Fx2jProcessor fx2jProcessor = new Fx2jProcessor(List.of(fxmlProcessor), true);

        Class<Fx2jBuilderFinder> fx2jFinderBuilderClass = TestCompiler.compileAndLoadClass(compileOutput, fx2jProcessor,
                                                                                           "test.Fx2jBuilderFinder");
        assertNotNull(fx2jFinderBuilderClass);

        Module module = fx2jFinderBuilderClass.getModule();

        assertTrue(module.isNamed());
        assertEquals("test", module.getName());

        ModuleDescriptor descriptor = module.getDescriptor();

        Set<String> requires = descriptor.requires()
                                         .stream()
                                         .map(ModuleDescriptor.Requires::name)
                                         .collect(Collectors.toSet());

        assertEquals(5, requires.size());
        assertTrue(requires.contains("io.github.sheikah45.fx2j.api"));
        assertTrue(requires.contains("javafx.graphics"));
        assertTrue(requires.contains("javafx.controls"));
        assertTrue(requires.contains("javafx.base"));
        assertTrue(requires.contains("java.base"));

        Set<String> packages = descriptor.packages();
        assertEquals(2, packages.size());
        assertTrue(packages.contains("test"));
        assertTrue(packages.contains("test.fxml.read"));

        Set<ModuleDescriptor.Provides> provides = descriptor.provides();
        assertEquals(1, provides.size());

        ModuleDescriptor.Provides provider = provides.stream().findFirst().orElseThrow();
        assertEquals(Fx2jBuilderFinder.class.getCanonicalName(), provider.service());

        List<String> providers = provider.providers();
        assertEquals(1, providers.size());
        assertTrue(providers.contains("test.Fx2jBuilderFinder"));
    }

    private static ClassLoader createApiModuleClassLoader() {
        List<Path> classpath = TestCompiler.getFilteredClasspath();

        URL[] classpathURLs = classpath.stream().map(Path::toUri).map(uri -> {
            try {
                return uri.toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toArray(URL[]::new);
        URLClassLoader classLoader = new URLClassLoader(classpathURLs);
        return TestCompiler.getModuleClassPathLoader("io.github.sheikah45.fx2j.api", classpath, classLoader);
    }

    @Test
    void testWriteFx2j() throws Exception {
        FxmlProcessor fxmlProcessor = new FxmlProcessor(Path.of("src/test/resources/fxml/read/test.fxml"),
                                                        Path.of("src/test/resources"), "test",
                                                        Fx2jProcessorTest.class.getClassLoader()
        );
        Fx2jProcessor fx2jProcessor = new Fx2jProcessor(List.of(fxmlProcessor), true);

        fx2jProcessor.writeSourceFiles(sourceOutput.resolve("java"), sourceOutput.resolve("resources"));

        assertTrue(Files.exists(sourceOutput.resolve("java/test/fxml/read/TestBuilder.java")));
        assertTrue(Files.exists(sourceOutput.resolve("java/test/Fx2jBuilderFinder.java")));
        assertTrue(Files.exists(sourceOutput.resolve("java/module-info.java")));
        assertTrue(Files.exists(sourceOutput.resolve("resources/META-INF/services/")
                                            .resolve(Fx2jBuilderFinder.class.getCanonicalName())));
    }
}
