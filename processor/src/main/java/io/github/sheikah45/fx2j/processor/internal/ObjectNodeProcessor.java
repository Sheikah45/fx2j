package io.github.sheikah45.fx2j.processor.internal;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import io.github.sheikah45.fx2j.processor.FxmlProcessor;
import io.github.sheikah45.fx2j.processor.ProcessorException;
import io.github.sheikah45.fx2j.processor.internal.model.FxmlNode;
import io.github.sheikah45.fx2j.processor.internal.model.NamedArgValue;
import io.github.sheikah45.fx2j.processor.internal.model.ObjectNodeCode;
import io.github.sheikah45.fx2j.processor.internal.utils.StringUtils;

import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
import java.util.stream.Stream;

public class ObjectNodeProcessor {
    private static final Map<Class<?>, Object> DEFAULTS_MAP = Map.of(byte.class, (byte) 0, short.class, (short) 0,
                                                                     int.class, 0, long.class, 0L, float.class, 0.0f,
                                                                     double.class, 0.0d, char.class, '\u0000',
                                                                     boolean.class, false);

    private static final Set<Class<?>> ALLOWED_LITERALS = Set.of(Byte.class, Short.class, Integer.class, Long.class,
                                                                 Character.class, Float.class, Double.class,
                                                                 Boolean.class);

    private static final Pattern BINDING_PATTERN = Pattern.compile("\\$\\{.*\\}");
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
    private final FxmlNode objectNode;
    private final NameTracker nameTracker;
    private final Path resourceRootPath;
    private final String rootPackage;
    private final ObjectNodeCode nodeCode;

    private final CodeBlock.Builder objectInitializationBuilder = CodeBlock.builder();
    private final Map<String, String> fxAttributes = new HashMap<>();
    private final Map<String, String> handlerAttributes = new HashMap<>();
    private final Map<String, String> propertyAttributes = new HashMap<>();
    private final Map<String, String> staticPropertyAttributes = new HashMap<>();
    private final Map<String, FxmlNode> propertyChildren = new HashMap<>();
    private final Map<String, FxmlNode> staticPropertyChildren = new HashMap<>();
    private final List<FxmlNode> definedChildren = new ArrayList<>();
    private final List<FxmlNode> defaultPropertyChildren = new ArrayList<>();

    private Class<?> objectClass;
    private Class<?>[] typeArguments;
    private String objectIdentifier;

    public ObjectNodeProcessor(FxmlNode objectNode, Class<?> controllerClass, ReflectionResolver resolver,
                               Path filePath,
                               Path resourceRootPath, String rootPackage) {
        this(objectNode, controllerClass, resolver, filePath, resourceRootPath, rootPackage, new NameTracker());
    }

    private ObjectNodeProcessor(FxmlNode objectNode, Class<?> controllerClass, ReflectionResolver resolver,
                                Path filePath,
                                Path resourceRootPath, String rootPackage, NameTracker nameTracker) {
        this.resourceRootPath = resourceRootPath;
        this.rootPackage = rootPackage;
        String nodeName = objectNode.name();
        if (!nodeName.equals("fx:root") && nodeName.matches("^[a-z]")) {
            throw new IllegalArgumentException("Node represents a property");
        }

        this.resolver = resolver;
        this.filePath = filePath;
        this.controllerClass = controllerClass;
        this.objectNode = objectNode;
        this.nameTracker = nameTracker;

        validateAttributes();
        validateChildren();
        nodeCode = processNode();
    }

    private ObjectNodeCode processNodeInternal() {
        processObjectInitialization();
        processDefinedChildren();
        processDefaultProperty();
        processPropertyAttributes();
        processPropertyChildren();
        processStaticPropertyAttributes();
        processStaticChildren();
        processHandlerAttributes();

        return new ObjectNodeCode(objectIdentifier, objectClass, objectInitializationBuilder.add("\n").build());
    }

    private ObjectNodeCode processNode() {
        try {
            return processNodeInternal();
        } catch (ProcessorException processorException) {
            throw processorException;
        } catch (Exception exception) {
            throw new ProcessorException(exception, objectNode.toString());
        }
    }

    private void validateAttributes() {
        this.objectNode.attributes().forEach((key, value) -> {
            if (key.startsWith("xmlns")) {
                return;
            }

            if (key.startsWith("fx:")) {
                fxAttributes.put(key.substring(3), value);
            } else if (key.startsWith("on")) {
                handlerAttributes.put(key, value);
            } else if (key.matches("^[a-z]\\w*")) {
                propertyAttributes.put(key, value);
            } else if (key.matches("^[A-Z]\\w*\\.\\w*")) {
                staticPropertyAttributes.put(key, value);
            } else {
                throw new IllegalArgumentException("Unknown property type %s".formatted(key));
            }
        });
    }

