package io.github.sheikah45.fx2j.gradle.plugin;

import io.github.sheikah45.fx2j.compiler.processor.Fx2jProcessor;
import io.github.sheikah45.fx2j.compiler.processor.FxmlProcessor;
import io.github.sheikah45.fx2j.compiler.processor.ProcessorException;
import io.github.sheikah45.fx2j.gradle.plugin.exception.ClassNameCollisionException;
import io.github.sheikah45.fx2j.gradle.plugin.exception.CompilationException;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.CompileClasspath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CompileFx2jTask extends DefaultTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompileFx2jTask.class);

    @TaskAction
    public void compileFxml() throws IOException {
        Set<File> classpathFiles = getClassPathFiles().getFiles();

        URL[] classpath = classpathFiles.stream().map(File::toURI).map(uri -> {
            try {
                return uri.toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toArray(URL[]::new);

        Path sourcePath = getSourceOutputDirectory().get().getAsFile().toPath();
        Files.createDirectories(sourcePath);
        Path resourceDirectory = getGeneratedResourceDirectory().get().getAsFile().toPath();
        Files.createDirectories(resourceDirectory);

        try (Stream<Path> sourceFiles = Files.walk(sourcePath);
             Stream<Path> resourceFiles = Files.walk(resourceDirectory)) {
            sourceFiles.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);

            resourceFiles.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }

        List<FxmlProcessor> processors = new ArrayList<>();

        String rootPackage = getFxmlBuilderPackage().get();

        boolean modularizeIfPossible = getModularizeIfPossible().get();
        try (URLClassLoader urlClassLoader = new URLClassLoader(classpath, getClass().getClassLoader())) {

            ClassLoader loader = urlClassLoader;
            Boolean strict = getStrict().get();
            if (modularizeIfPossible) {
                try {
                    ModuleLayer boot = ModuleLayer.boot();
                    Configuration parent = boot.configuration();
                    ModuleFinder finder = ModuleFinder.of(
                            classpathFiles.stream().map(File::toPath).toArray(Path[]::new));
                    Set<String> modules = finder.findAll()
                                                .stream()
                                                .map(ModuleReference::descriptor)
                                                .map(ModuleDescriptor::name)
                                                .collect(Collectors.toSet());
                    Configuration cf = parent.resolveAndBind(finder, ModuleFinder.of(), modules);
                    loader = boot.defineModulesWithOneLoader(cf, urlClassLoader)
                                 .findLoader(modules.stream().findFirst().orElseThrow());
                } catch (Exception exception) {
                    if (strict) {
                        throw new CompilationException("Unable to create modular class loader", exception);
                    }

                    LOGGER.warn("Unable to create modular class loader");
                    LOGGER.info("", exception);
                    modularizeIfPossible = false;
                }
            }

            int failed = 0;
            int numFxmlFiles = 0;
            for (File file : getFxmlFiles().getFiles()) {
                try {
                    numFxmlFiles++;

                    processors.add(new FxmlProcessor(file.toPath(),
                                                     getFxmlResourceDirectory().get().getAsFile().toPath(), rootPackage,
                                                     loader
                    ));

                    LOGGER.debug("Compiled {}", file);
                } catch (Exception exception) {
                    failed++;
                    LOGGER.warn("Failed to compile `%s` due to %s".formatted(file, exception.getMessage()));
                    if (exception instanceof ProcessorException processorException) {
                        LOGGER.info("Failed node: \n {}", processorException.getNode());
                        LOGGER.debug("Caused by:", processorException.getCause());
                    } else {
                        LOGGER.debug("Caused by:", exception);
                    }
                }
            }
            LOGGER.info("{} out of {} files compiled", numFxmlFiles - failed, numFxmlFiles);

            if (Boolean.TRUE.equals(strict) && failed != 0) {
                throw new CompilationException(
                        "Some files failed to be compiled and the strict option is selected. See the log output for more details");
            }

            List<String> classNameCollisions = processors.stream()
                                                         .collect(Collectors.groupingBy(
                                                                 FxmlProcessor::getCanonicalClassName))
                                                         .values()
                                                         .stream()
                                                         .filter(groupedResults -> groupedResults.size() > 1)
                                                         .flatMap(Collection::stream)
                                                         .map(processor -> "File: %s ClassName: %s".formatted(
                                                                 processor.getRelativeFilePath(),
                                                                 processor.getCanonicalClassName()))
                                                         .toList();

            if (!classNameCollisions.isEmpty()) {
                throw new ClassNameCollisionException(
                        "Following files resolve to the same class: %s".formatted(classNameCollisions));
            }
        }

        if (processors.isEmpty()) {
            return;
        }

        Fx2jProcessor fx2jProcessor = new Fx2jProcessor(processors, modularizeIfPossible);
        fx2jProcessor.writeSourceFiles(sourcePath, resourceDirectory);
    }

    @InputFiles
    @CompileClasspath
    public FileCollection getClassPathFiles() {
        return getProject().getLayout()
                           .files(getInputSourceSet().map(sourceSet -> List.of(sourceSet.getCompileClasspath(),
                                                                               sourceSet.getOutput()
                                                                                        .getClassesDirs())));
    }

    @Internal
    public Provider<Directory> getGeneratedResourceDirectory() {
        return getProject().getLayout()
                           .dir(getOutputSourceSet().map(SourceSet::getResources)
                                                    .map(SourceDirectorySet::getSourceDirectories)
                                                    .map(FileCollection::getSingleFile));
    }

    @Input
    public abstract Property<String> getFxmlBuilderPackage();

    @Internal
    public abstract Property<SourceSet> getInputSourceSet();

    @Internal
    public abstract Property<SourceSet> getOutputSourceSet();

    @InputFiles
    @SkipWhenEmpty
    public FileCollection getFxmlFiles() {
        Provider<List<Path>> fxmlFiles = getInputSourceSet().map(SourceSet::getResources)
                                                            .map(SourceDirectorySet::getFiles)
                                                            .flatMap(this::filterForMatchingFxmlFiles);
        return getProject().getLayout().files(fxmlFiles);
    }

    @Input
    public abstract Property<Boolean> getStrict();

    @Input
    public abstract Property<Boolean> getModularizeIfPossible();

    private Provider<List<Path>> filterForMatchingFxmlFiles(Collection<File> files) {
        return getFxmlResourceDirectory().map(Directory::getAsFile)
                                         .map(File::toPath)
                                         .flatMap(resources -> filterFxmlFilesRelativeToResources(resources, files));
    }

    private Provider<List<Path>> filterFxmlFilesRelativeToResources(Path resources, Collection<File> fxmlFiles) {
        List<Path> relativeFxmlPaths = fxmlFiles.stream()
                                                .filter(file -> file.getName().endsWith(".fxml"))
                                                .map(File::toPath)
                                                .map(resources::relativize)
                                                .toList();
        return getIncludesPathMatchers().map(includes -> {
            if (includes.isEmpty()) {
                return relativeFxmlPaths;
            } else {
                return relativeFxmlPaths.stream()
                                        .filter(path -> includes.stream()
                                                                .anyMatch(pathMatcher -> pathMatcher.matches(path)))
                                        .toList();
            }
        }).flatMap(includedFxmlPaths -> getExcludesPathMatchers().map(excludes -> {
            if (excludes.isEmpty()) {
                return includedFxmlPaths;
            } else {
                return includedFxmlPaths.stream()
                                        .filter(path -> excludes.stream()
                                                                .noneMatch(pathMatcher -> pathMatcher.matches(path)))
                                        .toList();
            }
        })).map(paths -> paths.stream().map(resources::resolve).toList());
    }

    @Internal
    public Provider<Directory> getFxmlResourceDirectory() {
        return getProject().getLayout()
                           .dir(getInputSourceSet().map(SourceSet::getResources)
                                                   .map(SourceDirectorySet::getSourceDirectories)
                                                   .map(FileCollection::getSingleFile));
    }

    @Internal
    public Provider<Set<PathMatcher>> getIncludesPathMatchers() {
        return getIncludes().map(this::convertToGlobPathMatchers);
    }

    @Internal
    public Provider<Set<PathMatcher>> getExcludesPathMatchers() {
        return getExcludes().map(this::convertToGlobPathMatchers);
    }

    @Input
    public abstract SetProperty<String> getIncludes();

    private Set<PathMatcher> convertToGlobPathMatchers(Set<String> patterns) {
        FileSystem fileSystem = FileSystems.getDefault();
        return patterns.stream()
                       .map(pattern -> "glob:" + pattern)
                       .map(fileSystem::getPathMatcher)
                       .collect(Collectors.toSet());
    }

    @OutputDirectory
    public Provider<Directory> getSourceOutputDirectory() {
        return getProject().getLayout()
                           .dir(getOutputSourceSet().map(SourceSet::getAllJava)
                                                    .map(SourceDirectorySet::getSourceDirectories)
                                                    .map(FileCollection::getSingleFile));
    }

    @OutputDirectory
    public Provider<Directory> getClassOutputDirectory() {
        return getProject().getLayout()
                           .dir(getOutputSourceSet().map(SourceSet::getOutput)
                                                    .map(SourceSetOutput::getClassesDirs)
                                                    .map(FileCollection::getSingleFile));
    }

    @Input
    public abstract SetProperty<String> getExcludes();
}
