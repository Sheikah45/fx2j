package io.github.sheikah45.fx2j.compiler.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import io.github.sheikah45.fx2j.api.Fx2jBuilder;
import io.github.sheikah45.fx2j.compiler.internal.model.FxmlComponents;
import io.github.sheikah45.fx2j.compiler.internal.model.ObjectNodeCode;
import io.github.sheikah45.fx2j.compiler.internal.processor.ObjectNodeProcessor;
import io.github.sheikah45.fx2j.compiler.internal.processor.ReflectionResolver;
import io.github.sheikah45.fx2j.compiler.utils.FXMLUtils;
import io.github.sheikah45.fx2j.compiler.utils.JavaFileUtils;
import io.github.sheikah45.fx2j.compiler.utils.StringUtils;

import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.lang.invoke.MethodType;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;

public class FxmlProcessor {

    public static final String CONTROLLER_NAME = "controller";
    public static final String RESOURCES_NAME = "resources";
    public static final String CONTROLLER_FACTORY_NAME = "controllerFactory";
    public static final String BUILDER_PROVIDED_CONTROLLER_NAME = "builderProvidedController";
    public static final String BUILDER_PROVIDED_ROOT_NAME = "builderProvidedRoot";

    private final ReflectionResolver resolver;
    private final String rootPackage;
    private final Class<?> controllerClass;
    private final JavaFile javaFile;
    private final Path relativeFilePath;
    private final String canonicalClassName;
    private final ObjectNodeCode objectNodeCode;
    private final Class<?> rootClass;

    public FxmlProcessor(Path filePath, Path resourceRootPath, String rootPackage, ClassLoader classLoader) {
        this.rootPackage = rootPackage;
        Path absoluteFilePath = filePath.toAbsolutePath();
        FxmlComponents fxmlComponents = FXMLUtils.readFxml(absoluteFilePath);
        resolver = new ReflectionResolver(fxmlComponents.imports(), classLoader);

        String controllerClassName = fxmlComponents.rootNode()
                                                   .attributes()
                                                   .getOrDefault("fx:controller", fxmlComponents.controllerType());

        if (controllerClassName != null) {
            controllerClass = resolver.resolveRequired(controllerClassName);
            if (controllerClass == null) {
                throw new IllegalArgumentException("Unable to find controller class %s".formatted(controllerClassName));
            }
        } else {
            controllerClass = Object.class;
        }

        Path absoluteResourceRootPath = resourceRootPath.toAbsolutePath();
        objectNodeCode = new ObjectNodeProcessor(fxmlComponents.rootNode(), controllerClass, resolver, absoluteFilePath,
                                                 absoluteResourceRootPath, this.rootPackage).getNodeCode();
        rootClass = MethodType.methodType(objectNodeCode.nodeClass()).wrap().returnType();

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
                                                          .addParameter(controllerFactoryParameter);

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
            resolver.hasDefaultConstructor(controllerClass)) {
            setControllerBuilder.nextControlFlow("else")
                                .addStatement("$L = new $T();", CONTROLLER_NAME, controllerClass);
        }

        MethodSpec setControllerMethodSpec = setControllerBuilder.endControlFlow().build();

        buildMethodBuilder.addStatement("$N($N, $N)", setControllerMethodSpec, builderProvidedControllerParameter,
                                        controllerFactoryParameter);

        buildMethodBuilder.addCode(objectNodeCode.objectInitializationCode());

        MethodSpec getControllerMethodSpec = MethodSpec.methodBuilder("getController")
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
                                                 .returns(rootClass)
                                                 .addModifiers(Modifier.PUBLIC)
                                                 .addStatement("return $N", rootFieldSpec)
                                                 .build();

        buildMethodBuilder.addStatement("$N($L)", setRootMethodSpec, objectNodeCode.nodeIdentifier());


        if (controllerClass != Object.class) {
            resolver.findMethodRequiredPublicIfExists(controllerClass, "initialize")
                    .ifPresent(method -> buildMethodBuilder.addStatement("$L.$L()", CONTROLLER_NAME, method.getName()));
        }

        List<MethodSpec> methodSpecs = List.of(getControllerMethodSpec, setControllerMethodSpec, getRootMethodSpec,
                                               setRootMethodSpec);

        return typeSpecBuilder.addMethods(methodSpecs)
                              .addMethod(buildMethodBuilder.build())
                              .addFields(List.of(controllerFieldSpec, rootFieldSpec))
                              .build();
    }

    public void writeTo(Path path) throws IOException {
        javaFile.writeTo(path);
    }

    public JavaFileObject toJavaFileObject() {
        return javaFile.toJavaFileObject();
    }

    public Set<String> getRequiredModules() {
        return resolver.getResolvedModules();
    }

    public String getCanonicalClassName() {
        return canonicalClassName;
    }

    public Path getRelativeFilePath() {
        return relativeFilePath;
    }

    public Class<?> getRootClass() {
        return rootClass;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public String getRootPackage() {
        return rootPackage;
    }
}
