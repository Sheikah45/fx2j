package io.github.sheikah45.fx2j.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import io.github.sheikah45.fx2j.api.Fx2jBuilder;
import io.github.sheikah45.fx2j.api.Fx2jBuilderFinder;
import io.github.sheikah45.fx2j.processor.internal.model.StringJavaFileObject;
import io.github.sheikah45.fx2j.processor.internal.utils.JavaFileUtils;

import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Fx2jProcessor is responsible for generating the final products such as the module-info.java, Fx2jBuilderFinder,
 * and ServiceProvider file.
 */
public class Fx2jProcessor {

    private static final WildcardTypeName OBJECT_WILDCARD = WildcardTypeName.subtypeOf(Object.class);
    private static final ParameterizedTypeName BUILDER_TYPE_NAME = ParameterizedTypeName.get(
            ClassName.get(Fx2jBuilder.class), OBJECT_WILDCARD, OBJECT_WILDCARD);
    private static final WildcardTypeName EXTENDS_BUILDER = WildcardTypeName.subtypeOf(BUILDER_TYPE_NAME);
    private static final ParameterizedTypeName SUPPLIER_TYPE_NAME = ParameterizedTypeName.get(
            ClassName.get(Supplier.class), EXTENDS_BUILDER);
    private static final ParameterizedTypeName MAP_TYPE_NAME = ParameterizedTypeName.get(ClassName.get(HashMap.class),
                                                                                         ClassName.get(String.class),
                                                                                         SUPPLIER_TYPE_NAME);

    private final List<FxmlProcessor> fxmlProcessors;
    private final JavaFile builderFinderJavaFile;
    private final String builderFinderCanonicalClassName;
    private final String rootPackage;
    private final boolean modular;

    /**
     * Constructs a new Fx2jProcessor object.
     *
     * @param fxmlProcessors the list of FxmlProcessors to be considered by the Fx2jProcessor
     * @param modular        a boolean flag indicating whether the Fx2jProcessor should produce a module-info.java
     * @throws IllegalArgumentException if no root package is detected or multiple processor root packages are detected
     */
    public Fx2jProcessor(List<FxmlProcessor> fxmlProcessors, boolean modular) {
        this.fxmlProcessors = List.copyOf(fxmlProcessors);
        this.modular = modular;

        List<String> rootPackages = this.fxmlProcessors.stream().map(FxmlProcessor::getRootPackage).distinct().toList();

        if (rootPackages.isEmpty()) {
            throw new IllegalArgumentException("No root package detected");
        }

        if (rootPackages.size() != 1) {
            throw new IllegalArgumentException("Multiple processor root packages detected: %s".formatted(rootPackages));
        }

        this.rootPackage = rootPackages.getFirst();
        builderFinderJavaFile = JavaFile.builder(this.rootPackage, buildBuilderFinderTypeSpec()).build();
        builderFinderCanonicalClassName = JavaFileUtils.getCanonicalClassName(builderFinderJavaFile);
    }

    private TypeSpec buildBuilderFinderTypeSpec() {
        return TypeSpec.classBuilder(Fx2jBuilderFinder.class.getSimpleName())
                       .addModifiers(Modifier.PUBLIC)
                       .addSuperinterface(Fx2jBuilderFinder.class)
                       .addField(buildLookupFieldSpec())
                       .addStaticBlock(buildInitializationCodeBlock())
                       .addMethod(buildFindMethodSpec())
                       .build();
    }

