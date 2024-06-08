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
import io.github.sheikah45.fx2j.parser.FxmlComponents;
import io.github.sheikah45.fx2j.parser.FxmlParser;
import io.github.sheikah45.fx2j.parser.FxmlProcessingInstruction;
import io.github.sheikah45.fx2j.parser.attribute.ControllerAttribute;
import io.github.sheikah45.fx2j.processor.internal.ObjectNodeProcessor;
import io.github.sheikah45.fx2j.processor.internal.model.ObjectNodeCode;
import io.github.sheikah45.fx2j.processor.internal.resolve.MethodResolver;
import io.github.sheikah45.fx2j.processor.internal.resolve.ResolverContainer;
import io.github.sheikah45.fx2j.processor.internal.resolve.TypeResolver;
import io.github.sheikah45.fx2j.processor.internal.utils.CodeBlockUtils;
import io.github.sheikah45.fx2j.processor.internal.utils.JavaFileUtils;
import io.github.sheikah45.fx2j.processor.internal.utils.StringUtils;

import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The FxmlProcessor class is responsible for processing an FXML file and generating Java code for it.
 * The generated Java code implements the Fx2jBuilder interface, which is a builder for JavaFX nodes.
 */
public class FxmlProcessor {

    public static final String CONTROLLER_NAME = "controller";
    public static final String RESOURCES_NAME = "resources";
    public static final String CONTROLLER_FACTORY_NAME = "controllerFactory";
    public static final String BUILDER_PROVIDED_CONTROLLER_NAME = "builderProvidedController";
    public static final String BUILDER_PROVIDED_ROOT_NAME = "builderProvidedRoot";

    private final TypeResolver typeResolver;
    private final MethodResolver methodResolver;
    private final String rootPackage;
    private final Class<?> controllerClass;
    private final JavaFile javaFile;
    private final Path relativeFilePath;
    private final String canonicalClassName;
    private final ObjectNodeCode objectNodeCode;
    private final Class<?> rootClass;

    /**
     * FxmlProcessor is responsible for processing an FXML file and generating the corresponding Java code.
     *
     * @param filePath         The path to the FXML file to be processed.
     * @param resourceRootPath The root path to the resources used by the FXML file.
     * @param rootPackage      The root package for the generated Java code.
     * @param classLoader      The class loader to use for resolving imported classes.
     */
    public FxmlProcessor(Path filePath, Path resourceRootPath, String rootPackage, ClassLoader classLoader) {
        this.rootPackage = rootPackage;
        Path absoluteFilePath = filePath.toAbsolutePath();
        FxmlComponents fxmlComponents = FxmlParser.readFxml(absoluteFilePath);
        Set<String> imports = fxmlComponents.rootProcessingInstructions()
                                            .stream()
                                            .filter(FxmlProcessingInstruction.Import.class::isInstance)
                                            .map(FxmlProcessingInstruction.Import.class::cast)
                                            .map(FxmlProcessingInstruction.Import::value)
                                            .collect(Collectors.toSet());

        ResolverContainer resolverContainer = ResolverContainer.from(imports, classLoader);
        typeResolver = resolverContainer.getTypeResolver();
        methodResolver = resolverContainer.getMethodResolver();

        controllerClass = fxmlComponents.rootNode()
                                        .content()
                                        .attributes()
                                        .stream()
                                        .filter(ControllerAttribute.class::isInstance)
                                        .map(ControllerAttribute.class::cast)
                                        .map(ControllerAttribute::className)
                                        .findFirst()
                                        .or(() -> fxmlComponents.rootProcessingInstructions()
                                                                .stream()
                                                                .filter(FxmlProcessingInstruction.Custom.class::isInstance)
                                                                .map(FxmlProcessingInstruction.Custom.class::cast)
                                                                .filter(custom -> "fx2jControllerType".equals(
                                                                        custom.name()))
                                                                .map(FxmlProcessingInstruction.Custom::value)
                                                                .findFirst())
                                        .map(typeResolver::resolve)
                                        .orElse(Object.class);

        Path absoluteResourceRootPath = resourceRootPath.toAbsolutePath();
        objectNodeCode = new ObjectNodeProcessor(fxmlComponents.rootNode(), controllerClass, resolverContainer,
                                                 absoluteFilePath, absoluteResourceRootPath,
                                                 this.rootPackage).getNodeCode();
        rootClass = typeResolver.wrapType(objectNodeCode.nodeClass());

        relativeFilePath = absoluteResourceRootPath.relativize(absoluteFilePath);
        String relativePackage = StringUtils.fxmlFileToPackageName(relativeFilePath);
        String packageName = rootPackage.isBlank() ? relativePackage : "%s.%s".formatted(rootPackage, relativePackage);
        javaFile = JavaFile.builder(packageName, generateTypeSpec()).build();
        canonicalClassName = JavaFileUtils.getCanonicalClassName(javaFile);
    }

