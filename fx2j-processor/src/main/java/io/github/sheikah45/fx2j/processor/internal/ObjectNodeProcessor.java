package io.github.sheikah45.fx2j.processor.internal;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import io.github.sheikah45.fx2j.parser.ParseException;
import io.github.sheikah45.fx2j.parser.attribute.EventHandlerAttribute;
import io.github.sheikah45.fx2j.parser.attribute.FxmlAttribute;
import io.github.sheikah45.fx2j.parser.attribute.IdAttribute;
import io.github.sheikah45.fx2j.parser.attribute.InstancePropertyAttribute;
import io.github.sheikah45.fx2j.parser.attribute.StaticPropertyAttribute;
import io.github.sheikah45.fx2j.parser.element.ClassInstanceElement;
import io.github.sheikah45.fx2j.parser.element.ConstantElement;
import io.github.sheikah45.fx2j.parser.element.CopyElement;
import io.github.sheikah45.fx2j.parser.element.DefineElement;
import io.github.sheikah45.fx2j.parser.element.FactoryElement;
import io.github.sheikah45.fx2j.parser.element.IncludeElement;
import io.github.sheikah45.fx2j.parser.element.InstanceElement;
import io.github.sheikah45.fx2j.parser.element.InstancePropertyElement;
import io.github.sheikah45.fx2j.parser.element.ReferenceElement;
import io.github.sheikah45.fx2j.parser.element.RootElement;
import io.github.sheikah45.fx2j.parser.element.ScriptElement;
import io.github.sheikah45.fx2j.parser.element.StaticPropertyElement;
import io.github.sheikah45.fx2j.parser.element.ValueElement;
import io.github.sheikah45.fx2j.parser.property.FxmlProperty;
import io.github.sheikah45.fx2j.parser.property.Value;
import io.github.sheikah45.fx2j.processor.FxmlProcessor;
import io.github.sheikah45.fx2j.processor.ProcessorException;
import io.github.sheikah45.fx2j.processor.internal.model.NamedArgValue;
import io.github.sheikah45.fx2j.processor.internal.model.ObjectNodeCode;
import io.github.sheikah45.fx2j.processor.internal.utils.StringUtils;

