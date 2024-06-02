package io.github.sheikah45.fx2j.processor.internal;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import io.github.sheikah45.fx2j.parser.attribute.AssignableAttribute;
import io.github.sheikah45.fx2j.parser.attribute.EventHandlerAttribute;
import io.github.sheikah45.fx2j.parser.attribute.FxmlAttribute;
import io.github.sheikah45.fx2j.parser.attribute.IdAttribute;
import io.github.sheikah45.fx2j.parser.attribute.InstancePropertyAttribute;
import io.github.sheikah45.fx2j.parser.attribute.SpecialAttribute;
import io.github.sheikah45.fx2j.parser.attribute.StaticPropertyAttribute;
import io.github.sheikah45.fx2j.parser.element.AssignableElement;
import io.github.sheikah45.fx2j.parser.element.ClassInstanceElement;
import io.github.sheikah45.fx2j.parser.element.ConstantElement;
import io.github.sheikah45.fx2j.parser.element.CopyElement;
import io.github.sheikah45.fx2j.parser.element.DefineElement;
import io.github.sheikah45.fx2j.parser.element.ElementContent;
import io.github.sheikah45.fx2j.parser.element.FactoryElement;
import io.github.sheikah45.fx2j.parser.element.IncludeElement;
import io.github.sheikah45.fx2j.parser.element.InstanceElement;
import io.github.sheikah45.fx2j.parser.element.InstancePropertyElement;
import io.github.sheikah45.fx2j.parser.element.ReferenceElement;
import io.github.sheikah45.fx2j.parser.element.RootElement;
import io.github.sheikah45.fx2j.parser.element.ScriptElement;
import io.github.sheikah45.fx2j.parser.element.StaticPropertyElement;
import io.github.sheikah45.fx2j.parser.element.ValueElement;
import io.github.sheikah45.fx2j.parser.property.Expression;
import io.github.sheikah45.fx2j.parser.property.FxmlProperty;
import io.github.sheikah45.fx2j.parser.property.Handler;
import io.github.sheikah45.fx2j.parser.property.Value;
import io.github.sheikah45.fx2j.processor.FxmlProcessor;
import io.github.sheikah45.fx2j.processor.ProcessorException;
import io.github.sheikah45.fx2j.processor.internal.model.CodeValue;
import io.github.sheikah45.fx2j.processor.internal.model.ExpressionResult;
import io.github.sheikah45.fx2j.processor.internal.model.NamedArgValue;
import io.github.sheikah45.fx2j.processor.internal.model.ObjectNodeCode;
import io.github.sheikah45.fx2j.processor.internal.resolve.ExpressionResolver;
import io.github.sheikah45.fx2j.processor.internal.resolve.MethodResolver;
import io.github.sheikah45.fx2j.processor.internal.resolve.NameResolver;
import io.github.sheikah45.fx2j.processor.internal.resolve.ResolverContainer;
import io.github.sheikah45.fx2j.processor.internal.resolve.TypeResolver;
import io.github.sheikah45.fx2j.processor.internal.resolve.ValueResolver;
import io.github.sheikah45.fx2j.processor.internal.utils.CodeBlockUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.Set;
import java.util.stream.Collectors;

public class ObjectNodeProcessor {
    private static final String EVENT_HANDLER_CLASS = "javafx.event.EventHandler";
    private static final String OBSERVABLE_VALUE_CLASS = "javafx.beans.value.ObservableValue";
    private static final String OBSERVABLE_LIST_CLASS = "javafx.collections.ObservableList";
    private static final String LIST_CHANGE_CLASS = "javafx.collections.ListChangeListener$Change";
    private static final String OBSERVABLE_SET_CLASS = "javafx.collections.ObservableSet";
    private static final String SET_CHANGE_CLASS = "javafx.collections.SetChangeListener$Change";
    private static final String OBSERVABLE_MAP_CLASS = "javafx.collections.ObservableMap";
    private static final String MAP_CHANGE_CLASS = "javafx.collections.MapChangeListener$Change";
    private static final Map<String, String> COLLECTION_LISTENER_MAP = Map.of(OBSERVABLE_LIST_CLASS, LIST_CHANGE_CLASS,
                                                                              OBSERVABLE_SET_CLASS, SET_CHANGE_CLASS,
                                                                              OBSERVABLE_MAP_CLASS, MAP_CHANGE_CLASS);

    private final ResolverContainer resolverContainer;
    private final TypeResolver typeResolver;
    private final ExpressionResolver expressionResolver;
    private final ValueResolver valueResolver;
    private final MethodResolver methodResolver;
    private final NameResolver nameResolver;
    private final Path filePath;
    private final Class<?> controllerClass;
    private final ClassInstanceElement rootNode;
    private final Path resourceRootPath;
    private final String rootPackage;
    private final ObjectNodeCode nodeCode;
    private final String providedId;

    private final CodeBlock.Builder objectInitializationBuilder = CodeBlock.builder();
    private final List<ClassInstanceElement> defaultPropertyElements = new ArrayList<>();
    private final SequencedMap<String, FxmlProperty.Instance> instanceProperties = new LinkedHashMap<>();
    private final List<FxmlProperty.Static> staticProperties = new ArrayList<>();
    private final List<FxmlProperty.EventHandler> handlerProperties = new ArrayList<>();
    private final List<ClassInstanceElement> definedChildren = new ArrayList<>();
    private final List<ScriptElement> scripts = new ArrayList<>();

    private Type objectType;
    private Type[] typeArguments;
    private String objectIdentifier;