    private void validateChildren() {
        this.objectNode.children().forEach(child -> {
            String childName = child.name();
            if (childName.equals("fx:define")) {
                definedChildren.add(child);
            } else if (childName.matches("[a-z]\\w*")) {
                propertyChildren.put(childName, child);
            } else if (childName.matches("(\\w*\\.)+\\w*")) {
                if (resolver.isResolvable(childName)) {
                    defaultPropertyChildren.add(child);
                } else {
                    staticPropertyChildren.put(childName, child);
                }
            } else {
                defaultPropertyChildren.add(child);
            }
        });
    }

    public ObjectNodeCode getNodeCode() {
        return nodeCode;
    }

    private void processIncludeInitialization() {
        Path relativeSourcePath = Path.of(propertyAttributes.remove("source"));
        FxmlProcessor includedProcessor = new FxmlProcessor(filePath.resolveSibling(relativeSourcePath),
                                                            resourceRootPath, rootPackage, resolver.getClassLoader());
        objectClass = resolver.checkResolved(includedProcessor.getRootClass());

        if (objectClass == null) {
            throw new IllegalArgumentException(
                    "Unable to determine object class for %s".formatted(filePath.resolveSibling(relativeSourcePath)));
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
        if (includedControllerClass != Object.class && fxAttributes.containsKey("id")) {
            String controllerIdentifier = objectIdentifier + "Controller";
            objectInitializationBuilder.addStatement("$T $L = $L.getController()", includedControllerClass,
                                                     controllerIdentifier, builderIdentifier);
            processControllerSetter(controllerIdentifier, includedControllerClass);
        }
    }

    private void processHandlerAttributes() {
        handlerAttributes.forEach(this::processHandlerAttribute);
    }

    private void processConstructorInitialization() {
        objectClass = resolver.resolveRequired(objectNode.name());
        resolveIdentifier();

        for (List<NamedArgValue> constructorArgs : getMatchingConstructorArgs()) {
            try {
                buildWithConstructorArgs(constructorArgs);
                return;
            } catch (Exception ignored) {}
        }

        throw new IllegalArgumentException("Unknown constructor");
    }

    private void processPropertyAttributes() {
        if (Map.class.isAssignableFrom(objectClass)) {
            Class<?> keyTypeBound = typeArguments == null ? Object.class : typeArguments[0];
            Class<?> valueTypeBound = typeArguments == null ? Object.class : typeArguments[1];

            addAttributesToMapWithTypeBounds(objectIdentifier, propertyAttributes, keyTypeBound, valueTypeBound);
            return;
        }

        propertyAttributes.forEach(this::processPropertyAttribute);
    }

    private void addAttributesToMapWithTypeBounds(String identifier, Map<String, String> attributes,
                                                  Class<?> keyTypeBound, Class<?> valueTypeBound) {
        attributes.forEach((key, valueString) -> {
            CodeBlock keyValue = coerceValue(keyTypeBound, key);
            CodeBlock valueValue = coerceValue(valueTypeBound, valueString);
            objectInitializationBuilder.addStatement("$L.put($L, $L)", identifier, keyValue, valueValue);
        });
    }

    private void processStaticPropertyAttributes() {
        staticPropertyAttributes.forEach(this::processStaticPropertyAttribute);
    }

    private void processDefinedChildren() {
        definedChildren.stream().map(FxmlNode::children).flatMap(Collection::stream).forEach(this::buildChildNode);
    }

    private void processStaticChildren() {
        staticPropertyChildren.values().forEach(this::processStaticPropertyChild);
    }

    private void processPropertyChildren() {
        if (Map.class.isAssignableFrom(objectClass)) {
            Class<?> keyTypeBound = typeArguments == null ? Object.class : typeArguments[0];
            Class<?> valueTypeBound = typeArguments == null ? Object.class : typeArguments[1];
            addNodesToMapWithTypeBounds(objectIdentifier, propertyChildren.values(), keyTypeBound, valueTypeBound);
            return;
        }

        if (Collection.class.isAssignableFrom(objectClass)) {
            Class<?> contentTypeBound = typeArguments == null ? Object.class : typeArguments[0];
            addNodesToCollectionWithTypeBound(objectIdentifier, propertyChildren.values(), contentTypeBound);
            return;
        }

        propertyChildren.values().forEach(this::processPropertyChild);
    }

    private void processPropertyChild(FxmlNode propertyNode) {
        if (propertyNode.innerText() != null) {
            processPropertyAttribute(propertyNode.name(), propertyNode.innerText());
        }
        if (!propertyNode.children().isEmpty()) {
            processPropertyNestedChildren(propertyNode.name(), propertyNode.children());
        }
        if (!propertyNode.attributes().isEmpty()) {
            processAttributesOnProperty(propertyNode.name(), propertyNode.attributes());
        }
    }

    private void processStaticPropertyChild(FxmlNode propertyNode) {
        if (propertyNode.innerText() != null) {
            processStaticPropertyAttribute(propertyNode.name(), propertyNode.innerText());
        }
        if (!propertyNode.children().isEmpty()) {
            processStaticPropertyNestedChildren(propertyNode.name(), propertyNode.children());
        }
        if (!propertyNode.attributes().isEmpty()) {
            throw new UnsupportedOperationException("Attributes on static property children not supported");
        }
    }

    private void processObjectInitialization() {
        objectInitializationBuilder.add("\n");
        switch (objectNode.name()) {
            case "fx:root" -> processRootInitialization();
            case "fx:reference" -> processReferenceInitialization();
            case "fx:copy" -> processCopyInitialization();
            case "fx:include" -> processIncludeInitialization();
            default -> processAttributeBasedInitialization();
        }

        if (fxAttributes.containsKey("id")) {
            processControllerSetter(objectIdentifier, objectClass);
            resolver.resolveSetter(objectClass, "id", String.class)
                    .ifPresent(method -> objectInitializationBuilder.addStatement("$L.$L($S)", objectIdentifier,
                                                                                  method.getName(), objectIdentifier));
            processObjectTypeArguments();
        }
    }

    private void processObjectTypeArguments() {
        Method setterMethod = resolver.resolveSetter(controllerClass, objectIdentifier, objectClass).orElse(null);
        if (setterMethod != null) {
            Type genericParameterType = setterMethod.getGenericParameterTypes()[0];
            typeArguments = resolver.resolveUpperBoundTypeArguments(genericParameterType);
            return;
        }

        Field field = resolver.resolveField(controllerClass, objectIdentifier).orElse(null);
        if (field != null) {
            Type genericFieldType = field.getGenericType();
            typeArguments = resolver.resolveUpperBoundTypeArguments(genericFieldType);
        }
    }

    private void processControllerSetter(String identifier, Class<?> valueClass) {
        if (controllerClass != Object.class) {
            processControllerSettersFromKnownClass(identifier, valueClass);
        } else {
            throw new UnsupportedOperationException("fx:id without controller type not supported");
        }
    }

    private void resolveIdentifier() {
        String id = fxAttributes.get("id");
        if (id != null) {
            objectIdentifier = id;
        } else {
            objectIdentifier = nameTracker.getDeconflictedName(StringUtils.camelCase(objectClass.getSimpleName()));
        }
        nameTracker.storeIdClass(objectIdentifier, objectClass);
    }

    private void processCopyInitialization() {
        String sourceReference = propertyAttributes.remove("source");
        objectClass = nameTracker.getStoredClassById(sourceReference);
        if (!resolver.hasCopyConstructor(objectClass)) {
            throw new IllegalArgumentException("No copy constructor found for class %s".formatted(objectClass));
        }

        objectIdentifier = sourceReference + "Copy";
        objectInitializationBuilder.addStatement("$1T $2L = new $1T($3L)", objectClass, objectIdentifier,
                                                 sourceReference);
    }

    private void processReferenceInitialization() {
        objectIdentifier = propertyAttributes.remove("source");
        objectClass = nameTracker.getStoredClassById(objectIdentifier);
        if (!propertyAttributes.isEmpty() ||
            !staticPropertyAttributes.isEmpty() ||
            !fxAttributes.isEmpty() ||
            !objectNode.children().isEmpty()) {
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

    private void processRootInitialization() {
        objectIdentifier = FxmlProcessor.BUILDER_PROVIDED_ROOT_NAME;
        String rootType = propertyAttributes.remove("type");
        objectClass = resolver.resolveRequired(rootType);
    }

    private void processValueInitialization() {
        String value = fxAttributes.get("value");
        objectClass = resolver.resolveRequired(objectNode.name());
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

    private void processAttributeBasedInitialization() {
        if (fxAttributes.containsKey("factory")) {
            processFactoryBasedInitialization();
        } else if (fxAttributes.containsKey("constant")) {
            processConstantInitialization();
        } else if (fxAttributes.containsKey("value")) {
            processValueInitialization();
        } else {
            processConstructorInitialization();
        }
    }

    private Set<List<NamedArgValue>> getMatchingConstructorArgs() {
        Set<String> definedProperties = Stream.of(propertyAttributes.keySet(), propertyChildren.keySet())
                                              .flatMap(Collection::stream)
                                              .collect(Collectors.toSet());

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

    private void processConstantInitialization() {
        String constant = fxAttributes.get("constant");
        Class<?> constantContainerClass = resolver.resolveRequired(objectNode.name());
        objectClass = resolver.resolveFieldTypeRequiredPublic(constantContainerClass, constant).orElseThrow();
        resolveIdentifier();
        objectInitializationBuilder.addStatement("$T $L = $T.$L", objectClass, objectIdentifier, constantContainerClass,
                                                 constant);
    }

    private void processFactoryBasedInitialization() {
        String factory = fxAttributes.get("factory");
        Class<?> factoryClazz = resolver.resolveRequired(objectNode.name());
        Method factoryMethod = resolver.findMethod(factoryClazz, factory)
                                       .orElseThrow(() -> new IllegalArgumentException(
                                               "Factory method not found %s.%s".formatted(objectNode.name(), factory)));

        objectClass = factoryMethod.getReturnType();
        resolveIdentifier();

        objectInitializationBuilder.addStatement("$T $L = $T.$L()", objectClass, objectIdentifier, factoryClazz,
                                                 factoryMethod.getName());
    }

    private void buildWithConstructorArgs(List<NamedArgValue> namedArgValues) {
        CodeBlock parameterValues = namedArgValues.stream()
                                                  .map(this::resolveParameterValue)
                                                  .collect(CodeBlock.joining(", "));

        namedArgValues.stream().map(NamedArgValue::name).forEach(paramName -> {
            propertyAttributes.remove(paramName);
            propertyChildren.remove(paramName);
        });

        objectInitializationBuilder.addStatement("$T $L = new $T($L)", objectClass, objectIdentifier, objectClass,
                                                 parameterValues);
    }

    private CodeBlock resolveParameterValue(NamedArgValue namedArgValue) {
        Class<?> paramType = namedArgValue.parameterType();
        String paramName = namedArgValue.name();
        FxmlNode paramNode = propertyChildren.get(paramName);
        if (paramNode != null) {
            List<FxmlNode> paramChildren = paramNode.children();
            if (paramChildren.isEmpty()) {
                return coerceValue(paramType, paramNode.innerText());
            }

            if (paramChildren.size() == 1) {
                ObjectNodeCode nodeCode = buildChildNode(paramChildren.get(0));
                return CodeBlock.of("$L", nodeCode.nodeIdentifier());
            }

            throw new UnsupportedOperationException("Multiple children parameter construction not supported");
        }

        String attributeValue = propertyAttributes.get(paramName);
        if (attributeValue != null) {
            return coerceValue(paramType, attributeValue);
        }

        String defaultValue = namedArgValue.defaultValue();
        if (!defaultValue.isEmpty()) {
            return coerceValue(paramType, defaultValue);
        }

        return CodeBlock.of("$L", DEFAULTS_MAP.get(namedArgValue.parameterType()));
    }

    private List<List<NamedArgValue>> getPossibleConstructorArgs() {
        return Arrays.stream(objectClass.getConstructors())
                     .filter(resolver::hasAllNamedArgs)
                     .map(resolver::getNamedArgs)
                     .toList();
    }

    private void addNodesToMapWithTypeBounds(String mapIdentifier, Collection<FxmlNode> nodes, Class<?> keyTypeBound,
                                             Class<?> valueTypeBound) {
        for (FxmlNode propertyNode : nodes) {
            CodeBlock key = coerceValue(keyTypeBound, propertyNode.name());

            if (propertyNode.innerText() != null) {
                CodeBlock value = coerceValue(valueTypeBound, propertyNode.innerText());
                objectInitializationBuilder.addStatement("$L.put($L, $L)", mapIdentifier, key, value);
            }

            List<FxmlNode> children = propertyNode.children();
            if (!children.isEmpty()) {
                if (children.size() == 1) {
                    ObjectNodeCode nodeCode = buildChildNode(children.get(0));
                    if (!valueTypeBound.isAssignableFrom(nodeCode.nodeClass())) {
                        throw new IllegalArgumentException(
                                "Value type of %s can not be assigned to node type of %s".formatted(valueTypeBound,
                                                                                                    nodeCode.nodeClass()));
                    }

                    objectInitializationBuilder.addStatement("$L.put($L, $L)", mapIdentifier, key,
                                                             nodeCode.nodeIdentifier());
                } else {
                    throw new UnsupportedOperationException("Unable to create map value with multiple children");
                }
            }
        }
    }

    private void processHandlerAttribute(String key, String valueString) {
        Method handlerSetter = resolver.resolveSetterRequiredPublicIfExists(objectClass, key, resolver.resolveRequired(
                EVENT_HANDLER_CLASS)).orElse(null);
        if (handlerSetter != null) {
            processHandlerSetter(valueString, handlerSetter);
            return;
        }

        Matcher propertyMatcher = CHANGE_PROPERTY_PATTERN.matcher(key);
        if (propertyMatcher.matches()) {
            String property = propertyMatcher.group("property");
            processPropertyChangeListener(valueString, property);
            return;
        }

        throw new UnsupportedOperationException(
                "Unknown property %s for class %s".formatted(key, objectClass.getName()));
    }

    private void processPropertyChangeListener(String valueString, String property) {
        Method propertyMethod = resolver.resolveProperty(objectClass, property)
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                "Unable to find property method for %s".formatted(property)));

        Class<?> propertyClass = resolver.resolveGetter(objectClass, property)
                                         .map(Method::getReturnType)
                                         .orElseThrow(() -> new IllegalArgumentException(
                                                 "Unable to determine the class of property %s".formatted(property)));

        CodeBlock valueCode = resolveControllerPropertyChangeListener(propertyClass, valueString);
        objectInitializationBuilder.addStatement("$L.$L().addListener($L)", objectIdentifier, propertyMethod.getName(),
                                                 valueCode);
    }

    private void processHandlerSetter(String valueString, Method handlerSetter) {
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
        CodeBlock valueCode = resolveControllerEventHandler(eventClass, valueString);
        objectInitializationBuilder.addStatement("$L.$L($L)", objectIdentifier, handlerSetter.getName(), valueCode);
    }

    private void processAttributesOnProperty(String property, Map<String, String> attributes) {
        Method propertyGetter = resolver.resolveGetter(objectClass, property)
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                "Unable to find getter for property %s on class %s".formatted(property,
                                                                                                              objectClass)));

        Class<?> propertyClass = propertyGetter.getReturnType();

        attributes.forEach((key, value) -> {
            if (ON_CHANGE.equals(key)) {
                Method propertyMethod = resolver.resolveProperty(objectClass, property).orElse(null);
                if (propertyMethod != null) {
                    CodeBlock listener = resolveControllerPropertyChangeListener(propertyClass, value);
                    objectInitializationBuilder.addStatement("$L.$L().addListener($L)", objectIdentifier,
                                                             propertyMethod.getName(), listener);
                    return;
                }

                CodeBlock listener = resolveControllerContainerChangeListener(propertyGetter.getGenericReturnType(),
                                                                              value);
                objectInitializationBuilder.addStatement("$L.$L().addListener($L)", objectIdentifier,
                                                         propertyGetter.getName(), listener);
                return;
            }

            if (Map.class.isAssignableFrom(propertyClass)) {
                Class<?> keyClass = typeArguments == null ? Object.class : typeArguments[0];
                Class<?> valueClass = typeArguments == null ? Object.class : typeArguments[1];
                CodeBlock keyValue = coerceValue(keyClass, key);
                CodeBlock valueValue = coerceValue(valueClass, value);
                objectInitializationBuilder.addStatement("$L.$L().put($L, $L)", objectIdentifier,
                                                         propertyGetter.getName(), keyValue, valueValue);
            }
        });
    }

    private void processPropertyAttribute(String key, String valueString) {
        Matcher bindingMatcher = BINDING_PATTERN.matcher(valueString);
        if (bindingMatcher.matches()) {
            throw new UnsupportedOperationException("Binding expressions not supported");
        }

        Method propertySetter = resolver.resolveSetter(objectClass, key).orElse(null);
        if (propertySetter != null) {
            processPropertySetter(valueString, propertySetter);
            return;
        }

        Method propertyGetter = resolver.resolveGetter(objectClass, key).orElse(null);

        if (propertyGetter != null) {
            Type propertyGenericType = propertyGetter.getGenericReturnType();
            Class<?> propertyClass = propertyGetter.getReturnType();
            String propertyName = objectIdentifier + StringUtils.capitalize(key);
            if (Collection.class.isAssignableFrom(propertyClass) &&
                propertyGenericType instanceof ParameterizedType parameterizedType) {
                processCollectionInitialization(valueString, parameterizedType, propertyName, propertyGetter);
                return;
            }

            throw new IllegalArgumentException(
                    "Unable to process read only attribute of type %s".formatted(propertyClass));
        }

        throw new UnsupportedOperationException(
                "Unknown property %s for class %s".formatted(key, objectClass.getName()));
    }

    private void processCollectionInitialization(String valueString, ParameterizedType parameterizedType,
                                                 String propertyName, Method propertyGetter) {
        objectInitializationBuilder.addStatement("$T $L = $L.$L()",
                                                 resolver.resolveTypeNameWithoutVariables(parameterizedType),
                                                 propertyName, objectIdentifier, propertyGetter.getName());
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length != 1) {
            throw new IllegalArgumentException(
                    "Unable to resolve contained type for type %s".formatted(parameterizedType));
        }
        Class<?> containedClassBound = resolver.resolveTypeUpperBound(actualTypeArguments[0]);
        Arrays.stream(valueString.split(",\\s*"))
              .map(value -> coerceValue(containedClassBound, value))
              .forEach(codeBlock -> objectInitializationBuilder.addStatement("$L.add($L)", propertyName, codeBlock));
    }