    private TypeSpec generateTypeSpec() {
        ParameterizedTypeName fx2jSuperType = ParameterizedTypeName.get(Fx2jBuilder.class, controllerClass, rootClass);
        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(StringUtils.fxmlFileToBuilderClass(relativeFilePath))
                                                   .addSuperinterface(fx2jSuperType)
                                                   .addModifiers(Modifier.PUBLIC);


        FieldSpec controllerFieldSpec = FieldSpec.builder(controllerClass, CONTROLLER_NAME, Modifier.PRIVATE).build();

        FieldSpec rootFieldSpec = FieldSpec.builder(rootClass, "root", Modifier.PRIVATE).build();

        WildcardTypeName extendsObjectType = WildcardTypeName.subtypeOf(Object.class);
        ParameterizedTypeName classOfObjectTypeName = ParameterizedTypeName.get(ClassName.get(Class.class),
                                                                                extendsObjectType);
        ParameterizedTypeName controllerFactoryType = ParameterizedTypeName.get(ClassName.get(Function.class),
                                                                                classOfObjectTypeName,
                                                                                ClassName.get(Object.class));
        ParameterSpec controllerFactoryParameter = ParameterSpec.builder(controllerFactoryType, CONTROLLER_FACTORY_NAME)
                                                                .build();

        ParameterSpec builderProvidedControllerParameter = ParameterSpec.builder(controllerClass,
                                                                                 BUILDER_PROVIDED_CONTROLLER_NAME)
                                                                        .build();

        ParameterSpec builderProvidedRootParameter = ParameterSpec.builder(rootClass, BUILDER_PROVIDED_ROOT_NAME)
                                                                  .build();
        ParameterSpec resourcesParameter = ParameterSpec.builder(ResourceBundle.class, "resources").build();
        MethodSpec.Builder buildMethodBuilder = MethodSpec.methodBuilder("build")
                                                          .addModifiers(Modifier.PUBLIC)
                                                          .addParameter(builderProvidedControllerParameter)
                                                          .addParameter(builderProvidedRootParameter)
                                                          .addParameter(resourcesParameter)
                                                          .addParameter(controllerFactoryParameter)
                                                          .addAnnotation(Override.class);

        MethodSpec.Builder setControllerBuilder = MethodSpec.methodBuilder("setController")
                                                            .addModifiers(Modifier.PRIVATE)
                                                            .addParameter(builderProvidedControllerParameter)
                                                            .addParameter(controllerFactoryParameter)
                                                            .beginControlFlow("if ($N != null)",
                                                                              builderProvidedControllerParameter)
                                                            .addStatement("$L = $N", CONTROLLER_NAME,
                                                                          builderProvidedControllerParameter)
                                                            .nextControlFlow("else if ($N != null)",
                                                                             controllerFactoryParameter)
                                                            .addStatement("$1L = ($2T) $3N.apply($2T.class)",
                                                                          CONTROLLER_NAME, controllerClass,
                                                                          controllerFactoryParameter);

        if (!controllerClass.isInterface() &&
            !java.lang.reflect.Modifier.isAbstract(controllerClass.getModifiers()) &&
            methodResolver.hasDefaultConstructor(controllerClass)) {
            setControllerBuilder.nextControlFlow("else")
                                .addStatement("$L = new $T();", CONTROLLER_NAME, controllerClass);
        }

        MethodSpec setControllerMethodSpec = setControllerBuilder.endControlFlow().build();

        buildMethodBuilder.addStatement("$N($N, $N)", setControllerMethodSpec, builderProvidedControllerParameter,
                                        controllerFactoryParameter);

        buildMethodBuilder.addCode("\n");

        buildMethodBuilder.addCode(objectNodeCode.initializers()
                                                 .stream()
                                                 .map(CodeBlockUtils::convertStatementToCodeBlock)
                                                 .collect(CodeBlock.joining("")));

        MethodSpec getControllerMethodSpec = MethodSpec.methodBuilder("getController")
                                                       .addAnnotation(Override.class)
                                                       .returns(controllerClass)
                                                       .addModifiers(Modifier.PUBLIC)
                                                       .addStatement("return $N", controllerFieldSpec)
                                                       .build();

        MethodSpec setRootMethodSpec = MethodSpec.methodBuilder("setRoot")
                                                 .addModifiers(Modifier.PRIVATE)
                                                 .addParameter(ParameterSpec.builder(rootClass, "root").build())
                                                 .addStatement("this.root = root")
                                                 .build();

        MethodSpec getRootMethodSpec = MethodSpec.methodBuilder("getRoot")
                                                 .addAnnotation(Override.class)
                                                 .returns(rootClass)
                                                 .addModifiers(Modifier.PUBLIC)
                                                 .addStatement("return $N", rootFieldSpec)
                                                 .build();

        buildMethodBuilder.addCode("\n");

        buildMethodBuilder.addStatement("$N($L)", setRootMethodSpec,
                                        CodeBlockUtils.convertExpressionToCodeBlock(objectNodeCode.nodeValue()));


        if (controllerClass != Object.class) {
            methodResolver.findMethodRequiredPublicIfExists(controllerClass, "initialize")
                          .ifPresent(method -> buildMethodBuilder.addStatement("$L.$L()", CONTROLLER_NAME,
                                                                               method.getName()));
        }

        List<MethodSpec> methodSpecs = List.of(getControllerMethodSpec, setControllerMethodSpec, getRootMethodSpec,
                                               setRootMethodSpec);

        return typeSpecBuilder.addMethods(methodSpecs)
                              .addMethod(buildMethodBuilder.build())
                              .addFields(List.of(controllerFieldSpec, rootFieldSpec))
                              .build();
    }