    public ObjectNodeProcessor(ClassInstanceElement rootNode, Class<?> controllerClass,
                               ResolverContainer resolverContainer, Path filePath, Path resourceRootPath,
                               String rootPackage) {
        this.resourceRootPath = resourceRootPath;
        this.rootPackage = rootPackage;
        this.filePath = filePath;
        this.controllerClass = controllerClass;
        this.rootNode = rootNode;
        this.resolverContainer = resolverContainer;
        this.typeResolver = resolverContainer.getTypeResolver();
        this.methodResolver = resolverContainer.getMethodResolver();
        this.nameResolver = resolverContainer.getNameResolver();
        this.expressionResolver = resolverContainer.getExpressionResolver();
        this.valueResolver = resolverContainer.getValueResolver();

        List<? extends FxmlAttribute> attributes = rootNode.content().attributes();
        this.providedId = attributes.stream()
                                    .filter(IdAttribute.class::isInstance)
                                    .map(IdAttribute.class::cast)
                                    .map(IdAttribute::value)
                                    .findFirst()
                                    .orElse(null);

        attributes.forEach(attribute -> {
            switch (attribute) {
                case InstancePropertyAttribute instanceProperty ->
                        instanceProperties.put(instanceProperty.propertyName(), instanceProperty);
                case StaticPropertyAttribute staticProperty -> staticProperties.add(staticProperty);
                case EventHandlerAttribute handler -> handlerProperties.add(handler);
                case SpecialAttribute ignored -> {}
            }
        });

        rootNode.content().elements().forEach(element -> {
            switch (element) {
                case InstancePropertyElement instanceProperty ->
                        instanceProperties.put(instanceProperty.propertyName(), instanceProperty);
                case StaticPropertyElement staticProperty -> staticProperties.add(staticProperty);
                case DefineElement(List<ClassInstanceElement> elements) -> definedChildren.addAll(elements);
                case ClassInstanceElement classInstanceElement -> defaultPropertyElements.add(classInstanceElement);
                case ScriptElement script -> scripts.add(script);
            }
        });

        nodeCode = processNode();
    }

    private ObjectNodeCode processNodeInternal() {
        if (!scripts.isEmpty()) {
            throw new UnsupportedOperationException("Scripts not supported");
        }

        processObjectInitialization();
        definedChildren.forEach(this::buildChildNode);
        processDefaultPropertyElements();
        processDefaultPropertyValue();
        instanceProperties.values().forEach(this::processInstanceProperty);
        handlerProperties.forEach(this::processHandlerProperty);
        staticProperties.forEach(this::processStaticProperty);

        return new ObjectNodeCode(objectIdentifier, objectType, objectInitializationBuilder.add("\n").build());
    }

    private ObjectNodeCode processNode() {
        try {
            return processNodeInternal();
        } catch (ProcessorException processorException) {
            throw processorException;
        } catch (Exception exception) {
            throw new ProcessorException(exception, rootNode.toString());
        }
    }

    public ObjectNodeCode getNodeCode() {
        return nodeCode;
    }

    private void processIncludeInitialization(Path source) {
        FxmlProcessor includedProcessor = new FxmlProcessor(filePath.resolveSibling(source), resourceRootPath,
                                                            rootPackage, typeResolver.getClassLoader());
        objectType = typeResolver.resolveClassFromType(includedProcessor.getRootClass());

        if (objectType == null) {
            throw new IllegalArgumentException(
                    "Unable to determine object class for %s".formatted(filePath.resolveSibling(source)));
        }

        resolveIdentifier();

        String builderIdentifier = objectIdentifier + "Builder";
        ClassName includedClassName = ClassName.bestGuess(includedProcessor.getCanonicalClassName());
        objectInitializationBuilder.addStatement("$1T $2L = new $1T()", includedClassName, builderIdentifier)
                                   .addStatement("$L.build(null, null, $L, $L)", builderIdentifier,
                                                 FxmlProcessor.RESOURCES_NAME, FxmlProcessor.CONTROLLER_FACTORY_NAME)
                                   .addStatement("$T $L = $L.getRoot()", objectType, objectIdentifier,
                                                 builderIdentifier);

        Class<?> includedControllerClass = typeResolver.resolveClassFromType(includedProcessor.getControllerClass());
        if (includedControllerClass != Object.class && providedId != null) {
            String controllerIdentifier = objectIdentifier + "Controller";
            objectInitializationBuilder.addStatement("$T $L = $L.getController()", includedControllerClass,
                                                     controllerIdentifier, builderIdentifier);
            processControllerSetter(controllerIdentifier, includedControllerClass);
        }
    }

    private void processConstructorInitialization(String className) {
        objectType = typeResolver.resolve(className);
        resolveIdentifier();

        for (List<NamedArgValue> constructorArgs : getMatchingConstructorArgs()) {
            try {
                buildWithConstructorArgs(constructorArgs);
                return;
            } catch (Exception ignored) {}
        }

        throw new IllegalArgumentException("Unknown constructor");
    }

    private void processDefaultPropertyElements() {
        if (defaultPropertyElements.isEmpty()) {
            return;
        }

        String defaultProperty = methodResolver.resolveDefaultProperty(objectType);
        if (defaultProperty != null) {
            defaultPropertyElements.forEach(element -> processInstancePropertyElement(defaultProperty, element));
            return;
        }

        if (typeResolver.isAssignableFrom(Collection.class, objectType)) {
            Type contentTypeBound = typeArguments == null ? Object.class : typeArguments[0];
            CodeBlock objectBlock = CodeBlock.of("$L", objectIdentifier);
            defaultPropertyElements.forEach(
                    element -> addToCollectionWithTypeBound(objectBlock, element, contentTypeBound));
            return;
        }

        throw new IllegalArgumentException("Unable to handle default elements for elements %s".formatted(objectType));
    }

    private void processDefaultPropertyValue() {
        Value value = rootNode.content().value();
        if (value instanceof Value.Empty) {
            return;
        }

        String defaultProperty = methodResolver.resolveDefaultProperty(objectType);
        if (defaultProperty != null) {
            processInstancePropertyValue(defaultProperty, value);
            return;
        }

        if (typeResolver.isAssignableFrom(Collection.class, objectType)) {
            Type contentTypeBound = typeArguments == null ? Object.class : typeArguments[0];
            addToCollectionWithTypeBound(CodeBlock.of("$L", objectIdentifier), value, contentTypeBound);
            return;
        }

        throw new IllegalArgumentException("Unable to handle default value for elements %s".formatted(objectType));
    }