    private void processPropertySetter(String valueString, Method propertySetter) {
        Class<?> valueType = propertySetter.getParameterTypes()[0];
        CodeBlock valueCode = coerceValue(valueType, valueString);
        objectInitializationBuilder.addStatement("$L.$L($L)", objectIdentifier, propertySetter.getName(), valueCode);
    }

    private void processStaticPropertyAttribute(String key, String valueString) {
        String[] propertyParts = key.split("\\.");
        String staticProperty = propertyParts[1];
        Class<?> staticPropertyClass = resolver.resolveRequired(propertyParts[0]);
        Method propertySetter = resolver.resolveStaticSetter(staticPropertyClass, staticProperty).orElse(null);
        if (propertySetter == null) {
            throw new IllegalArgumentException(
                    "Unable to find static setter for %s on %s".formatted(staticProperty, staticPropertyClass));
        }
        Class<?> objectType = propertySetter.getParameterTypes()[0];

        if (!objectType.isAssignableFrom(objectClass)) {
            throw new IllegalArgumentException("First parameter of static property setter does not match node type");
        }

        Class<?> valueType = propertySetter.getParameterTypes()[1];
        CodeBlock valueCode = coerceValue(valueType, valueString);
        objectInitializationBuilder.addStatement("$T.$L($L, $L)", staticPropertyClass, propertySetter.getName(),
                                                 objectIdentifier, valueCode);
    }

