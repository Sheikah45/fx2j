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
import io.github.sheikah45.fx2j.parser.property.Concrete;
import io.github.sheikah45.fx2j.parser.property.Expression;
import io.github.sheikah45.fx2j.parser.property.FxmlProperty;
import io.github.sheikah45.fx2j.parser.property.Handler;
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
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ObjectNodeProcessor {
    private static final Map<Class<?>, Object> DEFAULTS_MAP = Map.of(byte.class, (byte) 0, short.class, (short) 0,
                                                                     int.class, 0, long.class, 0L, float.class, 0.0f,
                                                                     double.class, 0.0d, char.class, '\u0000',
                                                                     boolean.class, false);

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

    private Type objectClass;
    private Class<?>[] typeArguments;
    private String objectIdentifier;

    public ObjectNodeProcessor(ClassInstanceElement rootNode, Class<?> controllerClass, ReflectionResolver resolver,
                               Path filePath, Path resourceRootPath, String rootPackage) {
        this.resourceRootPath = resourceRootPath;
        this.rootPackage = rootPackage;
        this.resolver = resolver;
        this.filePath = filePath;
        this.controllerClass = controllerClass;
        this.rootNode = rootNode;
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
                case FxmlAttribute.SpecialAttribute ignored -> {}
            }
        });

        rootNode.content().children().forEach(child -> {
            switch (child) {
                case InstancePropertyElement instance -> instanceProperties.put(instance.property(), instance);
                case StaticPropertyElement stat -> staticProperties.add(stat);
                case DefineElement(List<ClassInstanceElement> children) -> definedChildren.addAll(children);
                case ClassInstanceElement classInstanceElement ->
                        defaultPropertyChildren.add(new Concrete.Element(classInstanceElement));
                case ScriptElement script -> scripts.add(script);
            }
        });

        if (!(rootNode.content().body() instanceof Concrete.Empty)) {
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
        if (resolver.isAssignableFrom(Map.class, objectClass)) {
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
        if (!(property.value() instanceof Concrete concrete)) {
            throw new IllegalArgumentException("Cannot add non-concrete value to map");
        }
        CodeBlock keyValue = coerceValue(keyTypeBound, property.property());
        CodeBlock valueValue = coerceValue(valueTypeBound, concrete);
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
            case Concrete.Element(ClassInstanceElement classInstanceElement) ->
                    processPropertyClassInstanceElement(property, classInstanceElement);
            case Concrete.Attribute(FxmlProperty.Instance attribute) ->
                    processPropertiesOnProperty(property, List.of(attribute));
            case Value.Single single -> processInstancePropertySingle(property, single);
            case Value.Multi(List<? extends Value.Single> values) -> {
                List<ClassInstanceElement> elements = new ArrayList<>();
                List<FxmlProperty.Instance> properties = new ArrayList<>();

                values.forEach(val -> {
                    switch (val) {
                        case Concrete.Element(ClassInstanceElement element) -> elements.add(element);
                        case Concrete.Element(FxmlProperty.Instance element) -> properties.add(element);
                        case Concrete.Attribute(FxmlProperty.Instance attribute) -> properties.add(attribute);
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
            case Concrete.Element(ClassInstanceElement element) ->
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

        if (!resolver.isAssignableFrom(parameterType, objectClass)) {
            throw new IllegalArgumentException("First parameter of static property setter does not match node type");
        }

        ObjectNodeCode nodeCode = buildChildNode(staticProperty);
        if (!resolver.isAssignableFrom(propertySetter.getParameterTypes()[1], nodeCode.nodeClass())) {
            throw new IllegalArgumentException(
                    "Second parameter of static property setter %s does not match node type %s".formatted(
                            propertySetter, nodeCode.nodeClass()));
        }

        objectInitializationBuilder.addStatement("$T.$L($L, $L)", staticPropertyClass, propertySetter.getName(),
                                                 objectIdentifier, nodeCode.nodeIdentifier());
    }

    private ObjectNodeCode buildChildNode(ClassInstanceElement childNode) {
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(childNode, controllerClass, resolver, filePath,
                                                          resourceRootPath, rootPackage).getNodeCode();
        objectInitializationBuilder.add(nodeCode.objectInitializationCode());
        return nodeCode;
    }

    private boolean propertyIsMutable(String property) {
        return resolver.isAssignableFrom(Map.class, objectClass) ||
               resolver.resolveSetter(objectClass, property).isPresent() ||
               resolver.resolveGetter(objectClass, property)
                       .map(Method::getReturnType)
                       .map(returnType -> resolver.isAssignableFrom(Collection.class, returnType) ||
                                          resolver.isAssignableFrom(Map.class, returnType))
                       .orElse(false);
    }

    private CodeBlock coerceUsingValueOfMethodResults(String valueString, Method valueOfMethod, Type valueType)
            throws IllegalAccessException, InvocationTargetException {
        if (resolver.isPrimitive(valueType)) {
            Object value = valueOfMethod.invoke(null, valueString);
            if (Objects.equals(value, Double.POSITIVE_INFINITY) || Objects.equals(value, Float.POSITIVE_INFINITY)) {
                return CodeBlock.of("$T.POSITIVE_INFINITY", valueType);
            }

            if (Objects.equals(value, Double.NEGATIVE_INFINITY) || Objects.equals(value, Float.NEGATIVE_INFINITY)) {
                return CodeBlock.of("$T.NEGATIVE_INFINITY", valueType);
            }

            if (Objects.equals(value, Double.NaN) || Objects.equals(value, Float.NaN)) {
                return CodeBlock.of("$T.NaN", valueType);
            }

            return CodeBlock.of("$L", value);
        }

        if (resolver.isAssignableFrom(Enum.class, valueType)) {
            Object value = valueOfMethod.invoke(null, valueString);
            return CodeBlock.of("$T.$L", valueType, value);
        }

        return CodeBlock.of("$T.$L($S)", valueType, valueOfMethod.getName(), valueString);
    }

    private CodeBlock resolveParameterValue(NamedArgValue namedArgValue) {
        Class<?> paramType = namedArgValue.parameterType();
        String paramName = namedArgValue.name();
        FxmlProperty.Instance property = instanceProperties.get(paramName);
        Value value = property == null ? null : property.value();
        return switch (value) {
            case Concrete concrete -> coerceValue(paramType, concrete);
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
        return resolver.getConstructors(objectClass).stream()
                     .filter(resolver::hasAllNamedArgs)
                     .map(resolver::getNamedArgs)
                     .toList();
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
            case ValueElement(String className, Concrete.Literal(String value), ClassInstanceElement.Content ignored) ->
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

    private void processReferenceInitialization(String source, ClassInstanceElement.Content content) {
        objectIdentifier = source;
        objectClass = resolver.getStoredClassById(source);
        if (!content.attributes().isEmpty() || !content.children().isEmpty()) {
            throw new UnsupportedOperationException("References with children or attributes not supported");
        }
    }

    private void processConstantInitialization(String className, String member) {
        Class<?> constantContainerClass = resolver.resolveRequired(className);
        objectClass = resolver.resolveFieldTypeRequiredPublic(constantContainerClass, member).orElseThrow();
        resolveIdentifier();
        objectInitializationBuilder.addStatement("$T $L = $T.$L", objectClass, objectIdentifier, constantContainerClass,
                                                 member);
    }

    private void processCopyInitialization(String source) {
        objectClass = resolver.getStoredClassById(source);
        ;
        if (!resolver.hasCopyConstructor(objectClass)) {
            throw new IllegalArgumentException("No copy constructor found for class %s".formatted(objectClass));
        }

        objectIdentifier = source + "Copy";
        objectInitializationBuilder.addStatement("$1T $2L = new $1T($3L)", objectClass, objectIdentifier, source);
    }

    private void buildWithConstructorArgs(List<NamedArgValue> namedArgValues) {
        CodeBlock parameterValues = namedArgValues.stream()
                                                  .map(this::resolveParameterValue)
                                                  .collect(CodeBlock.joining(", "));

        namedArgValues.stream().map(NamedArgValue::name).forEach(instanceProperties::remove);

        objectInitializationBuilder.addStatement("$T $L = new $T($L)", objectClass, objectIdentifier, objectClass,
                                                 parameterValues);
    }

    private void processFactoryBasedInitialization(String factoryClassName, String factoryMethodName) {
        Class<?> factoryClass = resolver.resolveRequired(factoryClassName);
        Method factoryMethod = resolver.findMethod(factoryClass, factoryMethodName, 0)
                                       .orElseThrow(() -> new IllegalArgumentException(
                                               "Factory method not found %s.%s".formatted(factoryClassName,
                                                                                          factoryMethodName)));

        objectClass = factoryMethod.getReturnType();
        resolveIdentifier();

        objectInitializationBuilder.addStatement("$T $L = $T.$L()", objectClass, objectIdentifier, factoryClass,
                                                 factoryMethod.getName());
    }

    private void processControllerSetter(String identifier, Type valueClass) {
        if (controllerClass != Object.class) {
            processControllerSettersFromKnownClass(identifier, valueClass);
        }
    }

    private Class<?>[] extractTypeArguments() {
        return resolver.resolveSetter(controllerClass, objectIdentifier, objectClass)
                       .map(Method::getGenericParameterTypes)
                       .map(types -> types[0])
                       .or(() -> resolver.resolveField(controllerClass, objectIdentifier).map(Field::getGenericType))
                       .map(resolver::resolveUpperBoundTypeArguments)
                       .orElse(null);
    }

    private void resolveIdentifier() {
        if (id != null) {
            objectIdentifier = id;
            resolver.storeIdType(objectIdentifier, objectClass);
        } else {
            objectIdentifier = resolver.getDeconflictedName(objectClass);
        }
    }

    private void processControllerSettersFromKnownClass(String identifier, Type valueClass) {
        resolver.resolveSetterRequiredPublicIfExists(controllerClass, identifier, valueClass)
                .map(Method::getName)
                .map(methodName -> CodeBlock.of("$L.$L($L)", FxmlProcessor.CONTROLLER_NAME, methodName, identifier))
                .or(() -> resolver.resolveFieldRequiredPublic(controllerClass, identifier)
                                  .filter(field -> resolver.isAssignableFrom(field.getType(), valueClass))
                                  .map(Field::getName)
                                  .map(fieldName -> CodeBlock.of("$1L.$2L = $2L", FxmlProcessor.CONTROLLER_NAME,
                                                                 fieldName)))
                .ifPresent(objectInitializationBuilder::addStatement);
    }

    private void processInstancePropertySingle(String property, Value.Single value) {
        if (value instanceof Concrete.Empty) {
            return;
        }

        if (value instanceof Expression expression) {
            Method propertyMethod = resolver.resolveProperty(objectClass, property)
                                            .orElseThrow(() -> new IllegalArgumentException(
                                                    "No property found for expression binding %s".formatted(property)));
            processPropertyExpression(expression, propertyMethod);
            return;
        }

        if (value instanceof Concrete concrete) {
            Method propertySetter = resolver.resolveSetter(objectClass, property).orElse(null);
            if (propertySetter != null) {
                processPropertySetter(concrete, propertySetter);
                return;
            }
        }

        Method propertyGetter = resolver.resolveGetter(objectClass, property).orElse(null);

        if (propertyGetter != null) {
            Type propertyGenericType = propertyGetter.getGenericReturnType();
            Class<?> propertyClass = propertyGetter.getReturnType();
            String propertyName = objectIdentifier + StringUtils.capitalize(property);
            if (resolver.isAssignableFrom(Collection.class, propertyClass) &&
                propertyGenericType instanceof ParameterizedType parameterizedType &&
                value instanceof Concrete.Literal(String val)) {
                processCollectionInitialization(val, parameterizedType, propertyName, propertyGetter);
                return;
            }

            throw new IllegalArgumentException(
                    "Unable to process read only attribute of type %s".formatted(propertyClass));
        }

        throw new UnsupportedOperationException("Unknown property %s for class %s".formatted(property, objectClass));
    }

    private void processHandlerProperty(FxmlProperty.EventHandler eventHandler) {
        String eventName = eventHandler.eventName();
        Handler handler = eventHandler.handler();
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

        throw new UnsupportedOperationException("Unknown event %s for class %s".formatted(eventName, objectClass));
    }

    private void processHandlerSetter(Handler handler, Method handlerSetter) {
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

    private void processPropertyChangeListener(Handler handler, String property) {
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

    private CodeBlock resolveControllerEventHandler(Class<?> eventType, Handler handler) {
        if (!(handler instanceof Handler.Method(String methodName))) {
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
                                               .add("$L.$L(event);\n", FxmlProcessor.CONTROLLER_NAME, method.getName())
                                               .nextControlFlow("catch ($T e)", Exception.class)
                                               .add("throw new $T(e);\n", RuntimeException.class)
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

    private void processPropertySetter(Concrete value, Method propertySetter) {
        if (value instanceof Concrete.Empty) {
            return;
        }

        Class<?> valueType = propertySetter.getParameterTypes()[0];
        CodeBlock valueCode = coerceValue(valueType, value);
        objectInitializationBuilder.addStatement("$L.$L($L)", objectIdentifier, propertySetter.getName(), valueCode);
    }

    private void processPropertyExpression(Expression value, Method propertyMethod) {
        Type valueType = propertyMethod.getGenericReturnType();
        resolver.findMethod(valueType, "bind", 1);
        ExpressionResult result = initializeExpression(value);
        objectInitializationBuilder.addStatement("$L.$L().bind($L)", objectIdentifier, propertyMethod.getName(),
                                                 result.identifier());
    }

    private ExpressionResult initializeExpression(Expression value) {
        return switch (value) {
            case Expression.Null() -> new ExpressionResult(Object.class, null);
            case Expression.Whole(long val) -> {
                String identifier = resolver.getDeconflictedName(long.class);
                objectInitializationBuilder.addStatement("$T $L = $L", long.class, identifier, val);
                yield new ExpressionResult(long.class, identifier);
            }
            case Expression.Fraction(double val) -> {
                String identifier = resolver.getDeconflictedName(double.class);
                objectInitializationBuilder.addStatement("$T $L = $L", double.class, identifier, val);
                yield new ExpressionResult(double.class, identifier);
            }
            case Expression.Boolean(boolean val) -> {
                String identifier = resolver.getDeconflictedName(boolean.class);
                objectInitializationBuilder.addStatement("$T $L = $L", boolean.class, identifier, val);
                yield new ExpressionResult(boolean.class, identifier);
            }
            case Expression.Str(String val) -> {
                String identifier = resolver.getDeconflictedName(String.class);
                objectInitializationBuilder.addStatement("$T $L = $L", String.class, identifier, val);
                yield new ExpressionResult(String.class, identifier);
            }
            case Expression.Variable(String name) -> new ExpressionResult(resolver.getStoredTypeById(name), name);
            case Expression.PropertyRead(Expression expression, String property) -> {
                ExpressionResult expressionResult = initializeExpression(expression);
                Method readProperty = resolver.resolveProperty(expressionResult.type(), property)
                                              .orElseThrow(() -> new IllegalArgumentException(
                                                      "No property found for expression binding %s".formatted(
                                                              property)));
                Type valueType = readProperty.getGenericReturnType();
                String identifier = resolver.getDeconflictedName(valueType);
                objectInitializationBuilder.addStatement("$T $L = $L.$L()", valueType, identifier,
                                                         expressionResult.identifier(), readProperty.getName());
                yield new ExpressionResult(valueType, identifier);
            }
            case Expression.MethodCall(
                    Expression expression, String methodName, List<Expression> args
            ) -> {
                ExpressionResult expressionResult = initializeExpression(expression);
                List<ExpressionResult> argResults = args.stream().map(this::initializeExpression).toList();
                Type[] parameterTypes = argResults.stream()
                                                  .map(ExpressionResult::type)
                                                  .toArray(Type[]::new);
                Method method = resolver.findMethod(expressionResult.type(), methodName,
                                                    parameterTypes)
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                "No method found for class %s method name %s and parameters %s".formatted(
                                                        expressionResult.type(), methodName, parameterTypes)));
                Type valueType = method.getGenericReturnType();
                String identifier = resolver.getDeconflictedName(valueType);
                CodeBlock methodArgs = argResults.stream()
                                                 .map(ExpressionResult::identifier)
                                                 .map(ident -> CodeBlock.of("$S", ident))
                                                 .collect(CodeBlock.joining(", "));
                objectInitializationBuilder.addStatement("$T $L = $L.$L($L)", valueType, identifier,
                                                         expressionResult.identifier(), method.getName(), methodArgs);
                yield new ExpressionResult(valueType, identifier);
            }
            case Expression.CollectionAccess(Expression expression, Expression key) -> {
                ExpressionResult expressionResult = initializeExpression(expression);
                ExpressionResult keyResult = initializeExpression(key);
                Class<?> bindingsClass = resolver.resolveRequired("javafx.beans.binding.Bindings");
                Method valueAtMethod = resolver.findMethod(bindingsClass, "valueAt", expressionResult.type(),
                                                           keyResult.type())
                                               .orElseThrow(() -> new IllegalArgumentException(
                                                       "Unable to find method to access collection"));
                Type valueType = valueAtMethod.getGenericReturnType();
                String identifier = resolver.getDeconflictedName(valueType);
                objectInitializationBuilder.addStatement("$T $L = $T.$L($L, $L)", valueType, identifier, bindingsClass,
                                                         valueAtMethod.getName(), expressionResult.identifier(),
                                                         keyResult.identifier());
                yield new ExpressionResult(valueType, identifier);
            }
            case Expression.Add(Expression left, Expression right) ->
                    computeExpressionWithMethod(left, right, "add").or(
                                                                           () -> computeExpressionWithMethod(left, right, "concat"))
                                                                   .orElseThrow(() -> new IllegalArgumentException(
                                                                           "Cannot add %s and %s".formatted(left,
                                                                                                            right)));

            case Expression.Subtract(Expression left, Expression right) ->
                    computeExpressionWithMethod(left, right, "subtract").orElseThrow(
                            () -> new IllegalArgumentException("Cannot subtract %s and %s".formatted(left, right)));
            case Expression.Multiply(Expression left, Expression right) ->
                    computeExpressionWithMethod(left, right, "multiply").orElseThrow(
                            () -> new IllegalArgumentException("Cannot multiply %s and %s".formatted(left, right)));
            case Expression.Divide(Expression left, Expression right) ->
                    computeExpressionWithMethod(left, right, "divide").orElseThrow(
                            () -> new IllegalArgumentException("Cannot divide %s and %s".formatted(left, right)));
            case Expression.GreaterThan(Expression left, Expression right) ->
                    computeExpressionWithMethod(left, right, "greaterThan").orElseThrow(
                            () -> new IllegalArgumentException("Cannot greaterThan %s and %s".formatted(left, right)));
            case Expression.GreaterThanEqual(Expression left, Expression right) ->
                    computeExpressionWithMethod(left, right, "greaterThanEqual").orElseThrow(
                            () -> new IllegalArgumentException(
                                    "Cannot greaterThanEqual %s and %s".formatted(left, right)));
            case Expression.LessThan(Expression left, Expression right) ->
                    computeExpressionWithMethod(left, right, "lessThan").orElseThrow(
                            () -> new IllegalArgumentException("Cannot lessThan %s and %s".formatted(left, right)));
            case Expression.LessThanEqual(Expression left, Expression right) ->
                    computeExpressionWithMethod(left, right, "lessThanEqual").orElseThrow(
                            () -> new IllegalArgumentException(
                                    "Cannot lessThanEqual %s and %s".formatted(left, right)));
            case Expression.Equal(Expression left, Expression right) ->
                    computeExpressionWithMethod(left, right, "equal").orElseThrow(
                            () -> new IllegalArgumentException("Cannot equal %s and %s".formatted(left, right)));
            case Expression.NotEqual(Expression left, Expression right) ->
                    computeExpressionWithMethod(left, right, "notEqual").orElseThrow(
                            () -> new IllegalArgumentException("Cannot notEqual %s and %s".formatted(left, right)));
            case Expression.And(Expression left, Expression right) ->
                    computeExpressionWithMethod(left, right, "and").orElseThrow(
                            () -> new IllegalArgumentException("Cannot and %s and %s".formatted(left, right)));
            case Expression.Or(Expression left, Expression right) ->
                    computeExpressionWithMethod(left, right, "or").orElseThrow(
                            () -> new IllegalArgumentException("Cannot or %s and %s".formatted(left, right)));
            case Expression.Invert(Expression expression) -> computeExpressionWithMethod(expression, "not").orElseThrow(
                    () -> new IllegalArgumentException("Cannot not %s".formatted(expression)));
            case Expression.Negate(Expression expression) ->
                    computeExpressionWithMethod(expression, "negate").orElseThrow(
                            () -> new IllegalArgumentException("Cannot negate %s".formatted(expression)));
            case Expression.Modulo(Expression left, Expression right) ->
                    throw new UnsupportedOperationException("Modulo operation not supported");
        };
    }

    private Optional<ExpressionResult> computeExpressionWithMethod(Expression left, Expression right,
                                                                   String methodName) {
        ExpressionResult leftResult = initializeExpression(left);
        ExpressionResult rightResult = initializeExpression(right);

        Method directMethod = resolver.findMethod(leftResult.type(), methodName, rightResult.type()).orElse(null);
        if (directMethod != null) {
            Type valueType = directMethod.getGenericReturnType();
            String identifier = resolver.getDeconflictedName(valueType);
            objectInitializationBuilder.addStatement("$T $L = $L.$L($L)", valueType, identifier,
                                                     leftResult.identifier(), directMethod.getName(),
                                                     rightResult.identifier());
            return Optional.of(new ExpressionResult(valueType, identifier));
        }

        Class<?> bindingsClass = resolver.resolveRequired("javafx.beans.binding.Bindings");
        Method indirectMethod = resolver.findMethod(bindingsClass, methodName, leftResult.type(), rightResult.type())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                "Unable to find method to combine expressions"));
        if (indirectMethod != null) {
            Type valueType = indirectMethod.getGenericReturnType();
            String identifier = resolver.getDeconflictedName(valueType);
            objectInitializationBuilder.addStatement("$T $L = $T.$L($L, $L)", valueType, identifier, bindingsClass,
                                                     indirectMethod.getName(), leftResult.identifier(),
                                                     rightResult.identifier());
            return Optional.of(new ExpressionResult(valueType, identifier));
        }

        return Optional.empty();
    }

    private Optional<ExpressionResult> computeExpressionWithMethod(Expression value, String methodName) {
        ExpressionResult result = initializeExpression(value);

        Method directMethod = resolver.findMethod(result.type(), methodName).orElse(null);
        if (directMethod != null) {
            Type valueType = directMethod.getGenericReturnType();
            String identifier = resolver.getDeconflictedName(valueType);
            objectInitializationBuilder.addStatement("$T $L = $L.$L()", valueType, identifier, result.identifier(),
                                                     directMethod.getName());
            return Optional.of(new ExpressionResult(valueType, identifier));
        }

        Class<?> bindingsClass = resolver.resolveRequired("javafx.beans.binding.Bindings");
        Method indirectMethod = resolver.findMethod(bindingsClass, methodName, result.type()).orElse(null);
        if (indirectMethod != null) {
            Type valueType = indirectMethod.getGenericReturnType();
            String identifier = resolver.getDeconflictedName(valueType);
            objectInitializationBuilder.addStatement("$T $L = $T.$L($L)", valueType, identifier, bindingsClass,
                                                     indirectMethod.getName(), result.identifier());
            return Optional.of(new ExpressionResult(valueType, identifier));
        }

        return Optional.empty();
    }

    private void processStaticPropertySingle(String className, String property, Value value) {
        if (!(value instanceof Concrete concrete)) {
            throw new IllegalArgumentException("Cannot set static property with non concrete value");
        }

        Class<?> staticPropertyClass = resolver.resolveRequired(className);
        Method propertySetter = resolver.resolveStaticSetter(staticPropertyClass, property).orElse(null);
        if (propertySetter == null) {
            throw new IllegalArgumentException(
                    "Unable to find static setter for %s on %s".formatted(property, staticPropertyClass));
        }
        Class<?> objectType = propertySetter.getParameterTypes()[0];

        if (!resolver.isAssignableFrom(objectType, objectClass)) {
            throw new IllegalArgumentException("First parameter of static property setter does not match node type");
        }

        Class<?> valueType = propertySetter.getParameterTypes()[1];
        CodeBlock valueCode = coerceValue(valueType, concrete);
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
                    Handler handler = eventHandler.handler();

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

    private CodeBlock resolveControllerContainerChangeListener(Type valueType, Handler handler) {
        if (!(handler instanceof Handler.Method(String methodName))) {
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

    private void processPropertyClassInstanceElement(String property, ClassInstanceElement element) {
        Method propertySetter = resolver.resolveSetter(objectClass, property).orElse(null);
        if (propertySetter != null) {
            processPropertyChildSetter(element, propertySetter);
        } else {
            processPropertyElements(property, List.of(element));
        }
    }

    private void processPropertyContainerListener(Method propertyGetter, Handler handler) {
        CodeBlock listener = resolveControllerContainerChangeListener(propertyGetter.getGenericReturnType(), handler);
        objectInitializationBuilder.addStatement("$L.$L().addListener($L)", objectIdentifier, propertyGetter.getName(),
                                                 listener);
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
                      CodeBlock.of("$L.$L()", objectIdentifier, propertyGetter.getName()), new Concrete.Literal(val),
                      containedClassBound));
    }

    private void processPropertyChildSetter(ClassInstanceElement propertyNode, Method propertySetter) {
        ObjectNodeCode nodeCode = buildChildNode(propertyNode);

        Class<?> parameterType = propertySetter.getParameterTypes()[0];
        if (!resolver.isAssignableFrom(parameterType, nodeCode.nodeClass())) {
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

        if (resolver.isAssignableFrom(Collection.class, objectClass)) {
            Class<?> contentTypeBound = typeArguments == null ? Object.class : typeArguments[0];
            defaultPropertyChildren.forEach(value -> {
                if (!(value instanceof Concrete concrete)) {
                    throw new IllegalArgumentException("Cannot add non concrete value to collection");
                }
                addToCollectionWithTypeBound(CodeBlock.of("$L", objectIdentifier), concrete, contentTypeBound);
            });
            return;
        }

        if (resolver.isAssignableFrom(Map.class, objectClass)) {
            List<FxmlProperty.Instance> properties = defaultPropertyChildren.stream().map(value -> {
                if (value instanceof Concrete.Element(FxmlProperty.Instance element)) {
                    return element;
                }

                if (value instanceof Concrete.Attribute(FxmlProperty.Instance attribute)) {
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

        if (resolver.isAssignableFrom(Collection.class, propertyClass)) {
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
                    CodeBlock.of("$L.$L()", objectIdentifier, propertyGetter.getName()), new Concrete.Element(element),
                    contentTypeBound));
            return;
        }

        throw new UnsupportedOperationException("Unknown read only property type %s".formatted(propertyClass));
    }

    private void addToCollectionWithTypeBound(CodeBlock collectionCodeBlock, Concrete value,
                                              Class<?> contentTypeBound) {
        objectInitializationBuilder.addStatement("$L.add($L)", collectionCodeBlock,
                                                 coerceValue(contentTypeBound, value));
    }

    private CodeBlock coerceValue(Type valueType, Concrete value) {
        return switch (value) {
            case Concrete.Element(ClassInstanceElement element) -> {
                ObjectNodeCode nodeCode = buildChildNode(element);
                if (!resolver.isAssignableFrom(resolver.resolveClassFromType(valueType), nodeCode.nodeClass())) {
                    throw new IllegalArgumentException(
                            "Cannot assign %s to %s".formatted(nodeCode.nodeClass(), valueType));
                }
                yield CodeBlock.of("$L", nodeCode.nodeIdentifier());
            }
            case Concrete.Location ignored ->
                    throw new UnsupportedOperationException("Location resolution not yet supported");
            case Concrete.Reference(String reference) -> {
                Class<?> referenceClass = resolver.getStoredClassById(reference);
                if (!resolver.isAssignableFrom(resolver.resolveClassFromType(valueType), referenceClass)) {
                    throw new IllegalArgumentException("Cannot assign %s to %s".formatted(referenceClass, valueType));
                }

                yield CodeBlock.of("$L", reference);
            }
            case Concrete.Resource(String resource) when valueType == String.class ->
                    CodeBlock.of("$1L.getString($2S)", FxmlProcessor.RESOURCES_NAME, resource);
            case Concrete.Literal(String val) when valueType == String.class -> CodeBlock.of("$S", val);
            case Concrete.Literal(String val) -> coerceValue(valueType, val);
            default -> throw new UnsupportedOperationException(
                    "Cannot create type %s from %s".formatted(valueType, value));
        };
    }

    private CodeBlock coerceValue(Type valueType, String value) {
        Class<?> rawType = resolver.resolveClassFromType(valueType);
        if (rawType.isArray()) {
            Class<?> componentType = rawType.getComponentType();
            CodeBlock arrayInitializer = Arrays.stream(value.split(","))
                                               .map(componentString -> coerceValue(componentType, componentString))
                                               .collect(CodeBlock.joining(", "));
            return CodeBlock.of("new $T{$L}", valueType, arrayInitializer);
        }

        if (rawType == String.class) {
            return CodeBlock.of("$S", value);
        }

        if (rawType.isPrimitive()) {
            Class<?> boxedType = MethodType.methodType(rawType).wrap().returnType();
            Method method = resolver.findMethod(boxedType, "parse%s".formatted(boxedType.getSimpleName()), String.class)
                                    .orElse(null);
            if (method != null) {
                try {
                    return coerceUsingValueOfMethodResults(value, method, boxedType);
                } catch (IllegalAccessException | InvocationTargetException ignored) {}
            }
        }

        Class<?> boxedType = MethodType.methodType(rawType).wrap().returnType();
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

    private record ExpressionResult(Type type, String identifier) {}
}