    private void processInstanceProperty(FxmlProperty.Instance property) {
        switch (property) {
            case InstancePropertyElement(
                    String propertyName, ElementContent<AssignableAttribute, AssignableElement> content
            ) -> processInstantPropertyContent(propertyName, content);
            case InstancePropertyAttribute(String propertyName, Value value) ->
                    processInstancePropertyValue(propertyName, value);
        }
    }

    private void processInstantPropertyContent(String propertyName,
                                               ElementContent<AssignableAttribute, AssignableElement> content) {
        content.attributes().forEach(attribute -> processAttributeOnProperty(propertyName, attribute));
        content.elements().forEach(element -> processElementOnProperty(propertyName, element));
        processInstancePropertyValue(propertyName, content.value());
    }

    private void processElementOnProperty(String propertyName, AssignableElement element) {
        switch (element) {
            case ClassInstanceElement classInstanceElement ->
                    processInstancePropertyElement(propertyName, classInstanceElement);
            case InstancePropertyElement(
                    String key, ElementContent<AssignableAttribute, AssignableElement> innerContent
            ) -> {
                if (!innerContent.attributes().isEmpty() || !innerContent.elements().isEmpty()) {
                    throw new UnsupportedOperationException(
                            "Cannot resolve property value from content with attributes");
                }

                addToPropertyMap(propertyName, key, innerContent.value());
            }
        }
    }

    private void processAttributeOnProperty(String propertyName, AssignableAttribute attribute) {
        switch (attribute) {
            case EventHandlerAttribute(String eventName, Handler handler) when "onChange".equals(eventName) ->
                    processHandlerOnProperty(propertyName, handler);
            case EventHandlerAttribute ignored ->
                    throw new UnsupportedOperationException("Unknown event handler on property");
            case InstancePropertyAttribute(String key, Value value) -> addToPropertyMap(propertyName, key, value);
        }
    }

    private void addToPropertyMap(String propertyName, String key, Value value) {
        Method propertyGetter = methodResolver.resolveGetter(objectType, propertyName)
                                              .orElseThrow(() -> new IllegalArgumentException(
                                                      "Unable to find getter for property %s on class %s".formatted(
                                                              propertyName, objectType)));


        Type propertyType = propertyGetter.getGenericReturnType();
        Type[] typeArguments = typeResolver.resolveUpperBoundTypeArguments(propertyType);
        if (!typeResolver.isAssignableFrom(Map.class, propertyType)) {
            throw new IllegalArgumentException("Property %s does not extend map".formatted(propertyName));
        }

        if (typeArguments == null || typeArguments.length != 2) {
            throw new IllegalArgumentException(
                    "Property %s does not represent a map with two type arguments".formatted(propertyName));
        }
        addToMapWithTypeBounds(CodeBlock.of("$L.$L()", objectIdentifier, propertyGetter.getName()), key, value,
                               typeArguments[0], typeArguments[1]);
    }

    private void processInstancePropertyElement(String propertyName, ClassInstanceElement element) {

        Method propertySetter = methodResolver.resolveSetter(objectType, propertyName).orElse(null);
        if (propertySetter != null) {
            ObjectNodeCode nodeCode = buildChildNode(element);
            Type parameterType = propertySetter.getGenericParameterTypes()[0];

            if (!typeResolver.isAssignableFrom(parameterType, nodeCode.nodeClass())) {
                throw new IllegalArgumentException(
                        "Property setter %s does not match node type %s".formatted(propertySetter,
                                                                                   nodeCode.nodeClass()));
            }

            objectInitializationBuilder.addStatement("$L.$L($L)", objectIdentifier, propertySetter.getName(),
                                                     nodeCode.nodeIdentifier());
            return;
        }

        Method propertyGetter = methodResolver.resolveGetter(objectType, propertyName).orElse(null);
        if (propertyGetter != null) {
            Type propertyGenericType = propertyGetter.getGenericReturnType();
            if (typeResolver.isAssignableFrom(Collection.class, propertyGenericType) &&
                propertyGenericType instanceof ParameterizedType parameterizedType) {

                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length != 1) {
                    throw new IllegalArgumentException(
                            "Unable to resolve contained type for type %s".formatted(parameterizedType));
                }

                addToCollectionWithTypeBound(CodeBlock.of("$L.$L()", objectIdentifier, propertyGetter.getName()),
                                             element, actualTypeArguments[0]);
                return;
            }

            throw new IllegalArgumentException(
                    "Unable to process read only property %s type %s".formatted(propertyName, propertyGenericType));
        }

        CodeBlock objectBlock = CodeBlock.of("$L", objectIdentifier);
        Type[] actualTypeArguments = typeArguments == null ? new Type[]{Object.class, Object.class} : typeArguments;
        if (typeResolver.isAssignableFrom(Map.class, objectType)) {

            if (actualTypeArguments.length != 2) {
                throw new IllegalArgumentException(
                        "Unable to resolve key and value type for type %s".formatted(objectType));
            }

            addToMapWithTypeBounds(objectBlock, propertyName, element, actualTypeArguments[0], actualTypeArguments[1]);
            return;
        }

