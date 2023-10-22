package io.github.sheikah45.fx2j.processor;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class Fx2jCompiler {

    public static void compile(Fx2jProcessor fx2jProcessor, Path classOutputDirectory,
                               Collection<Path> classOrModulePath) {
        Collection<JavaFileObject> compilationUnits = fx2jProcessor.createJavaFileObjects();

        try {
            Files.createDirectories(classOutputDirectory);
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager standardJavaFileManager = compiler.getStandardFileManager(null, null, null);
            standardJavaFileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(classOutputDirectory.toFile()));
            if (classOrModulePath != null) {
                StandardLocation location = fx2jProcessor.isModular() ?
                                            StandardLocation.MODULE_PATH :
                                            StandardLocation.CLASS_PATH;
                standardJavaFileManager.setLocationFromPaths(location, classOrModulePath);
            }

            JavaCompiler.CompilationTask compilationTask = compiler.getTask(null, standardJavaFileManager, null, null,
                                                                            null, compilationUnits);
            Boolean success = compilationTask.call();
            if (!Boolean.TRUE.equals(success)) {
                throw new RuntimeException();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
