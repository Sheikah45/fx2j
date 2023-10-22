package io.github.sheikah45.fx2j.processor.testutils;

import io.github.sheikah45.fx2j.processor.Fx2jCompiler;
import io.github.sheikah45.fx2j.processor.Fx2jProcessor;

import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestCompiler {

    @SuppressWarnings("unchecked")
    public static <T> Class<T> compileAndLoadClass(Path compiledClassOutputPath, Fx2jProcessor fx2jProcessor,
                                                   String className) {
        List<Path> filteredClasspath = getFilteredClasspath();
        Fx2jCompiler.compile(fx2jProcessor, compiledClassOutputPath, filteredClasspath);

        try {
            ClassLoader classLoader = URLClassLoader.newInstance(new URL[]{compiledClassOutputPath.toUri().toURL()});
            if (fx2jProcessor.isModular()) {
                List<Path> classpath = Stream.concat(filteredClasspath.stream(), Stream.of(compiledClassOutputPath))
                                             .toList();

                classLoader = getModuleClassPathLoader(fx2jProcessor.getRootPackage(), classpath, classLoader);
            }

            return (Class<T>) classLoader.loadClass(className);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Path> getFilteredClasspath() {
        String classpathString = System.getProperty("java.class.path");
        return Arrays.stream(classpathString.split(System.getProperty("path.separator")))
                     .filter(path -> path.contains("fx2j") || path.contains("javafx") || path.contains("javapoet"))
                     .map(Path::of)
                     .toList();
    }

    public static ClassLoader getModuleClassPathLoader(String moduleName, Collection<Path> classpath,
                                                       ClassLoader classLoader) {
        ModuleLayer boot = ModuleLayer.boot();
        Configuration parent = boot.configuration();
        ModuleFinder moduleFinder = ModuleFinder.of(classpath.toArray(Path[]::new));

        Set<String> modules = moduleFinder.findAll()
                                          .stream()
                                          .map(ModuleReference::descriptor)
                                          .map(ModuleDescriptor::name)
                                          .collect(Collectors.toSet());

        Configuration configuration = parent.resolveAndBind(moduleFinder, ModuleFinder.of(), modules);
        return boot.defineModulesWithOneLoader(configuration, classLoader).findLoader(moduleName);
    }
}