        throw new IllegalStateException("Unknown property %s".formatted(propertyName));
    }

    private void processInstancePropertyValue(String propertyName, Value value) {
        switch (value) {
            case Value.Empty ignored -> {}
            case Expression expression -> {
                Method propertyMethod = methodResolver.resolveProperty(objectType, propertyName)
                                                      .orElseThrow(() -> new IllegalArgumentException(
                                                              "No property found for expression binding %s".formatted(
                                                                      propertyName)));
                Type valueType = propertyMethod.getGenericReturnType();
                ExpressionResult result = expressionResolver.resolveExpression(expression);
                Method bindMethod = methodResolver.findMethod(valueType, "bind", result.type())
                                                  .orElseThrow(() -> new IllegalArgumentException(
                                                          "Property %s does not have a bind method".formatted(
                                                                  propertyName)));
                result.initializers()
                      .stream()
                      .map(CodeBlockUtils::convertToCodeBlock)
                      .forEach(objectInitializationBuilder::addStatement);
                objectInitializationBuilder.addStatement("$L.$L().$L($L)", objectIdentifier, propertyMethod.getName(),
                                                         bindMethod.getName(), result.identifier());
            }
            case Value val -> {
                Method propertySetter = methodResolver.resolveSetter(objectType, propertyName).orElse(null);
                if (propertySetter != null) {
                    Type valueType = propertySetter.getGenericParameterTypes()[0];
                    CodeBlock valueCode = CodeBlockUtils.convertToCodeBlock(
                            valueResolver.resolveCodeValue(valueType, val));
                    objectInitializationBuilder.addStatement("$L.$L($L)", objectIdentifier, propertySetter.getName(),
                                                             valueCode);
                    return;
                }

                Method propertyGetter = methodResolver.resolveGetter(objectType, propertyName).orElse(null);
                if (propertyGetter != null) {
                    Type propertyGenericType = propertyGetter.getGenericReturnType();
                    CodeBlock propertyBlock = CodeBlock.of("$L.$L()", objectIdentifier, propertyGetter.getName());
                    if (typeResolver.isAssignableFrom(Collection.class, propertyGenericType) &&
                        propertyGenericType instanceof ParameterizedType parameterizedType) {

                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        if (actualTypeArguments.length != 1) {
                            throw new IllegalArgumentException(
                                    "Unable to resolve contained type for type %s".formatted(parameterizedType));
                        }

                        addToCollectionWithTypeBound(propertyBlock, val, actualTypeArguments[0]);
                        return;
                    }

                    if (typeResolver.isAssignableFrom(Map.class, propertyGenericType) &&
                        propertyGenericType instanceof ParameterizedType parameterizedType) {

                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        if (actualTypeArguments.length != 2) {
                            throw new IllegalArgumentException(
                                    "Unable to resolve key and value type for type %s".formatted(parameterizedType));
                        }

                        addToMapWithTypeBounds(propertyBlock, propertyName, val, actualTypeArguments[0],
                                               actualTypeArguments[1]);
                        return;
                    }

                    throw new IllegalArgumentException(
                            "Unable to process read only property %s type %s".formatted(propertyName,
                                                                                        propertyGenericType));
                }

                if (typeResolver.isAssignableFrom(Map.class, objectType)) {
                    Type[] actualTypeArguments = typeArguments == null ?
                                                 new Type[]{Object.class, Object.class} :
                                                 typeArguments;
                    if (typeArguments != null && typeArguments.length != 2) {
                        throw new IllegalArgumentException(
                                "Unable to resolve key and value type for type %s".formatted(objectType));
                    }

                    addToMapWithTypeBounds(CodeBlock.of("$L", objectIdentifier), propertyName, value,
                                           actualTypeArguments[0], actualTypeArguments[1]);
                    return;
                }

                throw new UnsupportedOperationException(
                        "Unknown property %s for class %s".formatted(propertyName, objectType));
            }
        }
    }

    private void processStaticProperty(FxmlProperty.Static property) {
        switch (property) {
            case StaticPropertyElement(
                    String className, String propertyName,
                    ElementContent<AssignableAttribute, AssignableElement> content
            ) -> processStaticPropertyContent(className, propertyName, content);
            case StaticPropertyAttribute(String className, String propertyName, Value value) ->
                    processStaticPropertyValue(className, propertyName, value);
        }
    }

    private void processStaticPropertyContent(String className, String propertyName,
                                              ElementContent<AssignableAttribute, AssignableElement> content) {
        if (!content.attributes().isEmpty()) {
            throw new UnsupportedOperationException("Cannot handle attributes on static property element");
        }

        if ((!content.elements().isEmpty() && !(content.value() instanceof Value.Empty)) ||
            content.elements().size() > 1) {
            throw new UnsupportedOperationException("Cannot handle multiple values for static property element");
        }

        if (content.elements().size() == 1) {
            if (!(content.elements().getFirst() instanceof ClassInstanceElement classInstanceElement)) {
                throw new UnsupportedOperationException(
                        "Cannot handle static property element value that is not a class instance");
            }
            processStaticPropertyElement(className, propertyName, classInstanceElement);
        }

        processStaticPropertyValue(className, propertyName, content.value());
    }

    private void processStaticPropertyElement(String className, String property, ClassInstanceElement element) {
        Class<?> staticPropertyClass = typeResolver.resolve(className);
        Method propertySetter = methodResolver.resolveStaticSetter(staticPropertyClass, property).orElse(null);

        if (propertySetter != null) {
            Type parameterType = propertySetter.getGenericParameterTypes()[0];

            if (!typeResolver.isAssignableFrom(parameterType, objectType)) {
                throw new IllegalArgumentException(
                        "First parameter of static property setter does not match node type");
            }

            ObjectNodeCode nodeCode = buildChildNode(element);
            if (!typeResolver.isAssignableFrom(propertySetter.getParameterTypes()[1], nodeCode.nodeClass())) {
                throw new IllegalArgumentException(
                        "Second parameter of static property setter %s does not match node type %s".formatted(
                                propertySetter, nodeCode.nodeClass()));
            }

            objectInitializationBuilder.addStatement("$T.$L($L, $L)", staticPropertyClass, propertySetter.getName(),
                                                     objectIdentifier, nodeCode.nodeIdentifier());
        } else {
            throw new IllegalStateException(
                    "Cannot find setter for static property %s.%s".formatted(className, property));
        }
    }

    private void addToCollectionWithTypeBound(CodeBlock collectionCodeBlock, Value value, Type contentTypeBound) {
        switch (value) {
            case Value.Empty() -> {}
            case Value.Literal(String val) when val.contains(",") -> Arrays.stream(val.split(",\\s*"))
                                                                           .map(item -> valueResolver.resolveCodeValue(
                                                                                   contentTypeBound, item))
                                                                           .forEach(
                                                                                   valueCode -> objectInitializationBuilder.addStatement(
                                                                                           "$L.add($L)",
                                                                                           collectionCodeBlock,
                                                                                           valueCode));
            case Value val -> objectInitializationBuilder.addStatement("$L.add($L)", collectionCodeBlock,
                                                                       valueResolver.resolveCodeValue(contentTypeBound,
                                                                                                      val));
        }
    }

    private void addToMapWithTypeBounds(CodeBlock mapBlock, String key, Value value, Type keyTypeBound,
                                        Type valueTypeBound) {
        CodeBlock keyValue = CodeBlockUtils.convertToCodeBlock(valueResolver.resolveCodeValue(keyTypeBound, key));
        CodeBlock valueValue = CodeBlockUtils.convertToCodeBlock(valueResolver.resolveCodeValue(valueTypeBound, value));
        objectInitializationBuilder.addStatement("$L.put($L, $L)", mapBlock, keyValue, valueValue);
    }

    private boolean propertyIsMutable(String property) {
        return typeResolver.isAssignableFrom(Map.class, objectType) ||
               methodResolver.resolveSetter(objectType, property).isPresent() ||
               methodResolver.resolveGetter(objectType, property)
                             .map(Method::getReturnType)
                             .map(returnType -> typeResolver.isAssignableFrom(Collection.class, returnType) ||
                                                typeResolver.isAssignableFrom(Map.class, returnType))
                             .orElse(false);
    }

    private void processStaticPropertyValue(String className, String property, Value value) {
        if (value instanceof Value.Empty) {
            return;
        }

        Class<?> staticPropertyClass = typeResolver.resolve(className);
        Method propertySetter = methodResolver.resolveStaticSetter(staticPropertyClass, property).orElse(null);
        if (propertySetter == null) {
            throw new IllegalArgumentException(
                    "Unable to find static setter for %s on %s".formatted(property, staticPropertyClass));
        }

        Class<?>[] parameterTypes = propertySetter.getParameterTypes();
        if (!typeResolver.isAssignableFrom(parameterTypes[0], objectType)) {
            throw new IllegalArgumentException("First parameter of static property setter does not match node type");
        }

        CodeBlock valueCode = CodeBlockUtils.convertToCodeBlock(
                valueResolver.resolveCodeValue(parameterTypes[1], value));
        objectInitializationBuilder.addStatement("$T.$L($L, $L)", staticPropertyClass, propertySetter.getName(),
                                                 objectIdentifier, valueCode);
    }

    private List<List<NamedArgValue>> getPossibleConstructorArgs() {
        return methodResolver.getConstructors(objectType)
                             .stream()
                             .filter(methodResolver::hasAllNamedArgs)
                             .map(methodResolver::getNamedArgs)
                             .toList();
    }

    private void processRootInitialization(String type) {
        objectIdentifier = FxmlProcessor.BUILDER_PROVIDED_ROOT_NAME;
        objectType = typeResolver.resolve(type);
    }

    private CodeBlock resolveParameterValue(NamedArgValue namedArgValue) {
        Class<?> paramType = namedArgValue.parameterType();
        String paramName = namedArgValue.name();
        FxmlProperty.Instance property = instanceProperties.get(paramName);
        return switch (property) {
            case InstancePropertyElement(String ignored, ElementContent<?, ?> content) -> {
                if (!content.attributes().isEmpty() || !content.elements().isEmpty()) {
                    throw new UnsupportedOperationException(
                            "Cannot resolve property value from content with attributes");
                }

                CodeValue value = valueResolver.resolveCodeValue(paramType, content.value());
                if (!(value instanceof CodeValue.Null)) {
                    yield CodeBlockUtils.convertToCodeBlock(value);
                }

                yield CodeBlockUtils.convertToCodeBlock(valueResolver.coerceDefaultValue(namedArgValue));

            }
            case InstancePropertyAttribute(String ignored, Value value) ->
                    CodeBlockUtils.convertToCodeBlock(valueResolver.resolveCodeValue(paramType, value));
            case null -> CodeBlockUtils.convertToCodeBlock(valueResolver.coerceDefaultValue(namedArgValue));
        };
    }

    private Set<List<NamedArgValue>> getMatchingConstructorArgs() {
        Set<String> definedProperties = instanceProperties.keySet();

        Map<Boolean, Set<String>> definedPropertiesByMutability = definedProperties.stream()
                                                                                   .collect(Collectors.partitioningBy(
                                                                                           this::propertyIsMutable,
                                                                                           Collectors.toSet()));


        Set<String> constructorProperties = definedPropertiesByMutability.getOrDefault(false, Set.of());

        Set<String> setterProperties = definedPropertiesByMutability.getOrDefault(true, Set.of());

        int undefinedConstructorPropertiesBest = Integer.MAX_VALUE;
        int setterPropertiesBest = Integer.MAX_VALUE;

        Set<List<NamedArgValue>> bestConstructorArgs = new LinkedHashSet<>();
        for (List<NamedArgValue> namedArgValues : getPossibleConstructorArgs()) {
            Set<String> argumentNames = namedArgValues.stream().map(NamedArgValue::name).collect(Collectors.toSet());

            if (!argumentNames.containsAll(constructorProperties)) {
                continue;
            }

            Set<String> undefinedConstructorProperties = new HashSet<>(argumentNames);
            undefinedConstructorProperties.removeAll(definedProperties);

            Set<String> remainingSetterProperties = new HashSet<>(setterProperties);
            remainingSetterProperties.removeAll(argumentNames);

            int undefinedConstructorPropertiesCount = undefinedConstructorProperties.size();
            if (undefinedConstructorPropertiesBest == undefinedConstructorPropertiesCount &&
                setterPropertiesBest == remainingSetterProperties.size()) {
                bestConstructorArgs.add(namedArgValues);
            } else if (undefinedConstructorPropertiesBest > undefinedConstructorPropertiesCount ||
                       (undefinedConstructorPropertiesBest == undefinedConstructorPropertiesCount &&
                        setterPropertiesBest > remainingSetterProperties.size())) {
                undefinedConstructorPropertiesBest = undefinedConstructorPropertiesCount;
                setterPropertiesBest = remainingSetterProperties.size();
                bestConstructorArgs.clear();
                bestConstructorArgs.add(namedArgValues);
            }
        }

        return bestConstructorArgs;
    }

    private void processObjectInitialization() {
        objectInitializationBuilder.add("\n");
        switch (rootNode) {
            case RootElement(String type, ElementContent<?, ?> ignored) -> processRootInitialization(type);
            case ReferenceElement(String source, ElementContent<?, ?> content) ->
                    processReferenceInitialization(source, content);
            case CopyElement(
                    String source, ElementContent<?, ?> ignored
            ) -> processCopyInitialization(source);
            case IncludeElement(
                    Path source, Path ignored1, Charset ignored2, ElementContent<?, ?> ignored3
            ) -> processIncludeInitialization(source);
            case FactoryElement(
                    String factoryClassName, String methodName, ElementContent<?, ?> ignored
            ) -> processFactoryBasedInitialization(factoryClassName, methodName);
            case ConstantElement(String className, String member, ElementContent<?, ?> ignored) ->
                    processConstantInitialization(className, member);
            case ValueElement(String className, String value, ElementContent<?, ?> ignored) ->
                    processValueInitialization(className, value);
            case InstanceElement(String className, ElementContent<?, ?> ignored) ->
                    processConstructorInitialization(className);
            default -> throw new UnsupportedOperationException("Unable to initialize object");
        }

        if (providedId != null) {
            processControllerSetter(objectIdentifier, objectType);
            methodResolver.resolveSetter(objectType, "id", String.class)
                          .ifPresent(method -> objectInitializationBuilder.addStatement("$L.$L($S)", objectIdentifier,
                                                                                        method.getName(),
                                                                                        objectIdentifier));
            typeArguments = extractTypeArguments();
        }
    }

    private void processValueInitialization(String className, String value) {
        objectType = typeResolver.unwrapType(typeResolver.resolve(className));
        resolveIdentifier();
        if (objectType == String.class) {
            objectInitializationBuilder.addStatement("$T $L = $S", objectType, objectIdentifier, value);
            return;
        }


        Class<?> wrappedType = typeResolver.wrapType(objectType);
        Method method = methodResolver.findMethodRequiredPublicIfExists(wrappedType, "valueOf", String.class)
                                      .orElseThrow(() -> new IllegalArgumentException(
                                              "Class %s does not have a valueOf method".formatted(objectType)));


        try {
            CodeBlock valueCode = CodeBlockUtils.convertToCodeBlock(valueResolver.resolveCodeValue(method, value));
            objectInitializationBuilder.addStatement("$T $L = $L", objectType, objectIdentifier, valueCode);
        } catch (InvocationTargetException | IllegalAccessException | RuntimeException e) {
            throw new IllegalArgumentException(
                    "Value %s not parseable by %s.%s".formatted(value, objectType, method.getName()));
        }

    }

    private void processConstantInitialization(String className, String member) {
        Class<?> constantContainerClass = typeResolver.resolve(className);
        objectType = methodResolver.resolveFieldRequiredPublicIfExists(constantContainerClass, member)
                                   .map(Field::getType)
                                   .orElseThrow(() -> new IllegalArgumentException(
                                           "Field %s of %s is not found or public".formatted(member,
                                                                                             constantContainerClass)));
        resolveIdentifier();
        objectInitializationBuilder.addStatement("$T $L = $T.$L", objectType, objectIdentifier, constantContainerClass,
                                                 member);
    }

    private void resolveIdentifier() {
        if (providedId != null) {
            objectIdentifier = providedId;
            nameResolver.storeIdType(objectIdentifier, objectType);
        } else {
            objectIdentifier = nameResolver.resolveUniqueName(objectType);
        }
    }

    private void buildWithConstructorArgs(List<NamedArgValue> namedArgValues) {
        CodeBlock parameterValues = namedArgValues.stream()
                                                  .map(this::resolveParameterValue)
                                                  .collect(CodeBlock.joining(", "));

        namedArgValues.stream().map(NamedArgValue::name).forEach(instanceProperties::remove);

        objectInitializationBuilder.addStatement("$T $L = new $T($L)", objectType, objectIdentifier, objectType,
                                                 parameterValues);
    }

    private void processFactoryBasedInitialization(String factoryClassName, String factoryMethodName) {
        Class<?> factoryClass = typeResolver.resolve(factoryClassName);
        Method factoryMethod = methodResolver.findMethod(factoryClass, factoryMethodName, 0)
                                             .orElseThrow(() -> new IllegalArgumentException(
                                                     "Factory method not found %s.%s".formatted(factoryClassName,
                                                                                                factoryMethodName)));

        objectType = factoryMethod.getReturnType();
        resolveIdentifier();

        objectInitializationBuilder.addStatement("$T $L = $T.$L()", objectType, objectIdentifier, factoryClass,
                                                 factoryMethod.getName());
    }

    private void processControllerSetter(String identifier, Type valueClass) {
        if (controllerClass != Object.class) {
            processControllerSettersFromKnownClass(identifier, valueClass);
        }
    }

    private Type[] extractTypeArguments() {
        return methodResolver.resolveSetter(controllerClass, objectIdentifier, objectType)
                             .map(Method::getGenericParameterTypes)
                             .map(types -> types[0])
                             .or(() -> methodResolver.resolveField(controllerClass, objectIdentifier)
                                                     .map(Field::getGenericType))
                             .map(typeResolver::resolveUpperBoundTypeArguments)
                             .orElse(null);
    }

    private void processReferenceInitialization(String source, ElementContent<?, ?> content) {
        objectIdentifier = source;
        objectType = nameResolver.resolveTypeById(source);
        if (!content.attributes().isEmpty() || !content.elements().isEmpty()) {
            throw new UnsupportedOperationException("References with elements or attributes not supported");
        }
    }

    private void processControllerSettersFromKnownClass(String identifier, Type valueClass) {
        methodResolver.resolveSetterRequiredPublicIfExists(controllerClass, identifier, valueClass)
                      .map(Method::getName)
                      .map(methodName -> CodeBlock.of("$L.$L($L)", FxmlProcessor.CONTROLLER_NAME, methodName,
                                                      identifier))
                      .or(() -> methodResolver.resolveFieldRequiredPublicIfExists(controllerClass, identifier)
                                              .filter(field -> typeResolver.isAssignableFrom(field.getType(),
                                                                                             valueClass))
                                              .map(Field::getName)
                                              .map(fieldName -> CodeBlock.of("$1L.$2L = $2L",
                                                                             FxmlProcessor.CONTROLLER_NAME, fieldName)))
                      .ifPresent(objectInitializationBuilder::addStatement);
    }

    private void processHandlerProperty(FxmlProperty.EventHandler eventHandler) {
        String eventName = eventHandler.eventName();
        Handler handler = eventHandler.handler();
        Method handlerSetter = methodResolver.resolveSetterRequiredPublicIfExists(objectType, eventName,
                                                                                  typeResolver.resolve(
                                                                                          EVENT_HANDLER_CLASS))
                                             .orElse(null);
        if (handlerSetter != null) {
            processHandlerSetter(handler, handlerSetter);
            return;
        }

        if (eventName.startsWith("on") && eventName.endsWith("Change")) {
            String property = eventName.substring(2, eventName.length() - 6);
            processPropertyChangeListener(handler, property);
            return;
        }

        throw new UnsupportedOperationException("Unknown event %s for class %s".formatted(eventName, objectType));
    }

    private void processHandlerSetter(Handler handler, Method handlerSetter) {
        Type handlerType = handlerSetter.getGenericParameterTypes()[0];
        Class<?> eventClass;
        if (handlerType instanceof ParameterizedType parameterizedType) {
            Class<?>[] typeArgumentBounds = typeResolver.resolveLowerBoundTypeArguments(parameterizedType);
            if (typeArgumentBounds.length != 1) {
                throw new IllegalArgumentException(
                        "Unable to determine bounds of handler type %s".formatted(handlerType));
            }

            eventClass = typeArgumentBounds[0];
        } else {
            eventClass = Object.class;
        }
        CodeBlock valueCode = resolveControllerEventHandler(eventClass, handler);
        objectInitializationBuilder.addStatement("$L.$L($L)", objectIdentifier, handlerSetter.getName(), valueCode);
    }

    private void processPropertyChangeListener(Handler handler, String property) {
        Method propertyMethod = methodResolver.resolveProperty(objectType, property)
                                              .orElseThrow(() -> new IllegalArgumentException(
                                                      "Unable to find property method for %s".formatted(property)));

        Class<?> propertyClass = methodResolver.resolveGetter(objectType, property)
                                               .map(Method::getReturnType)
                                               .orElseThrow(() -> new IllegalArgumentException(
                                                       "Unable to determine the class of property %s".formatted(
                                                               property)));

        CodeBlock valueCode = resolveControllerPropertyChangeListener(propertyClass, handler);
        objectInitializationBuilder.addStatement("$L.$L().addListener($L)", objectIdentifier, propertyMethod.getName(),
                                                 valueCode);
    }

    private CodeBlock resolveControllerEventHandler(Class<?> eventType, Handler handler) {
        if (!(handler instanceof Handler.Method(String methodName))) {
            throw new UnsupportedOperationException("None method handlers not supported");
        }

        return methodResolver.findMethod(controllerClass, methodName, eventType)
                             .map(method -> {
                                 if (method.getExceptionTypes().length == 0) {
                                     return CodeBlock.of("$L::$L", FxmlProcessor.CONTROLLER_NAME, method.getName());
                                 } else {
                                     return CodeBlock.builder()
                                                     .add("event -> {\n")
                                                     .indent()
                                                     .beginControlFlow("try")
                                                     .add("$L.$L(event);\n", FxmlProcessor.CONTROLLER_NAME,
                                                          method.getName())
                                                     .nextControlFlow("catch ($T e)", Exception.class)
                                                     .add("throw new $T(e);\n", RuntimeException.class)
                                                     .endControlFlow()
                                                     .unindent()
                                                     .add("}")
                                                     .build();
                                 }
                             })
                             .or(() -> methodResolver.findMethod(controllerClass, methodName).map(method -> {
                                 if (method.getExceptionTypes().length == 0) {
                                     return CodeBlock.of("event -> $L.$L()", FxmlProcessor.CONTROLLER_NAME,
                                                         method.getName());
                                 } else {
                                     return CodeBlock.builder()
                                                     .add("event -> {\n")
                                                     .indent()
                                                     .beginControlFlow("try")
                                                     .add("$L.$L();\n", FxmlProcessor.CONTROLLER_NAME, method.getName())
                                                     .nextControlFlow("catch ($T e)", Exception.class)
                                                     .add("throw new $T(e);\n", RuntimeException.class)
                                                     .endControlFlow()
                                                     .unindent()
                                                     .add("}")
                                                     .build();
                                 }
                             }))
                             .orElseThrow(() -> new IllegalArgumentException(
                                     "No method %s on %s".formatted(methodName, controllerClass)));
    }

    private CodeBlock resolveControllerPropertyChangeListener(Class<?> valueClass, Handler handler) {
        if (!(handler instanceof Handler.Method(String methodName))) {
            throw new UnsupportedOperationException("Non method change listeners not supported");
        }

        Method changeMethod = methodResolver.findMethod(controllerClass, methodName,
                                                        typeResolver.resolve(OBSERVABLE_VALUE_CLASS), valueClass,
                                                        valueClass)
                                            .orElseThrow(() -> new IllegalArgumentException(
                                                    "Unable to find change method for name %s and property type %s".formatted(
                                                            methodName, valueClass)));

        Type observableType = changeMethod.getGenericParameterTypes()[0];
        if (observableType instanceof ParameterizedType parameterizedType &&
            typeResolver.hasNonMatchingWildcardUpperBounds(parameterizedType, valueClass)) {
            throw new IllegalArgumentException(
                    "Observable value parameter does not match signature of change listener as Observable Value does not have lower bound %s".formatted(
                            valueClass));
        }

        return CodeBlock.of("$L::$L", FxmlProcessor.CONTROLLER_NAME, changeMethod.getName());
    }

    private void processHandlerOnProperty(String propertyName, Handler handler) {
        methodResolver.resolveProperty(objectType, propertyName)
                      .ifPresentOrElse(propertyMethod -> processPropertyChangeListener(handler, propertyName), () -> {
                          Method propertyGetter = methodResolver.resolveGetter(objectType, propertyName)
                                                                .orElseThrow(() -> new IllegalArgumentException(
                                                                        "Unable to find getter for %s on class %s".formatted(
                                                                                propertyName, objectType)));
                          processPropertyContainerListener(propertyGetter, handler);
                      });
    }

    private CodeBlock resolveControllerContainerChangeListener(Type valueType, Handler handler) {
        if (!(handler instanceof Handler.Method(String methodName))) {
            throw new UnsupportedOperationException("Non method handlers not supported");
        }
        Class<?> valueClass = typeResolver.resolveClassFromType(valueType);
        Type[] boundTypeArguments = typeResolver.resolveUpperBoundTypeArguments(valueType);

        return COLLECTION_LISTENER_MAP.entrySet()
                                      .stream()
                                      .filter(entry -> typeResolver.resolve(entry.getKey())
                                                                   .isAssignableFrom(valueClass))
                                      .map(Map.Entry::getValue)
                                      .map(typeResolver::resolve)
                                      .findFirst()
                                      .flatMap(changeClass -> methodResolver.findMethod(controllerClass, methodName,
                                                                                        changeClass))
                                      .filter(method -> {
                                          Type parameterType = method.getGenericParameterTypes()[0];
                                          return typeResolver.typeArgumentsMeetBounds(parameterType,
                                                                                      boundTypeArguments);
                                      })
                                      .map(method -> CodeBlock.of("$L::$L", FxmlProcessor.CONTROLLER_NAME,
                                                                  method.getName()))
                                      .orElseThrow(() -> new IllegalArgumentException(
                                              "Unable to find change method for name %s and property type %s".formatted(
                                                      methodName, valueType)));
    }

    private void processPropertyContainerListener(Method propertyGetter, Handler handler) {
        CodeBlock listener = resolveControllerContainerChangeListener(propertyGetter.getGenericReturnType(), handler);
        objectInitializationBuilder.addStatement("$L.$L().addListener($L)", objectIdentifier, propertyGetter.getName(),
                                                 listener);
    }

    private void addToCollectionWithTypeBound(CodeBlock collectionCodeBlock, ClassInstanceElement element,
                                              Type contentTypeBound) {
        ObjectNodeCode nodeCode = buildChildNode(element);
        if (!typeResolver.isAssignableFrom(contentTypeBound, nodeCode.nodeClass())) {
            throw new IllegalArgumentException(
                    "Content type bound %s does not match node type %s".formatted(contentTypeBound,
                                                                                  nodeCode.nodeClass()));
        }

        objectInitializationBuilder.addStatement("$L.add($L)", collectionCodeBlock, nodeCode.nodeIdentifier());
    }

    private void processCopyInitialization(String source) {
        objectType = nameResolver.resolveTypeById(source);

        if (!methodResolver.hasCopyConstructor(objectType)) {
            throw new IllegalArgumentException("No copy constructor found for class %s".formatted(objectType));
        }

        objectIdentifier = source + "Copy";
        objectInitializationBuilder.addStatement("$1T $2L = new $1T($3L)", objectType, objectIdentifier, source);
    }

    private void addToMapWithTypeBounds(CodeBlock mapBlock, String key, ClassInstanceElement element, Type keyTypeBound,
                                        Type valueTypeBound) {
        ObjectNodeCode nodeCode = buildChildNode(element);
        if (!typeResolver.isAssignableFrom(valueTypeBound, nodeCode.nodeClass())) {
            throw new IllegalArgumentException(
                    "Content type bound %s does not match node type %s".formatted(valueTypeBound,
                                                                                  nodeCode.nodeClass()));
        }

        CodeBlock keyValue = CodeBlockUtils.convertToCodeBlock(valueResolver.resolveCodeValue(keyTypeBound, key));
        objectInitializationBuilder.addStatement("$L.put($L, $L)", mapBlock, keyValue, nodeCode.nodeIdentifier());
    }

    private ObjectNodeCode buildChildNode(ClassInstanceElement element) {
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(element, controllerClass, resolverContainer, filePath,
                                                          resourceRootPath, rootPackage).getNodeCode();
        objectInitializationBuilder.add(nodeCode.objectInitializationCode());
        return nodeCode;
    }
}
