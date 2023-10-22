package io.github.sheikah45.fx2j.processor;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;


/**
 * The Fx2jCompiler class is responsible for compiling Java source files using the Java Compiler API.
 * It requires an Fx2jProcessor, a class output directory, and a collection of class or module paths.
 * The Fx2jProcessor generates the Java source files to be compiled.
 * The class output directory specifies where the compiled class files will be saved.
 * The class or module path is used to resolve dependencies during compilation.
 */
public class Fx2jCompiler {

    /**
     * Compiles Java source files using the specified Fx2jProcessor, class output directory, and class or module path.
     *
     * @param fx2jProcessor        the Fx2jProcessor used for creating JavaFileObjects
     * @param classOutputDirectory the directory where the compiled class files will be saved
     * @param classOrModulePath    the class or module path used for compilation (can be null)
     * @throws RuntimeException if the compilation fails or an exception occurs during the compilation process
     */
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
                throw new RuntimeException("Unable to compile files");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}