    /**
     * Writes the generated Java code to a specified path.
     *
     * @param path The path where the Java file will be written to.
     * @throws IOException If an I/O error occurs while writing the file.
     */
    public void writeTo(Path path) throws IOException {
        javaFile.writeTo(path);
    }

    /**
     * Returns the JavaFileObject representation of the generated Java code.
     *
     * @return The JavaFileObject representation of the generated Java code.
     */
    public JavaFileObject toJavaFileObject() {
        return javaFile.toJavaFileObject();
    }

    /**
     * Returns the set of required modules.
     *
     * @return The set of required modules.
     */
    public Set<String> getRequiredModules() {
        return typeResolver.getResolvedModules();
    }

    /**
     * Returns the canonical class name of the generated Java class.
     *
     * @return The canonical class name.
     */
    public String getCanonicalClassName() {
        return canonicalClassName;
    }

    /**
     * Returns the relative file path of the fxml file from the resource root.
     *
     * @return The relative file path.
     */
    public Path getRelativeFilePath() {
        return relativeFilePath;
    }

    /**
     * Returns the root class of the JavaFX FXML file.
     *
     * @return The root class of the JavaFX FXML file.
     */
    public Class<?> getRootClass() {
        return rootClass;
    }

    /**
     * Returns the controller class associated with the JavaFX FXML file.
     *
     * @return The controller class associated with the JavaFX FXML file.
     */
    public Class<?> getControllerClass() {
        return controllerClass;
    }

    /**
     * Returns the root package name for the application.
     *
     * @return The root package name for the application.
     */
    public String getRootPackage() {
        return rootPackage;
    }
}