    private FieldSpec buildLookupFieldSpec() {
        int hashMapSize = (int) Math.ceil(fxmlProcessors.size() / .75);
        return FieldSpec.builder(MAP_TYPE_NAME, "BUILDER_PATH_MAP", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T<>($L, $Lf)", HashMap.class, hashMapSize, .75)
                        .build();
    }

    private CodeBlock buildInitializationCodeBlock() {
        return fxmlProcessors.stream().map(fxmlProcessor -> {
            String urlPath = fxmlProcessor.getRelativeFilePath().toString().replace("\\", "/");
            ClassName builderType = ClassName.bestGuess(fxmlProcessor.getCanonicalClassName());
            return CodeBlock.builder().addStatement("BUILDER_PATH_MAP.put($S, $T::new)", urlPath, builderType).build();
        }).collect(CodeBlock.joining(""));
    }

    private MethodSpec buildFindMethodSpec() {
        return MethodSpec.methodBuilder("findBuilder")
                         .addModifiers(Modifier.PUBLIC)
                         .addParameter(ParameterSpec.builder(URL.class, "location").build())
                         .returns(BUILDER_TYPE_NAME)
                         .beginControlFlow("if (location == null)")
                         .addStatement("return null")
                         .endControlFlow()
                         .addStatement("$T path = location.getPath()", String.class)
                         .addStatement(
                                 "return BUILDER_PATH_MAP.entrySet().stream().filter(entry -> path.endsWith(entry.getKey())).map($T::getValue).map($T::get).findFirst().orElse(null)",
                                 Map.Entry.class, Supplier.class)
                         .build();
    }

    /**
     * Returns the root package of the Fx2jProcessor.
     *
     * @return the root package of the Fx2jProcessor
     */
    public String getRootPackage() {
        return rootPackage;
    }

    /**
     * Writes the source files and resource files generated by the Fx2jProcessor.
     *
     * @param sourceRoot    the path to the root directory of the source files
     * @param resourcesRoot the path to the root directory of the resource files
     * @throws IOException if an I/O error occurs while writing the files
     */
    public void writeSourceFiles(Path sourceRoot, Path resourcesRoot) throws IOException {
        for (FxmlProcessor fxmlProcessor : fxmlProcessors) {
            fxmlProcessor.writeTo(sourceRoot);
        }
        builderFinderJavaFile.writeTo(sourceRoot);
        writeServiceFile(resourcesRoot);

        if (modular) {
            writeModuleInfoFile(sourceRoot);
        }
    }

    private void writeServiceFile(Path resourceRoot) throws IOException {
        Path servicesFile = resourceRoot.resolve("META-INF/services/")
                                        .resolve(Fx2jBuilderFinder.class.getCanonicalName());
        Files.createDirectories(servicesFile.getParent());
        Files.writeString(servicesFile, builderFinderCanonicalClassName);
    }

    private void writeModuleInfoFile(Path sourceRoot) throws IOException {
        Files.writeString(sourceRoot.resolve("module-info.java"), getModuleInfoContent());
    }

    private String getModuleInfoContent() {
        String moduleRequires = fxmlProcessors.stream()
                                              .map(FxmlProcessor::getRequiredModules)
                                              .flatMap(Collection::stream)
                                              .distinct()
                                              .map(module -> "requires " + module + ";")
                                              .collect(Collectors.joining("\n"));

        return """
               module %s {
                   requires io.github.sheikah45.fx2j.api;
               %s
                              
                   provides %s with %s;
               }
               """.formatted(rootPackage, moduleRequires.indent(4), Fx2jBuilderFinder.class.getCanonicalName(),
                             getBuilderFinderCanonicalClassName());
    }

    /**
     * Returns the canonical class name of the BuilderFinder.
     *
     * @return the canonical class name of the BuilderFinder
     */
    public String getBuilderFinderCanonicalClassName() {
        return builderFinderCanonicalClassName;
    }

    /**
     * Creates Java file objects representing the module-info.java and provided FxmlProcessors so they
     * can be used as compilationUnits for the JavaCompiler
     *
     * @return a collection of Java file objects
     */
    public Collection<JavaFileObject> createJavaFileObjects() {
        JavaFileObject moduleInfoFile = null;
        if (modular) {
            moduleInfoFile = new StringJavaFileObject("module-info", getModuleInfoContent());
        }

        Stream<JavaFileObject> builderAndFinderFilesStream = Stream.concat(
                Stream.of(builderFinderJavaFile.toJavaFileObject()),
                fxmlProcessors.stream().map(FxmlProcessor::toJavaFileObject));
        return Stream.concat(Stream.ofNullable(moduleInfoFile), builderAndFinderFilesStream).toList();
    }

    /**
     * Returns whether the processor will output a module-info.java.
     *
     * @return true if the processor will output a module-info.java, false otherwise
     */
    public boolean isModular() {
        return modular;
    }
}