    private CodeBlock coerceValue(Class<?> valueType, String valueString) {
        if (valueType.isArray()) {
            Class<?> componentType = valueType.getComponentType();
            CodeBlock arrayInitializer = Arrays.stream(valueString.split(","))
                                               .map(componentString -> coerceValue(componentType, componentString))
                                               .collect(CodeBlock.joining(", "));
            return CodeBlock.of("new $T{$L}", valueType, arrayInitializer);
        }

        if (valueString.startsWith("$")) {
            String reference = valueString.substring(1);
            Class<?> referenceClass = nameTracker.getStoredClassById(reference);
            if (!valueType.isAssignableFrom(referenceClass)) {
                throw new IllegalArgumentException("Cannot assign %s to %s".formatted(referenceClass, valueType));
            }

            return CodeBlock.of("$L", reference);
        }

        if (valueType == String.class) {
            if (valueString.startsWith("%")) {
                return CodeBlock.of("$1L.getString($2S)", FxmlProcessor.RESOURCES_NAME, valueString.substring(1));
            }

            if (valueString.startsWith("\\")) {
                valueString = valueString.substring(1);
            }

            return CodeBlock.of("$S", valueString);
        }

        if (valueString.startsWith("\\")) {
            valueString = valueString.substring(1);
        }

        if (valueType.isPrimitive()) {
            Class<?> boxedType = MethodType.methodType(valueType).wrap().returnType();
            Method method = resolver.findMethod(boxedType, "parse%s".formatted(boxedType.getSimpleName()), String.class)
                                    .orElse(null);
            if (method != null) {
                try {
                    return coerceUsingValueOfMethodResults(valueString, method, boxedType);
                } catch (IllegalAccessException | InvocationTargetException ignored) {}
            }
        }

        Class<?> boxedType = MethodType.methodType(valueType).wrap().returnType();
        Method method = resolver.findMethod(boxedType, "valueOf", String.class).orElse(null);
        if (method != null) {
            try {
                return coerceUsingValueOfMethodResults(valueString, method, boxedType);
            } catch (IllegalAccessException | InvocationTargetException ignored) {}
        }

        if (valueType == Object.class) {
            return CodeBlock.of("$S", valueString);
        }

        throw new UnsupportedOperationException("Cannot create type %s from %s".formatted(valueType, valueString));
    }