import java.lang.invoke.MethodType;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ObjectNodeProcessor {
    private static final Map<Class<?>, Object> DEFAULTS_MAP = Map.of(byte.class, (byte) 0, short.class, (short) 0,
                                                                     int.class, 0, long.class, 0L, float.class, 0.0f,
                                                                     double.class, 0.0d, char.class, '\u0000',
                                                                     boolean.class, false);

    private static final Set<Class<?>> ALLOWED_LITERALS = Set.of(Byte.class, Short.class, Integer.class, Long.class,
                                                                 Character.class, Float.class, Double.class,
                                                                 Boolean.class);

    private static final Pattern CHANGE_PROPERTY_PATTERN = Pattern.compile("on(?<property>.+)Change");
    private static final String EVENT_HANDLER_CLASS = "javafx.event.EventHandler";
    private static final String ON_CHANGE = "onChange";
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

    private final ReflectionResolver resolver;
    private final Path filePath;
    private final Class<?> controllerClass;
    private final ClassInstanceElement rootNode;
    private final NameTracker nameTracker;
    private final Path resourceRootPath;
    private final String rootPackage;
    private final ObjectNodeCode nodeCode;
    private final String id;

    private final CodeBlock.Builder objectInitializationBuilder = CodeBlock.builder();
    private final List<Value.Single> defaultPropertyChildren = new ArrayList<>();
    private final Map<String, FxmlProperty.Instance> instanceProperties = new HashMap<>();
    private final List<FxmlProperty.Static> staticProperties = new ArrayList<>();
    private final List<FxmlProperty.EventHandler> handlerProperties = new ArrayList<>();
    private final List<ClassInstanceElement> definedChildren = new ArrayList<>();
    private final List<ScriptElement> scripts = new ArrayList<>();

    private Class<?> objectClass;
    private Class<?>[] typeArguments;
    private String objectIdentifier;

    public ObjectNodeProcessor(ClassInstanceElement rootNode, Class<?> controllerClass, ReflectionResolver resolver,
                               Path filePath, Path resourceRootPath, String rootPackage) {
        this(rootNode, controllerClass, resolver, filePath, resourceRootPath, rootPackage, new NameTracker());
    }

    private ObjectNodeProcessor(ClassInstanceElement rootNode, Class<?> controllerClass, ReflectionResolver resolver,
                                Path filePath, Path resourceRootPath, String rootPackage, NameTracker nameTracker) {
        this.resourceRootPath = resourceRootPath;
        this.rootPackage = rootPackage;
        this.resolver = resolver;
        this.filePath = filePath;
        this.controllerClass = controllerClass;
        this.rootNode = rootNode;
        this.nameTracker = nameTracker;
        List<FxmlAttribute> attributes = rootNode.content().attributes();
        this.id = attributes.stream()
                            .filter(IdAttribute.class::isInstance)
                            .map(IdAttribute.class::cast)
                            .map(IdAttribute::value)
                            .findFirst()
                            .orElse(null);

        attributes.forEach(attribute -> {
            switch (attribute) {
                case InstancePropertyAttribute instance -> instanceProperties.put(instance.property(), instance);
                case StaticPropertyAttribute staticProperty -> staticProperties.add(staticProperty);
                case EventHandlerAttribute handler -> handlerProperties.add(handler);
                case FxmlAttribute.FxAttribute ignored -> {}
            }
        });

        rootNode.content().children().forEach(child -> {
            switch (child) {
                case InstancePropertyElement instance -> instanceProperties.put(instance.property(), instance);
                case StaticPropertyElement stat -> staticProperties.add(stat);
                case DefineElement(List<ClassInstanceElement> children) -> definedChildren.addAll(children);
                case ClassInstanceElement classInstanceElement ->
                        defaultPropertyChildren.add(new Value.Element(classInstanceElement));
                case ScriptElement script -> scripts.add(script);
            }
        });

        if (!(rootNode.content().body() instanceof Value.Empty)) {
            defaultPropertyChildren.add(rootNode.content().body());
        }

        nodeCode = processNode();
    }

    private ObjectNodeCode processNodeInternal() {
        if (!scripts.isEmpty()) {
            throw new UnsupportedOperationException("Scripts not supported");
        }

        processObjectInitialization();
        processDefinedChildren();
        processDefaultProperty();
        processInstanceProperties();
        processStaticProperties();
        processHandlerProperties();

        return new ObjectNodeCode(objectIdentifier, objectClass, objectInitializationBuilder.add("\n").build());
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
                                                            rootPackage, resolver.getClassLoader());
        objectClass = resolver.checkResolved(includedProcessor.getRootClass());

        if (objectClass == null) {
            throw new IllegalArgumentException(
                    "Unable to determine object class for %s".formatted(filePath.resolveSibling(source)));
        }

        resolveIdentifier();

        String builderIdentifier = objectIdentifier + "Builder";
        ClassName includedClassName = ClassName.bestGuess(includedProcessor.getCanonicalClassName());
        objectInitializationBuilder.addStatement("$1T $2L = new $1T()", includedClassName, builderIdentifier)
                                   .addStatement("$L.build(null, null, $L, $L)", builderIdentifier,
                                                 FxmlProcessor.RESOURCES_NAME, FxmlProcessor.CONTROLLER_FACTORY_NAME)
                                   .addStatement("$T $L = $L.getRoot()", objectClass, objectIdentifier,
                                                 builderIdentifier);

        Class<?> includedControllerClass = resolver.checkResolved(includedProcessor.getControllerClass());
        if (includedControllerClass != Object.class && id != null) {
            String controllerIdentifier = objectIdentifier + "Controller";
            objectInitializationBuilder.addStatement("$T $L = $L.getController()", includedControllerClass,
                                                     controllerIdentifier, builderIdentifier);
            processControllerSetter(controllerIdentifier, includedControllerClass);
        }
    }

    private void processHandlerProperties() {
        handlerProperties.forEach(this::processHandlerProperty);
    }

    private void processConstructorInitialization(String className) {
        objectClass = resolver.resolveRequired(className);
        resolveIdentifier();

        for (List<NamedArgValue> constructorArgs : getMatchingConstructorArgs()) {
            try {
                buildWithConstructorArgs(constructorArgs);
                return;
            } catch (Exception ignored) {}
        }

        throw new IllegalArgumentException("Unknown constructor");
    }

    private void processInstanceProperties() {
        Collection<FxmlProperty.Instance> instanceProperties = this.instanceProperties.values();
        if (Map.class.isAssignableFrom(objectClass)) {
            Class<?> keyTypeBound = typeArguments == null ? Object.class : typeArguments[0];
            Class<?> valueTypeBound = typeArguments == null ? Object.class : typeArguments[1];

            instanceProperties.forEach(
                    attribute -> addPropertyToMapWithTypeBounds(CodeBlock.of("$L", objectIdentifier), attribute,
                                                                keyTypeBound, valueTypeBound));
            return;
        }

        instanceProperties.forEach(property -> processInstanceProperty(property.property(), property.value()));
    }

    private void addPropertyToMapWithTypeBounds(CodeBlock mapBlock, FxmlProperty.Instance property,
                                                Class<?> keyTypeBound, Class<?> valueTypeBound) {
        CodeBlock keyValue = coerceValue(keyTypeBound, property.property());
        CodeBlock valueValue = coerceValue(valueTypeBound, property.value());
        objectInitializationBuilder.addStatement("$L.put($L, $L)", mapBlock, keyValue, valueValue);
    }

    private void processStaticProperties() {
        staticProperties.forEach(
                property -> processStaticProperty(property.className(), property.property(), property.value()));
    }

    private void processDefinedChildren() {
        definedChildren.forEach(this::buildChildNode);
    }

    private void processInstanceProperty(String property, Value value) {
        switch (value) {
            case Value.Element(ClassInstanceElement classInstanceElement) ->
                    processPropertyClassInstanceElement(property, classInstanceElement);
            case Value.Attribute(FxmlProperty.Instance attribute) ->
                    processPropertiesOnProperty(property, List.of(attribute));
            case Value.Single single -> processInstancePropertySingle(property, single);
            case Value.Multi(List<? extends Value.Single> values) -> {
                List<ClassInstanceElement> elements = new ArrayList<>();
                List<FxmlProperty.Instance> properties = new ArrayList<>();

                values.forEach(val -> {
                    switch (val) {
                        case Value.Element(ClassInstanceElement element) -> elements.add(element);
                        case Value.Element(FxmlProperty.Instance element) -> properties.add(element);
                        case Value.Attribute(FxmlProperty.Instance attribute) -> properties.add(attribute);
                        case Value.Single single -> processInstancePropertySingle(property, single);
                    }
                });

                if (!elements.isEmpty()) {
                    processPropertyElements(property, elements);
                }
                if (!properties.isEmpty()) {
                    processPropertiesOnProperty(property, properties);
                }
            }
            default -> throw new UnsupportedOperationException(
                    "Cannot process value %s for property %s".formatted(value, property));
        }
    }

    private void processStaticProperty(String className, String property, Value value) {
        switch (value) {
            case Value.Element(ClassInstanceElement element) ->
                    processStaticPropertyElement(className, property, element);
            case Value.Single single -> processStaticPropertySingle(className, property, single);
            default -> throw new UnsupportedOperationException(
                    "Cannot process value %s for static property %s".formatted(value, property));
        }
    }

    private void processStaticPropertyElement(String className, String property, ClassInstanceElement propertyElement) {
        Class<?> staticPropertyClass = resolver.resolveRequired(className);
        Method propertySetter = resolver.resolveStaticSetter(staticPropertyClass, property).orElse(null);

        if (propertySetter != null) {
            processStaticPropertySetter(propertyElement, propertySetter, staticPropertyClass);
        } else {
            throw new IllegalStateException(
                    "Cannot find setter for static property %s.%s".formatted(className, property));
        }
    }

    private void processStaticPropertySetter(ClassInstanceElement staticProperty, Method propertySetter,
                                             Class<?> staticPropertyClass) {
        Class<?> parameterType = propertySetter.getParameterTypes()[0];

        if (!parameterType.isAssignableFrom(objectClass)) {
            throw new IllegalArgumentException("First parameter of static property setter does not match node type");
        }

        ObjectNodeCode nodeCode = buildChildNode(staticProperty);
        if (!propertySetter.getParameterTypes()[1].isAssignableFrom(nodeCode.nodeClass())) {
            throw new IllegalArgumentException(
                    "Second parameter of static property setter %s does not match node type %s".formatted(
                            propertySetter, nodeCode.nodeClass()));
        }

        objectInitializationBuilder.addStatement("$T.$L($L, $L)", staticPropertyClass, propertySetter.getName(),
                                                 objectIdentifier, nodeCode.nodeIdentifier());
    }

    private void processControllerSetter(String identifier, Class<?> valueClass) {
        if (controllerClass != Object.class) {
            processControllerSettersFromKnownClass(identifier, valueClass);
        } else {
            throw new UnsupportedOperationException("fx:id without controller type not supported");
        }
    }

    private void resolveIdentifier() {
        if (id != null) {
            objectIdentifier = id;
        } else {
            objectIdentifier = nameTracker.getDeconflictedName(StringUtils.camelCase(objectClass.getSimpleName()));
        }
        nameTracker.storeIdClass(objectIdentifier, objectClass);
    }

    private void processCopyInitialization(String source) {
        objectClass = nameTracker.getStoredClassById(source);
        if (!resolver.hasCopyConstructor(objectClass)) {
            throw new IllegalArgumentException("No copy constructor found for class %s".formatted(objectClass));
        }

        objectIdentifier = source + "Copy";
        objectInitializationBuilder.addStatement("$1T $2L = new $1T($3L)", objectClass, objectIdentifier, source);
    }

    private void processReferenceInitialization(String source, ClassInstanceElement.Content content) {
        objectIdentifier = source;
        objectClass = nameTracker.getStoredClassById(objectIdentifier);
        if (!content.attributes().isEmpty() || !content.children().isEmpty()) {
            throw new UnsupportedOperationException("References with children or attributes not supported");
        }
    }

    private void processControllerSettersFromKnownClass(String identifier, Class<?> valueClass) {
        resolver.resolveSetterRequiredPublicIfExists(controllerClass, identifier, valueClass)
                .map(Method::getName)
                .map(methodName -> CodeBlock.of("$L.$L($L)", FxmlProcessor.CONTROLLER_NAME, methodName, identifier))
                .or(() -> resolver.resolveFieldRequiredPublic(controllerClass, identifier)
                                  .filter(field -> field.getType().isAssignableFrom(valueClass))
                                  .map(Field::getName)
                                  .map(fieldName -> CodeBlock.of("$1L.$2L = $2L", FxmlProcessor.CONTROLLER_NAME,
                                                                 fieldName)))
                .ifPresent(objectInitializationBuilder::addStatement);
    }

    private void processRootInitialization(String type) {
        objectIdentifier = FxmlProcessor.BUILDER_PROVIDED_ROOT_NAME;
        objectClass = resolver.resolveRequired(type);
    }

    private void processValueInitialization(String className, String value) {
        objectClass = resolver.resolveRequired(className);
        resolveIdentifier();
        if (objectClass == String.class) {
            objectInitializationBuilder.addStatement("$T $L = $S", objectClass, objectIdentifier, value);
            return;
        }


        Method method = resolver.findMethodRequiredPublicIfExists(objectClass, "valueOf", String.class)
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Class %s does not have a valueOf method".formatted(objectClass)));


        try {
            CodeBlock valueCode = coerceUsingValueOfMethodResults(value, method, objectClass);
            objectInitializationBuilder.addStatement("$T $L = $L", objectClass, objectIdentifier, valueCode);
        } catch (InvocationTargetException | IllegalAccessException | RuntimeException e) {
            throw new IllegalArgumentException(
                    "Value %s not parseable by %s.%s".formatted(value, objectClass, method.getName()));
        }

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

    private boolean propertyIsMutable(String property) {
        return Map.class.isAssignableFrom(objectClass) ||
               resolver.resolveSetter(objectClass, property).isPresent() ||
               resolver.resolveGetter(objectClass, property)
                       .map(Method::getReturnType)
                       .map(returnType -> Collection.class.isAssignableFrom(returnType) ||
                                          Map.class.isAssignableFrom(returnType))
                       .orElse(false);
    }

    private CodeBlock coerceUsingValueOfMethodResults(String valueString, Method valueOfMethod, Class<?> valueType)
            throws IllegalAccessException, InvocationTargetException {
        Object value = valueOfMethod.invoke(null, valueString);
        if (valueType.isEnum()) {
            return CodeBlock.of("$T.$L", valueType, value);
        }

        if (Objects.equals(value, Double.POSITIVE_INFINITY) || Objects.equals(value, Float.POSITIVE_INFINITY)) {
            return CodeBlock.of("$T.POSITIVE_INFINITY", valueType);
        }

        if (Objects.equals(value, Double.NEGATIVE_INFINITY) || Objects.equals(value, Float.NEGATIVE_INFINITY)) {
            return CodeBlock.of("$T.NEGATIVE_INFINITY", valueType);
        }

        if (Objects.equals(value, Double.NaN) || Objects.equals(value, Float.NaN)) {
            return CodeBlock.of("$T.NaN", valueType);
        }

        if (ALLOWED_LITERALS.contains(valueType)) {
            return CodeBlock.of("$L", value);
        }

        return CodeBlock.of("$T.$L($S)", valueType, valueOfMethod.getName(), valueString);
    }

    private void processConstantInitialization(String className, String member) {
        Class<?> constantContainerClass = resolver.resolveRequired(className);
        objectClass = resolver.resolveFieldTypeRequiredPublic(constantContainerClass, member).orElseThrow();
        resolveIdentifier();
        objectInitializationBuilder.addStatement("$T $L = $T.$L", objectClass, objectIdentifier, constantContainerClass,
                                                 member);
    }

    private void processFactoryBasedInitialization(String factoryClassName, String factoryMethodName) {
        Class<?> factoryClass = resolver.resolveRequired(factoryClassName);
        Method factoryMethod = resolver.findMethod(factoryClass, factoryMethodName)
                                       .orElseThrow(() -> new IllegalArgumentException(
                                               "Factory method not found %s.%s".formatted(factoryClassName,
                                                                                          factoryMethodName)));

        objectClass = factoryMethod.getReturnType();
        resolveIdentifier();

        objectInitializationBuilder.addStatement("$T $L = $T.$L()", objectClass, objectIdentifier, factoryClass,
                                                 factoryMethod.getName());
    }

    private void buildWithConstructorArgs(List<NamedArgValue> namedArgValues) {
        CodeBlock parameterValues = namedArgValues.stream()
                                                  .map(this::resolveParameterValue)
                                                  .collect(CodeBlock.joining(", "));

        namedArgValues.stream().map(NamedArgValue::name).forEach(instanceProperties::remove);

        objectInitializationBuilder.addStatement("$T $L = new $T($L)", objectClass, objectIdentifier, objectClass,
                                                 parameterValues);
    }

    private CodeBlock resolveParameterValue(NamedArgValue namedArgValue) {
        Class<?> paramType = namedArgValue.parameterType();
        String paramName = namedArgValue.name();
        FxmlProperty.Instance property = instanceProperties.get(paramName);
        Value value = property == null ? null : property.value();
        return switch (value) {
            case Value.Single single -> coerceValue(paramType, single);
            case null -> {
                String defaultValue = namedArgValue.defaultValue();
                if (defaultValue.isBlank()) {
                    yield CodeBlock.of("$L", DEFAULTS_MAP.get(namedArgValue.parameterType()));
                }
                yield coerceValue(paramType, defaultValue);
            }
            default ->
                    throw new UnsupportedOperationException("Cannot resolve parameter value from %s".formatted(value));
        };
    }

    private List<List<NamedArgValue>> getPossibleConstructorArgs() {
        return Arrays.stream(objectClass.getConstructors())
                     .filter(resolver::hasAllNamedArgs)
                     .map(resolver::getNamedArgs)
                     .toList();
    }

    private ObjectNodeCode buildChildNode(ClassInstanceElement childNode) {
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(childNode, controllerClass, resolver, filePath,
                                                          resourceRootPath, rootPackage, nameTracker).getNodeCode();
        objectInitializationBuilder.add(nodeCode.objectInitializationCode());
        return nodeCode;
    }

    private void processPropertyChangeListener(Value.Handler handler, String property) {
        Method propertyMethod = resolver.resolveProperty(objectClass, property)
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                "Unable to find property method for %s".formatted(property)));

        Class<?> propertyClass = resolver.resolveGetter(objectClass, property)
                                         .map(Method::getReturnType)
                                         .orElseThrow(() -> new IllegalArgumentException(
                                                 "Unable to determine the class of property %s".formatted(property)));

        CodeBlock valueCode = resolveControllerPropertyChangeListener(propertyClass, handler);
        objectInitializationBuilder.addStatement("$L.$L().addListener($L)", objectIdentifier, propertyMethod.getName(),
                                                 valueCode);
    }

    private void processHandlerSetter(Value.Handler handler, Method handlerSetter) {
        Type handlerType = handlerSetter.getGenericParameterTypes()[0];
        Class<?> eventClass;
        if (handlerType instanceof ParameterizedType parameterizedType) {
            Class<?>[] typeArgumentBounds = resolver.resolveLowerBoundTypeArguments(parameterizedType);
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

    private void processObjectInitialization() {
        objectInitializationBuilder.add("\n");
        switch (rootNode) {
            case RootElement(String type, ClassInstanceElement.Content ignored) -> processRootInitialization(type);
            case ReferenceElement(String source, ClassInstanceElement.Content content) ->
                    processReferenceInitialization(source, content);
            case CopyElement(
                    String source, ClassInstanceElement.Content ignored
            ) -> processCopyInitialization(source);
            case IncludeElement(
                    Path source, Path ignored1, Charset ignored2, ClassInstanceElement.Content ignored3
            ) -> processIncludeInitialization(source);
            case FactoryElement(
                    String factoryClassName, String methodName, ClassInstanceElement.Content ignored
            ) -> processFactoryBasedInitialization(factoryClassName, methodName);
            case ConstantElement(String className, String member, ClassInstanceElement.Content ignored) ->
                    processConstantInitialization(className, member);
            case ValueElement(String className, Value.Literal(String value), ClassInstanceElement.Content ignored) ->
                    processValueInitialization(className, value);
            case InstanceElement(String className, ClassInstanceElement.Content ignored) ->
                    processConstructorInitialization(className);
            default -> throw new UnsupportedOperationException("Unable to initialize object");
        }

        if (id != null) {
            processControllerSetter(objectIdentifier, objectClass);
            resolver.resolveSetter(objectClass, "id", String.class)
                    .ifPresent(method -> objectInitializationBuilder.addStatement("$L.$L($S)", objectIdentifier,
                                                                                  method.getName(), objectIdentifier));
            typeArguments = extractTypeArguments();
        }
    }

    private Class<?>[] extractTypeArguments() {
        Method setterMethod = resolver.resolveSetter(controllerClass, objectIdentifier, objectClass).orElse(null);
        if (setterMethod != null) {
            Type genericParameterType = setterMethod.getGenericParameterTypes()[0];
            return resolver.resolveUpperBoundTypeArguments(genericParameterType);
        }

        Field field = resolver.resolveField(controllerClass, objectIdentifier).orElse(null);
        if (field != null) {
            Type genericFieldType = field.getGenericType();
            return resolver.resolveUpperBoundTypeArguments(genericFieldType);
        }

        return null;
    }

    private void processInstancePropertySingle(String property, Value.Single value) {
        if (value instanceof Value.Expression) {
            throw new UnsupportedOperationException("Binding expressions not supported");
        }

        if (value instanceof Value.Empty) {
            return;
        }

        Method propertySetter = resolver.resolveSetter(objectClass, property).orElse(null);
        if (propertySetter != null) {
            processPropertySetter(value, propertySetter);
            return;
        }

        Method propertyGetter = resolver.resolveGetter(objectClass, property).orElse(null);

        if (propertyGetter != null) {
            Type propertyGenericType = propertyGetter.getGenericReturnType();
            Class<?> propertyClass = propertyGetter.getReturnType();
            String propertyName = objectIdentifier + StringUtils.capitalize(property);
            if (Collection.class.isAssignableFrom(propertyClass) &&
                propertyGenericType instanceof ParameterizedType parameterizedType &&
                value instanceof Value.Literal(String val)) {
                processCollectionInitialization(val, parameterizedType, propertyName, propertyGetter);
                return;
            }

            throw new IllegalArgumentException(
                    "Unable to process read only attribute of type %s".formatted(propertyClass));
        }

        throw new UnsupportedOperationException(
                "Unknown property %s for class %s".formatted(property, objectClass.getName()));
    }

    private void processHandlerProperty(FxmlProperty.EventHandler eventHandler) {
        String eventName = eventHandler.eventName();
        Value.Handler handler = eventHandler.handler();
        Method handlerSetter = resolver.resolveSetterRequiredPublicIfExists(objectClass, eventName,
                                                                            resolver.resolveRequired(
                                                                                    EVENT_HANDLER_CLASS)).orElse(null);
        if (handlerSetter != null) {
            processHandlerSetter(handler, handlerSetter);
            return;
        }

        Matcher propertyMatcher = CHANGE_PROPERTY_PATTERN.matcher(eventName);
        if (propertyMatcher.matches()) {
            String property = propertyMatcher.group("property");
            processPropertyChangeListener(handler, property);
            return;
        }

        throw new UnsupportedOperationException(
                "Unknown event %s for class %s".formatted(eventName, objectClass.getName()));
    }

    private void processPropertySetter(Value value, Method propertySetter) {
        if (value instanceof Value.Empty) {
            return;
        }

        Class<?> valueType = propertySetter.getParameterTypes()[0];
        CodeBlock valueCode = coerceValue(valueType, value);
        objectInitializationBuilder.addStatement("$L.$L($L)", objectIdentifier, propertySetter.getName(), valueCode);
    }

    private void processStaticPropertySingle(String className, String property, Value value) {
        Class<?> staticPropertyClass = resolver.resolveRequired(className);
        Method propertySetter = resolver.resolveStaticSetter(staticPropertyClass, property).orElse(null);
        if (propertySetter == null) {
            throw new IllegalArgumentException(
                    "Unable to find static setter for %s on %s".formatted(property, staticPropertyClass));
        }
        Class<?> objectType = propertySetter.getParameterTypes()[0];

        if (!objectType.isAssignableFrom(objectClass)) {
            throw new IllegalArgumentException("First parameter of static property setter does not match node type");
        }

        Class<?> valueType = propertySetter.getParameterTypes()[1];
        CodeBlock valueCode = coerceValue(valueType, value);
        objectInitializationBuilder.addStatement("$T.$L($L, $L)", staticPropertyClass, propertySetter.getName(),
                                                 objectIdentifier, valueCode);
    }

    private void processPropertiesOnProperty(String property, Collection<FxmlProperty.Instance> properties) {
        Method propertyGetter = resolver.resolveGetter(objectClass, property)
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                "Unable to find getter for property %s on class %s".formatted(property,
                                                                                                              objectClass)));


        properties.forEach(prop -> {
            switch (prop) {
                case EventHandlerAttribute eventHandler when ON_CHANGE.equals(eventHandler.eventName()) -> {
                    Value.Handler handler = eventHandler.handler();

                    resolver.resolveProperty(objectClass, property)
                            .ifPresentOrElse(propertyMethod -> processPropertyChangeListener(handler, property),
                                             () -> processPropertyContainerListener(propertyGetter, handler));
                }
                case FxmlProperty.Instance instance -> {
                    Type propertyClass = propertyGetter.getGenericReturnType();
                    Class<?>[] typeArguments = resolver.resolveUpperBoundTypeArguments(propertyClass);
                    if (typeArguments == null || typeArguments.length != 2) {
                        throw new IllegalArgumentException(
                                "Property %s does not represent a map with two type arguments".formatted(property));
                    }

                    Class<?> keyClass = typeArguments[0];
                    Class<?> valueClass = typeArguments[1];
                    addPropertyToMapWithTypeBounds(CodeBlock.of("$L.$L()", objectIdentifier, propertyGetter.getName()),
                                                   instance, keyClass, valueClass);
                }
            }
        });
    }

    private CodeBlock resolveControllerContainerChangeListener(Type valueType, Value.Handler handler) {
        if (!(handler instanceof Value.Handler.Method(String methodName))) {
            throw new UnsupportedOperationException("Non method handlers not supported");
        }
        Class<?> valueClass = resolver.resolveClassFromType(valueType);
        Class<?>[] boundTypeArguments = resolver.resolveUpperBoundTypeArguments(valueType);

        return COLLECTION_LISTENER_MAP.entrySet()
                                      .stream()
                                      .filter(entry -> resolver.resolveRequired(entry.getKey())
                                                               .isAssignableFrom(valueClass))
                                      .map(Map.Entry::getValue)
                                      .map(resolver::resolveRequired)
                                      .findFirst()
                                      .flatMap(changeClass -> resolver.findMethod(controllerClass, methodName,
                                                                                  changeClass))
                                      .filter(method -> {
                                          Type parameterType = method.getGenericParameterTypes()[0];
                                          return resolver.parameterTypeArgumentsMeetBounds(parameterType,
                                                                                           boundTypeArguments);
                                      })
                                      .map(method -> CodeBlock.of("$L::$L", FxmlProcessor.CONTROLLER_NAME,
                                                                  method.getName()))
                                      .orElseThrow(() -> new IllegalArgumentException(
                                              "Unable to find change method for name %s and property type %s".formatted(
                                                      methodName, valueType)));
    }

    private CodeBlock resolveControllerPropertyChangeListener(Class<?> valueClass, Value.Handler handler) {
        if (!(handler instanceof Value.Handler.Method(String methodName))) {
            throw new UnsupportedOperationException("Non method change listeners not supported");
        }

        Method changeMethod = resolver.findMethod(controllerClass, methodName,
                                                  resolver.resolveRequired(OBSERVABLE_VALUE_CLASS), valueClass,
                                                  valueClass)
                                      .orElseThrow(() -> new IllegalArgumentException(
                                              "Unable to find change method for name %s and property type %s".formatted(
                                                      methodName, valueClass)));

        Type observableType = changeMethod.getGenericParameterTypes()[0];
        if (observableType instanceof ParameterizedType parameterizedType &&
            resolver.hasNonMatchingWildcardUpperBounds(parameterizedType, valueClass)) {
            throw new IllegalArgumentException(
                    "Observable value parameter does not match signature of change listener as Observable Value does not have lower bound %s".formatted(
                            valueClass));
        }

        return CodeBlock.of("$L::$L", FxmlProcessor.CONTROLLER_NAME, changeMethod.getName());
    }

    private void processPropertyContainerListener(Method propertyGetter, Value.Handler handler) {
        CodeBlock listener = resolveControllerContainerChangeListener(propertyGetter.getGenericReturnType(), handler);
        objectInitializationBuilder.addStatement("$L.$L().addListener($L)", objectIdentifier, propertyGetter.getName(),
                                                 listener);
    }

    private void processPropertyClassInstanceElement(String property, ClassInstanceElement element) {
        Method propertySetter = resolver.resolveSetter(objectClass, property).orElse(null);
        if (propertySetter != null) {
            processPropertyChildSetter(element, propertySetter);
        } else {
            processPropertyElements(property, List.of(element));
        }
    }

    private void processCollectionInitialization(String value, ParameterizedType parameterizedType, String propertyName,
                                                 Method propertyGetter) {
        objectInitializationBuilder.addStatement("$T $L = $L.$L()",
                                                 resolver.resolveTypeNameWithoutVariables(parameterizedType),
                                                 propertyName, objectIdentifier, propertyGetter.getName());
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length != 1) {
            throw new IllegalArgumentException(
                    "Unable to resolve contained type for type %s".formatted(parameterizedType));
        }
        Class<?> containedClassBound = resolver.resolveTypeUpperBound(actualTypeArguments[0]);
        Arrays.stream(value.split(",\\s*"))
              .forEach(val -> addToCollectionWithTypeBound(
                      CodeBlock.of("$L.$L()", objectIdentifier, propertyGetter.getName()), new Value.Literal(val),
                      containedClassBound));
    }

    private CodeBlock resolveControllerEventHandler(Class<?> eventType, Value.Handler handler) {
        if (!(handler instanceof Value.Handler.Method(String methodName))) {
            throw new UnsupportedOperationException("None method handlers not supported");
        }

        return resolver.findMethod(controllerClass, methodName, eventType)
                       .map(method -> {
                           if (method.getExceptionTypes().length == 0) {
                               return CodeBlock.of("$L::$L", FxmlProcessor.CONTROLLER_NAME, method.getName());
                           } else {
                               return CodeBlock.builder()
                                               .add("event -> {\n")
                                               .indent()
                                               .beginControlFlow("try")
                                               .add("$L.$L(event);")
                                               .nextControlFlow("catch ($T e)", Exception.class)
                                               .add("throw new $T(e);", RuntimeException.class)
                                               .endControlFlow()
                                               .unindent()
                                               .add("}")
                                               .build();
                           }
                       })
                       .or(() -> resolver.findMethod(controllerClass, methodName).map(method -> {
                           if (method.getExceptionTypes().length == 0) {
                               return CodeBlock.of("event -> $L.$L()", FxmlProcessor.CONTROLLER_NAME, method.getName());
                           } else {
                               return CodeBlock.builder()
                                               .add("event -> {\n")
                                               .indent()
                                               .beginControlFlow("try")
                                               .add("$L.$L();")
                                               .nextControlFlow("catch ($T e)", Exception.class)
                                               .add("throw new $T(e);", RuntimeException.class)
                                               .endControlFlow()
                                               .unindent()
                                               .add("}")
                                               .build();
                           }
                       }))
                       .orElseThrow(() -> new IllegalArgumentException(
                               "No method %s on %s".formatted(methodName, controllerClass)));
    }

    private void processPropertyChildSetter(ClassInstanceElement propertyNode, Method propertySetter) {
        ObjectNodeCode nodeCode = buildChildNode(propertyNode);

        Class<?> parameterType = propertySetter.getParameterTypes()[0];
        if (!parameterType.isAssignableFrom(nodeCode.nodeClass())) {
            throw new IllegalArgumentException(
                    "Parameter type `%s` does not match node type `%s`".formatted(parameterType, nodeCode.nodeClass()));
        }

        objectInitializationBuilder.addStatement("$L.$L($L)", objectIdentifier, propertySetter.getName(),
                                                 nodeCode.nodeIdentifier());
    }

    private void processDefaultProperty() {
        if (defaultPropertyChildren.isEmpty()) {
            return;
        }

        if (Collection.class.isAssignableFrom(objectClass)) {
            Class<?> contentTypeBound = typeArguments == null ? Object.class : typeArguments[0];
            defaultPropertyChildren.forEach(
                    value -> addToCollectionWithTypeBound(CodeBlock.of("$L", objectIdentifier), value,
                                                          contentTypeBound));
            return;
        }

        if (Map.class.isAssignableFrom(objectClass)) {
            List<FxmlProperty.Instance> properties = defaultPropertyChildren.stream().map(value -> {
                if (value instanceof Value.Element(FxmlProperty.Instance element)) {
                    return element;
                }

                if (value instanceof Value.Attribute(FxmlProperty.Instance attribute)) {
                    return attribute;
                }

                throw new ParseException("Map property contains a non property element");
            }).toList();
            Class<?> keyTypeBound = typeArguments == null ? Object.class : typeArguments[0];
            Class<?> valueTypeBound = typeArguments == null ? Object.class : typeArguments[1];
            properties.forEach(
                    attribute -> addPropertyToMapWithTypeBounds(CodeBlock.of("$L", objectIdentifier), attribute,
                                                                keyTypeBound, valueTypeBound));
            return;
        }

        String defaultProperty = resolver.getDefaultProperty(objectClass);
        if (defaultProperty != null) {
            processInstanceProperty(defaultProperty, new Value.Multi(defaultPropertyChildren));
            return;
        }


        throw new IllegalArgumentException("Unable to handle default children for class %s".formatted(objectClass));
    }

    private void processPropertyElements(String property, Collection<ClassInstanceElement> childNodes) {
        Method propertyGetter = resolver.resolveGetter(objectClass, property)
                                        .orElseThrow(() -> new IllegalStateException(
                                                "Cannot find getter for property %s".formatted(property)));
        Class<?> propertyClass = propertyGetter.getReturnType();

        if (Collection.class.isAssignableFrom(propertyClass)) {
            List<ClassInstanceElement> classInstanceElements = childNodes.stream().map(element -> {
                if (!(element instanceof ClassInstanceElement classInstanceElement)) {
                    throw new ParseException("property element contains a non common element");
                }

                return classInstanceElement;
            }).toList();

            Type genericReturnType = propertyGetter.getGenericReturnType();
            if (!(genericReturnType instanceof ParameterizedType parameterizedType)) {
                throw new IllegalArgumentException("Cannot determine bounds of collection");
            }

            Class<?>[] collectionTypeBounds = resolver.resolveUpperBoundTypeArguments(parameterizedType);
            if (collectionTypeBounds.length != 1) {
                throw new IllegalArgumentException("Cannot determine bounds of collection contents");
            }

            Class<?> contentTypeBound = collectionTypeBounds[0];
            classInstanceElements.forEach(element -> addToCollectionWithTypeBound(
                    CodeBlock.of("$L.$L()", objectIdentifier, propertyGetter.getName()), new Value.Element(element),
                    contentTypeBound));
            return;
        }

        throw new UnsupportedOperationException("Unknown read only property type %s".formatted(propertyClass));
    }

    private void addToCollectionWithTypeBound(CodeBlock collectionCodeBlock, Value.Single value,
                                              Class<?> contentTypeBound) {
        objectInitializationBuilder.addStatement("$L.add($L)", collectionCodeBlock,
                                                 coerceValue(contentTypeBound, value));
    }

    private CodeBlock coerceValue(Class<?> valueType, Value value) {
        return switch (value) {
            case Value.Element(ClassInstanceElement element) -> {
                ObjectNodeCode nodeCode = buildChildNode(element);
                if (!valueType.isAssignableFrom(nodeCode.nodeClass())) {
                    throw new IllegalArgumentException(
                            "Cannot assign %s to %s".formatted(nodeCode.nodeClass(), valueType));
                }
                yield CodeBlock.of("$L", nodeCode.nodeIdentifier());
            }
            case Value.Location ignored ->
                    throw new UnsupportedOperationException("Location resolution not yet supported");
            case Value.Reference(String reference) -> {
                Class<?> referenceClass = nameTracker.getStoredClassById(reference);
                if (!valueType.isAssignableFrom(referenceClass)) {
                    throw new IllegalArgumentException("Cannot assign %s to %s".formatted(referenceClass, valueType));
                }

                yield CodeBlock.of("$L", reference);
            }
            case Value.Resource(String resource) when valueType == String.class ->
                    CodeBlock.of("$1L.getString($2S)", FxmlProcessor.RESOURCES_NAME, resource);
            case Value.Literal(String val) when valueType == String.class -> CodeBlock.of("$S", val);
            case Value.Literal(String val) -> coerceValue(valueType, val);
            default -> throw new UnsupportedOperationException(
                    "Cannot create type %s from %s".formatted(valueType, value));
        };
    }

    private CodeBlock coerceValue(Class<?> valueType, String value) {
        if (valueType.isArray()) {
            Class<?> componentType = valueType.getComponentType();
            CodeBlock arrayInitializer = Arrays.stream(value.split(","))
                                               .map(componentString -> coerceValue(componentType, componentString))
                                               .collect(CodeBlock.joining(", "));
            return CodeBlock.of("new $T{$L}", valueType, arrayInitializer);
        }

        if (valueType == String.class) {
            return CodeBlock.of("$S", value);
        }

        if (valueType.isPrimitive()) {
            Class<?> boxedType = MethodType.methodType(valueType).wrap().returnType();
            Method method = resolver.findMethod(boxedType, "parse%s".formatted(boxedType.getSimpleName()), String.class)
                                    .orElse(null);
            if (method != null) {
                try {
                    return coerceUsingValueOfMethodResults(value, method, boxedType);
                } catch (IllegalAccessException | InvocationTargetException ignored) {}
            }
        }

        Class<?> boxedType = MethodType.methodType(valueType).wrap().returnType();
        Method method = resolver.findMethod(boxedType, "valueOf", String.class).orElse(null);
        if (method != null) {
            try {
                return coerceUsingValueOfMethodResults(value, method, boxedType);
            } catch (IllegalAccessException | InvocationTargetException ignored) {}
        }

        if (valueType == Object.class) {
            return CodeBlock.of("$S", value);
        }

        throw new UnsupportedOperationException("Cannot create type %s from %s".formatted(valueType, value));
    }
}