    private ObjectNodeCode buildChildNode(FxmlNode childNode) {
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(childNode, controllerClass, resolver, filePath,
                                                          resourceRootPath, rootPackage, nameTracker
        ).getNodeCode();
        objectInitializationBuilder.add(nodeCode.objectInitializationCode());
        return nodeCode;
    }

    private CodeBlock resolveControllerEventHandler(Class<?> eventType, String valueString) {
        if (valueString.startsWith("#")) {
            String methodName = valueString.substring(1);
            return resolver.findMethod(controllerClass, methodName, eventType)
                           .map(method -> CodeBlock.of("$L::$L", FxmlProcessor.CONTROLLER_NAME, method.getName()))
                           .or(() -> resolver.findMethod(controllerClass, methodName)
                                             .map(method -> CodeBlock.of("event -> $L.$L()",
                                                                         FxmlProcessor.CONTROLLER_NAME,
                                                                         method.getName())))
                           .orElseThrow(() -> new IllegalArgumentException(
                                   "No method %s on %s".formatted(methodName, controllerClass)));
        }

        throw new IllegalArgumentException("No method for %s".formatted(valueString));
    }

    private CodeBlock resolveControllerContainerChangeListener(Type valueType, String valueString) {
        String methodName = valueString.substring(1);
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

    private CodeBlock resolveControllerPropertyChangeListener(Class<?> valueClass, String valueString) {
        if (!valueString.startsWith("#")) {
            throw new UnsupportedOperationException(
                    "Unknown value for change listener value %s".formatted(valueString));
        }
        String methodName = valueString.substring(1);
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

    private void processDefaultProperty() {
        if (defaultPropertyChildren.isEmpty() && objectNode.innerText() == null) {
            return;
        }

        if (Collection.class.isAssignableFrom(objectClass)) {
            Class<?> contentTypeBound = typeArguments == null ? Object.class : typeArguments[0];
            addNodesToCollectionWithTypeBound(objectIdentifier, defaultPropertyChildren, contentTypeBound);
            return;
        }

        if (Map.class.isAssignableFrom(objectClass)) {
            Class<?> keyTypeBound = typeArguments == null ? Object.class : typeArguments[0];
            Class<?> valueTypeBound = typeArguments == null ? Object.class : typeArguments[1];
            addNodesToMapWithTypeBounds(objectIdentifier, defaultPropertyChildren, keyTypeBound, valueTypeBound);
            return;
        }

        String defaultProperty = resolver.getDefaultProperty(objectClass);
        if (defaultProperty != null) {
            processPropertyChild(
                    new FxmlNode(defaultProperty, objectNode.innerText(), Map.of(), defaultPropertyChildren));
            return;
        }


        throw new IllegalArgumentException("Unable to handle default children for class %s".formatted(objectClass));
    }

    private void processPropertyNestedChildren(String property, Collection<FxmlNode> childNodes) {
        Map<Boolean, List<FxmlNode>> splitNodes = childNodes.stream()
                                                            .collect(Collectors.partitioningBy(
                                                                    node -> "fx:define".equals(node.name())));

        splitNodes.get(true).stream().map(FxmlNode::children).flatMap(Collection::stream).forEach(this::buildChildNode);

        List<FxmlNode> propertyNodes = splitNodes.get(false);
        Method propertySetter = resolver.resolveSetter(objectClass, property).orElse(null);
        if (propertySetter != null) {
            if (propertyNodes.size() != 1) {
                throw new UnsupportedOperationException(
                        "Cannot set property %s from multiple child nodes".formatted(property));
            }
            processPropertyChildSetter(propertyNodes.get(0), propertySetter);
            return;
        }

        Method propertyGetter = resolver.resolveGetter(objectClass, property).orElse(null);
        if (propertyGetter != null) {
            processReadOnlyProperty(property, propertyGetter, propertyNodes);
            return;
        }

        throw new UnsupportedOperationException("Unknown property element type for %s".formatted(property));
    }

    private void processReadOnlyProperty(String property, Method propertyGetter, Collection<FxmlNode> propertyNodes) {
        Class<?> propertyClass = propertyGetter.getReturnType();
        String propertyName = objectIdentifier + StringUtils.capitalize(property);

        if (Collection.class.isAssignableFrom(propertyClass)) {
            processPropertyNestedChildCollection(propertyGetter, propertyName, propertyNodes);
            return;
        }

        if (Map.class.isAssignableFrom(propertyClass)) {
            processPropertyNestedChildMap(propertyGetter, propertyName, propertyNodes);
            return;
        }

        throw new UnsupportedOperationException("Unknown read only property type %s".formatted(propertyClass));
    }

    private void processPropertyNestedChildMap(Method propertyGetter, String propertyName,
                                               Collection<FxmlNode> propertyNodes) {
        Type genericReturnType = propertyGetter.getGenericReturnType();
        objectInitializationBuilder.addStatement("$T $L = $L.$L()",
                                                 resolver.resolveTypeNameWithoutVariables(genericReturnType),
                                                 propertyName, objectIdentifier, propertyGetter.getName());

        if (!(genericReturnType instanceof ParameterizedType parameterizedType)) {
            throw new IllegalArgumentException("Cannot determine bounds of map key and value");
        }

        Class<?>[] mapTypeBounds = resolver.resolveUpperBoundTypeArguments(parameterizedType);
        if (mapTypeBounds.length != 2) {
            throw new IllegalArgumentException("Cannot determine bounds of map key and value");
        }

        Class<?> keyTypeBound = mapTypeBounds[0];
        Class<?> valueTypeBound = mapTypeBounds[1];
        addNodesToMapWithTypeBounds(propertyName, propertyNodes, keyTypeBound, valueTypeBound);
    }

    private void addNodesToCollectionWithTypeBound(String collectionIdentifier, Collection<FxmlNode> nodes,
                                                   Class<?> contentTypeBound) {
        for (FxmlNode propertyNode : nodes) {
            ObjectNodeCode nodeCode = buildChildNode(propertyNode);
            if (!contentTypeBound.isAssignableFrom(nodeCode.nodeClass())) {
                throw new IllegalArgumentException(
                        "Value type of %s can not be assigned to collection type of %s".formatted(contentTypeBound,
                                                                                                  nodeCode.nodeClass()));
            }

            objectInitializationBuilder.addStatement("$L.add($L)", collectionIdentifier, nodeCode.nodeIdentifier());
        }
    }

    private void processPropertyNestedChildCollection(Method propertyGetter, String propertyName,
                                                      Collection<FxmlNode> propertyNodes) {
        Type genericReturnType = propertyGetter.getGenericReturnType();
        objectInitializationBuilder.addStatement("$T $L = $L.$L()",
                                                 resolver.resolveTypeNameWithoutVariables(genericReturnType),
                                                 propertyName, objectIdentifier, propertyGetter.getName());

        if (!(genericReturnType instanceof ParameterizedType parameterizedType)) {
            throw new IllegalArgumentException("Cannot determine bounds of collection");
        }

        Class<?>[] collectionTypeBounds = resolver.resolveUpperBoundTypeArguments(parameterizedType);
        if (collectionTypeBounds.length != 1) {
            throw new IllegalArgumentException("Cannot determine bounds of collection contents");
        }

        Class<?> contentTypeBound = collectionTypeBounds[0];
        addNodesToCollectionWithTypeBound(propertyName, propertyNodes, contentTypeBound);
    }

    private void processPropertyChildSetter(FxmlNode propertyNode, Method propertySetter) {
        ObjectNodeCode nodeCode = buildChildNode(propertyNode);

        Class<?> parameterType = propertySetter.getParameterTypes()[0];
        if (!parameterType.isAssignableFrom(nodeCode.nodeClass())) {
            throw new IllegalArgumentException(
                    "Parameter type `%s` does not match node type `%s`".formatted(parameterType, nodeCode.nodeClass()));
        }
        objectInitializationBuilder.addStatement("$L.$L($L)", objectIdentifier, propertySetter.getName(),
                                                 nodeCode.nodeIdentifier());
    }

    private void processStaticPropertyNestedChildren(String property, List<FxmlNode> propertyNodes) {
        String[] propertyParts = property.split("\\.");
        String staticClass = propertyParts[0];
        String staticProperty = propertyParts[1];
        Class<?> staticPropertyClass = resolver.resolveRequired(staticClass);
        Method propertySetter = resolver.resolveStaticSetter(staticPropertyClass, staticProperty).orElse(null);

        if (propertySetter != null && propertyNodes.size() == 1) {
            processStaticPropertyChildSetter(propertyNodes.get(0), propertySetter, staticPropertyClass);
            return;
        }

        throw new UnsupportedOperationException("Read-only properties not supported for static properties");
    }

    private void processStaticPropertyChildSetter(FxmlNode propertyNode, Method propertySetter,
                                                  Class<?> staticPropertyClass) {
        Class<?> parameterType = propertySetter.getParameterTypes()[0];

        if (!parameterType.isAssignableFrom(objectClass)) {
            throw new IllegalArgumentException("First parameter of static property setter does not match node type");
        }

        ObjectNodeCode nodeCode = buildChildNode(propertyNode);
        objectInitializationBuilder.addStatement("$T.$L($L, $L)", staticPropertyClass, propertySetter.getName(),
                                                 objectIdentifier, nodeCode.nodeIdentifier());
    }
}
