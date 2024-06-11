package io.github.sheikah45.fx2j.processor.internal;

import io.github.sheikah45.fx2j.parser.attribute.EventHandlerAttribute;
import io.github.sheikah45.fx2j.parser.attribute.IdAttribute;
import io.github.sheikah45.fx2j.parser.attribute.InstancePropertyAttribute;
import io.github.sheikah45.fx2j.parser.attribute.StaticPropertyAttribute;
import io.github.sheikah45.fx2j.parser.element.ConstantElement;
import io.github.sheikah45.fx2j.parser.element.CopyElement;
import io.github.sheikah45.fx2j.parser.element.ElementContent;
import io.github.sheikah45.fx2j.parser.element.FactoryElement;
import io.github.sheikah45.fx2j.parser.element.IncludeElement;
import io.github.sheikah45.fx2j.parser.element.InstanceElement;
import io.github.sheikah45.fx2j.parser.element.InstancePropertyElement;
import io.github.sheikah45.fx2j.parser.element.ReferenceElement;
import io.github.sheikah45.fx2j.parser.element.RootElement;
import io.github.sheikah45.fx2j.parser.element.StaticPropertyElement;
import io.github.sheikah45.fx2j.parser.element.ValueElement;
import io.github.sheikah45.fx2j.parser.property.BindExpression;
import io.github.sheikah45.fx2j.parser.property.Handler;
import io.github.sheikah45.fx2j.parser.property.Value;
import io.github.sheikah45.fx2j.processor.FxmlProcessor;
import io.github.sheikah45.fx2j.processor.internal.code.CodeValues;
import io.github.sheikah45.fx2j.processor.internal.code.Expression;
import io.github.sheikah45.fx2j.processor.internal.code.Statement;
import io.github.sheikah45.fx2j.processor.internal.code.StatementExpression;
import io.github.sheikah45.fx2j.processor.internal.model.ObjectNodeCode;
import io.github.sheikah45.fx2j.processor.internal.resolve.ResolverContainer;
import io.github.sheikah45.fx2j.processor.testcontroller.ChangeHandlerController;
import io.github.sheikah45.fx2j.processor.testcontroller.EventHandlerMethodController;
import io.github.sheikah45.fx2j.processor.testcontroller.PublicController;
import io.github.sheikah45.fx2j.processor.testutils.CopyObject;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
class ObjectNodeProcessorTest {

    private static final ElementContent<?, ?> EMPTY_CONTENT = new ElementContent<>(List.of(), List.of(),
                                                                                   new Value.Empty());
    private static final Path EMPTY_PATH = Path.of("");

    private final ResolverContainer resolverContainer = ResolverContainer.from(Set.of(), getClass().getClassLoader());

    @Test
    void testRootInitialization() {
        Class<?> objectClass = Integer.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(
                new RootElement(objectClass.getCanonicalName(), EMPTY_CONTENT), Object.class, resolverContainer,
                EMPTY_PATH, EMPTY_PATH, "").getNodeCode();
        Expression.Variable variable = CodeValues.variable(FxmlProcessor.BUILDER_PROVIDED_ROOT_NAME);
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertTrue(initializers.isEmpty());
    }

    @Test
    void testReferenceInitialization() {
        Class<?> objectClass = Integer.class;
        resolverContainer.getNameResolver().storeIdType("obj", objectClass);
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new ReferenceElement("obj", EMPTY_CONTENT), Object.class,
                                                          resolverContainer, EMPTY_PATH, EMPTY_PATH, "").getNodeCode();
        Expression.Variable variable = CodeValues.variable("obj");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertTrue(initializers.isEmpty());
    }

    @Test
    void testIncludeInitialization() {
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(
                new IncludeElement(Path.of("constant.fxml"), null, StandardCharsets.UTF_8, EMPTY_CONTENT), Object.class,
                resolverContainer, Path.of("src/test/resources/fxml/process/include.fxml"),
                Path.of("src/test/resources"), "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("integer0");
        Class<?> objectClass = Integer.class;
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(3, initializers.size());
        Statement builderDeclaration = initializers.getFirst();
        String builderClassName = "fxml.process.ConstantBuilder";
        String builderIdentifier = "integer0Builder";
        assertEquals(
                CodeValues.declaration(builderClassName, builderIdentifier, CodeValues.newInstance(builderClassName)),
                builderDeclaration);
        Statement build = initializers.get(1);
        assertEquals(CodeValues.methodCall(builderIdentifier, "build", null, null,
                                           CodeValues.variable(FxmlProcessor.RESOURCES_NAME),
                                           CodeValues.variable(FxmlProcessor.CONTROLLER_FACTORY_NAME)), build);
        Statement declaration = initializers.getLast();
        assertEquals(CodeValues.declaration(objectClass, variable, CodeValues.methodCall(builderIdentifier, "getRoot")),
                     declaration);
    }

    @Test
    void testIncludeInitializationWithController() {
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(
                new IncludeElement(Path.of("public-controller.fxml"), null, StandardCharsets.UTF_8,
                                   new ElementContent<>(List.of(new IdAttribute("pane")), List.of(),
                                                        new Value.Empty())), Object.class, resolverContainer,
                Path.of("src/test/resources/fxml/controller/include.fxml"), Path.of("src/test/resources"),
                "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("pane");
        Class<?> objectClass = AnchorPane.class;
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(5, initializers.size());
        Statement builderDeclaration = initializers.getFirst();
        String builderClassName = "fxml.controller.PublicControllerBuilder";
        String builderIdentifier = "paneBuilder";
        assertEquals(
                CodeValues.declaration(builderClassName, builderIdentifier, CodeValues.newInstance(builderClassName)),
                builderDeclaration);
        Statement build = initializers.get(1);
        assertEquals(CodeValues.methodCall(builderIdentifier, "build", null, null,
                                           CodeValues.variable(FxmlProcessor.RESOURCES_NAME),
                                           CodeValues.variable(FxmlProcessor.CONTROLLER_FACTORY_NAME)), build);
        Statement declaration = initializers.get(2);
        assertEquals(CodeValues.declaration(objectClass, variable, CodeValues.methodCall(builderIdentifier, "getRoot")),
                     declaration);
        Statement controllerDeclaration = initializers.get(3);
        assertEquals(CodeValues.declaration(PublicController.class, "paneController",
                                            CodeValues.methodCall(builderIdentifier, "getController")),
                     controllerDeclaration);
    }

    @Test
    void testCopyInitialization() {
        Class<?> objectClass = CopyObject.class;
        resolverContainer.getNameResolver().storeIdType("obj", objectClass);
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new CopyElement("obj", EMPTY_CONTENT), Object.class,
                                                          resolverContainer, EMPTY_PATH, EMPTY_PATH, "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("objCopy");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(1, initializers.size());
        Statement statement = initializers.getFirst();
        assertEquals(CodeValues.declaration(objectClass, variable,
                                            CodeValues.newInstance(objectClass, CodeValues.variable("obj"))),
                     statement);
    }

    @Test
    void testFactoryInitialization() {
        Class<?> objectClass = List.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(
                new FactoryElement(objectClass.getCanonicalName(), "of", EMPTY_CONTENT), Object.class,
                resolverContainer, EMPTY_PATH, EMPTY_PATH, "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("list0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(1, initializers.size());
        Statement statement = initializers.getFirst();
        assertEquals(CodeValues.declaration(objectClass, variable, CodeValues.methodCall(objectClass, "of")),
                     statement);
    }

    @Test
    void testConstantInitialization() {
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(
                new ConstantElement("java.lang.Double", "MAX_EXPONENT", EMPTY_CONTENT), Object.class, resolverContainer,
                EMPTY_PATH, EMPTY_PATH, "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("int0");
        Class<?> objectClass = int.class;
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(1, initializers.size());
        Statement statement = initializers.getFirst();
        assertEquals(
                CodeValues.declaration(objectClass, variable, CodeValues.fieldAccess(Double.class, "MAX_EXPONENT")),
                statement);
    }

    @Test
    void testValueInitialization() {
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new ValueElement("java.lang.Double", "1", EMPTY_CONTENT),
                                                          Object.class, resolverContainer, EMPTY_PATH, EMPTY_PATH,
                                                          "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("double0");
        Class<?> objectClass = double.class;
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(1, initializers.size());
        Statement statement = initializers.getFirst();
        assertEquals(CodeValues.declaration(objectClass, variable, CodeValues.literal(1d)), statement);
    }

    @Test
    void testValueInitializationString() {
        Class<?> objectClass = String.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(
                new ValueElement(objectClass.getCanonicalName(), "1", EMPTY_CONTENT), Object.class, resolverContainer,
                EMPTY_PATH, EMPTY_PATH, "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("string0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(1, initializers.size());
        Statement statement = initializers.getFirst();
        assertEquals(CodeValues.declaration(objectClass, variable, CodeValues.literal("1")), statement);
    }

    @Test
    void testObjectParameterInitialization() {
        Class<?> objectClass = Insets.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(
                                                                                      List.of(new InstancePropertyAttribute(
                                                                                                      "top",
                                                                                                      new Value.Literal("10")),
                                                                                              new InstancePropertyAttribute(
                                                                                                      "bottom",
                                                                                                      new Value.Empty())),
                                                                                      List.of(new InstancePropertyElement(
                                                                                                      "left",
                                                                                                      new ElementContent<>(
                                                                                                              List.of(),
                                                                                                              List.of(),
                                                                                                              new Value.Literal(
                                                                                                                      "20"))),
                                                                                              new InstancePropertyElement(
                                                                                                      "right",
                                                                                                      new ElementContent<>(
                                                                                                              List.of(),
                                                                                                              List.of(new ValueElement(
                                                                                                                      Double.class.getCanonicalName(),
                                                                                                                      "30",
                                                                                                                      EMPTY_CONTENT)),
                                                                                                              new Value.Empty()))),
                                                                                      new Value.Empty())), Object.class,
                                                          resolverContainer, EMPTY_PATH, EMPTY_PATH, "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("insets0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(3, initializers.size());
        Statement lineBreak = initializers.getFirst();
        assertInstanceOf(Statement.LineBreak.class, lineBreak);
        Statement rightDeclaration = initializers.get(1);
        Expression.Variable rightVariable = CodeValues.variable("double0");
        assertEquals(CodeValues.declaration(double.class, rightVariable, 30d), rightDeclaration);
        Statement statement = initializers.getLast();
        assertEquals(CodeValues.declaration(objectClass, variable,
                                            CodeValues.newInstance(objectClass, 10d, rightVariable, 0d, 20d)),
                     statement);
    }

    @Test
    void testObjectParameterInitializationEmptyElement() {
        Class<?> objectClass = Insets.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(List.of(),
                                                                                                   List.of(new InstancePropertyElement(
                                                                                                           "top",
                                                                                                           new ElementContent<>(
                                                                                                                   List.of(),
                                                                                                                   List.of(),
                                                                                                                   new Value.Empty()))),
                                                                                                   new Value.Empty())),
                                                          Object.class, resolverContainer, EMPTY_PATH, EMPTY_PATH,
                                                          "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("insets0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(1, initializers.size());
        Statement statement = initializers.getLast();
        assertEquals(CodeValues.declaration(objectClass, variable, CodeValues.newInstance(objectClass, 0d, 0d, 0d, 0d)),
                     statement);
    }

    @Test
    void testObjectDefaultInitialization() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(
                new InstanceElement(objectClass.getCanonicalName(), EMPTY_CONTENT), Object.class, resolverContainer,
                EMPTY_PATH, EMPTY_PATH, "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(1, initializers.size());
        Statement statement = initializers.getFirst();
        assertEquals(CodeValues.declaration(objectClass, variable, CodeValues.newInstance(objectClass)), statement);
    }

    @Test
    void testDefaultValue() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(List.of(), List.of(),
                                                                                                   new Value.Literal(
                                                                                                           "test"))),
                                                          Object.class, resolverContainer, EMPTY_PATH, EMPTY_PATH,
                                                          "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(2, initializers.size());
        Statement statement = initializers.getLast();
        assertEquals(CodeValues.methodCall(variable, "setText", "test"), statement);
    }

    @Test
    void testDefaultElement() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(List.of(),
                                                                                                   List.of(new ValueElement(
                                                                                                           String.class.getCanonicalName(),
                                                                                                           "test",
                                                                                                           EMPTY_CONTENT)),
                                                                                                   new Value.Empty())),
                                                          Object.class, resolverContainer, EMPTY_PATH, EMPTY_PATH,
                                                          "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(4, initializers.size());
        Statement lineBreak = initializers.get(1);
        assertInstanceOf(Statement.LineBreak.class, lineBreak);
        Statement textNode = initializers.get(2);
        Expression.Variable textVariable = CodeValues.variable("string0");
        assertEquals(CodeValues.declaration(String.class, textVariable, "test"), textNode);
        Statement statement = initializers.getLast();
        assertEquals(CodeValues.methodCall(variable, "setText", textVariable), statement);
    }

    @Test
    void testCollectionDefaults() {
        Class<?> objectClass = ArrayList.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(List.of(),
                                                                                                   List.of(new ValueElement(
                                                                                                           String.class.getCanonicalName(),
                                                                                                           "test",
                                                                                                           EMPTY_CONTENT)),
                                                                                                   new Value.Literal(
                                                                                                           "temp"))),
                                                          Object.class, resolverContainer, EMPTY_PATH, EMPTY_PATH,
                                                          "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("arrayList0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(5, initializers.size());
        Statement lineBreak = initializers.get(1);
        assertInstanceOf(Statement.LineBreak.class, lineBreak);
        Statement stringNode = initializers.get(2);
        Expression.Variable stringVariable = CodeValues.variable("string0");
        assertEquals(CodeValues.declaration(String.class, stringVariable, "test"), stringNode);
        Statement testAdd = initializers.get(3);
        assertEquals(CodeValues.methodCall(variable, "add", stringVariable), testAdd);
        Statement tempAdd = initializers.getLast();
        assertEquals(CodeValues.methodCall(variable, "add", "temp"), tempAdd);
    }

    @Test
    void testPropertyCollectionPropertyElement() {
        Class<?> objectClass = VBox.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(List.of(),
                                                                                                   List.of(new InstancePropertyElement(
                                                                                                           "children",
                                                                                                           new ElementContent<>(
                                                                                                                   List.of(),
                                                                                                                   List.of(new InstanceElement(
                                                                                                                           Button.class.getCanonicalName(),
                                                                                                                           EMPTY_CONTENT)),
                                                                                                                   new Value.Empty()))),
                                                                                                   new Value.Empty())),
                                                          Object.class, resolverContainer, EMPTY_PATH, EMPTY_PATH,
                                                          "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("vBox0");
        StatementExpression.MethodCall getChildren = CodeValues.methodCall(variable, "getChildren");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(4, initializers.size());
        Statement lineBreak = initializers.get(1);
        assertInstanceOf(Statement.LineBreak.class, lineBreak);
        Expression.Variable buttonVariable = CodeValues.variable("button0");
        Statement buttonDeclaration = initializers.get(2);
        assertEquals(CodeValues.declaration(Button.class, buttonVariable, CodeValues.newInstance(Button.class)),
                     buttonDeclaration);
        Statement put1 = initializers.getLast();
        assertEquals(CodeValues.methodCall(getChildren, "add", buttonVariable), put1);
    }

    @Test
    void testPropertyCollectionPropertyElementValue() {
        Class<?> objectClass = VBox.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(List.of(),
                                                                                                   List.of(new InstancePropertyElement(
                                                                                                           "styleClass",
                                                                                                           new ElementContent<>(
                                                                                                                   List.of(),
                                                                                                                   List.of(),
                                                                                                                   new Value.Literal(
                                                                                                                           "child,dog")))),
                                                                                                   new Value.Empty())),
                                                          Object.class, resolverContainer, EMPTY_PATH, EMPTY_PATH,
                                                          "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("vBox0");
        StatementExpression.MethodCall getStyleClass = CodeValues.methodCall(variable, "getStyleClass");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(3, initializers.size());
        Statement add1 = initializers.get(1);
        assertEquals(CodeValues.methodCall(getStyleClass, "add", "child"), add1);
        Statement add2 = initializers.getLast();
        assertEquals(CodeValues.methodCall(getStyleClass, "add", "dog"), add2);
    }

    @Test
    void testPropertyCollectionPropertyAttribute() {
        Class<?> objectClass = VBox.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(
                                                                                      List.of(new InstancePropertyAttribute(
                                                                                              "styleClass",
                                                                                              new Value.Literal(
                                                                                                      "child,dog"))),
                                                                                      List.of(), new Value.Empty())),
                                                          Object.class, resolverContainer, EMPTY_PATH, EMPTY_PATH,
                                                          "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("vBox0");
        StatementExpression.MethodCall getStyleClass = CodeValues.methodCall(variable, "getStyleClass");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(3, initializers.size());
        Statement add1 = initializers.get(1);
        assertEquals(CodeValues.methodCall(getStyleClass, "add", "child"), add1);
        Statement add2 = initializers.getLast();
        assertEquals(CodeValues.methodCall(getStyleClass, "add", "dog"), add2);
    }

    @Test
    void testMapInitialization() {
        Class<?> objectClass = HashMap.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(
                                                                                      List.of(new InstancePropertyAttribute(
                                                                                              "val0",
                                                                                              new Value.Literal("0"))),
                                                                                      List.of(new InstancePropertyElement(
                                                                                                      "val1",
                                                                                                      new ElementContent<>(
                                                                                                              List.of(),
                                                                                                              List.of(),
                                                                                                              new Value.Literal(
                                                                                                                      "1"))),
                                                                                              new InstancePropertyElement(
                                                                                                      "val2",
                                                                                                      new ElementContent<>(
                                                                                                              List.of(),
                                                                                                              List.of(new ValueElement(
                                                                                                                      String.class.getCanonicalName(),
                                                                                                                      "2",
                                                                                                                      EMPTY_CONTENT)),
                                                                                                              new Value.Empty()))),
                                                                                      new Value.Empty())), Object.class,
                                                          resolverContainer, EMPTY_PATH, EMPTY_PATH, "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("hashMap0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(6, initializers.size());
        Statement put0 = initializers.get(1);
        assertEquals(CodeValues.methodCall(variable, "put", "val0", "0"), put0);
        Statement put1 = initializers.get(2);
        assertEquals(CodeValues.methodCall(variable, "put", "val1", "1"), put1);
        Statement lineBreak = initializers.get(3);
        assertInstanceOf(Statement.LineBreak.class, lineBreak);
        Statement stringNode = initializers.get(4);
        Expression.Variable stringVariable = CodeValues.variable("string0");
        assertEquals(CodeValues.declaration(String.class, stringVariable, "2"), stringNode);
        Statement put2 = initializers.getLast();
        assertEquals(CodeValues.methodCall(variable, "put", "val2", stringVariable), put2);
    }

    @Test
    void testPropertyMapProperties() {
        Class<?> objectClass = Pane.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(List.of(),
                                                                                                   List.of(new InstancePropertyElement(
                                                                                                           "properties",
                                                                                                           new ElementContent<>(
                                                                                                                   List.of(new InstancePropertyAttribute(
                                                                                                                           "val0",
                                                                                                                           new Value.Literal(
                                                                                                                                   "0"))),
                                                                                                                   List.of(new InstancePropertyElement(
                                                                                                                                   "val1",
                                                                                                                                   new ElementContent<>(
                                                                                                                                           List.of(),
                                                                                                                                           List.of(),
                                                                                                                                           new Value.Literal(
                                                                                                                                                   "1"))),
                                                                                                                           new InstancePropertyElement(
                                                                                                                                   "val2",
                                                                                                                                   new ElementContent<>(
                                                                                                                                           List.of(),
                                                                                                                                           List.of(new ValueElement(
                                                                                                                                                   String.class.getCanonicalName(),
                                                                                                                                                   "2",
                                                                                                                                                   EMPTY_CONTENT)),
                                                                                                                                           new Value.Empty()))),
                                                                                                                   new Value.Empty()))),
                                                                                                   new Value.Empty())),
                                                          Object.class, resolverContainer, EMPTY_PATH, EMPTY_PATH,
                                                          "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("pane0");
        StatementExpression.MethodCall getProperties = CodeValues.methodCall(variable, "getProperties");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(6, initializers.size());
        Statement put0 = initializers.get(1);
        assertEquals(CodeValues.methodCall(getProperties, "put", "val0", "0"), put0);
        Statement put1 = initializers.get(2);
        assertEquals(CodeValues.methodCall(getProperties, "put", "val1", "1"), put1);
        Statement lineBreak = initializers.get(3);
        assertInstanceOf(Statement.LineBreak.class, lineBreak);
        Statement stringNode = initializers.get(4);
        Expression.Variable stringVariable = CodeValues.variable("string0");
        assertEquals(CodeValues.declaration(String.class, stringVariable, "2"), stringNode);
        Statement put2 = initializers.getLast();
        assertEquals(CodeValues.methodCall(getProperties, "put", "val2", stringVariable), put2);
    }

    @Test
    void testObjectInstancePropertyAttribute() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(
                                                                                      List.of(new InstancePropertyAttribute(
                                                                                              "text", new Value.Literal(
                                                                                              "test"))), List.of(),
                                                                                      new Value.Empty())), Object.class,
                                                          resolverContainer, EMPTY_PATH, EMPTY_PATH, "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(2, initializers.size());
        Statement statement = initializers.getLast();
        assertEquals(CodeValues.methodCall(variable, "setText", "test"), statement);
    }

    @Test
    void testObjectInstancePropertyElementValue() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(List.of(),
                                                                                                   List.of(new InstancePropertyElement(
                                                                                                           "text",
                                                                                                           new ElementContent<>(
                                                                                                                   List.of(),
                                                                                                                   List.of(),
                                                                                                                   new Value.Literal(
                                                                                                                           "test")))),
                                                                                                   new Value.Empty())),
                                                          Object.class, resolverContainer, EMPTY_PATH, EMPTY_PATH,
                                                          "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(2, initializers.size());
        Statement statement = initializers.getLast();
        assertEquals(CodeValues.methodCall(variable, "setText", "test"), statement);
    }

    @Test
    void testObjectInstancePropertyElementElement() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(List.of(),
                                                                                                   List.of(new InstancePropertyElement(
                                                                                                           "text",
                                                                                                           new ElementContent<>(
                                                                                                                   List.of(),
                                                                                                                   List.of(new ValueElement(
                                                                                                                           String.class.getCanonicalName(),
                                                                                                                           "test",
                                                                                                                           EMPTY_CONTENT)),
                                                                                                                   new Value.Empty()))),
                                                                                                   new Value.Empty())),
                                                          Object.class, resolverContainer, EMPTY_PATH, EMPTY_PATH,
                                                          "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(4, initializers.size());
        Statement lineBreak = initializers.get(1);
        assertInstanceOf(Statement.LineBreak.class, lineBreak);
        Statement stringNode = initializers.get(2);
        Expression.Variable stringVariable = CodeValues.variable("string0");
        assertEquals(CodeValues.declaration(String.class, stringVariable, "test"), stringNode);
        Statement statement = initializers.getLast();
        assertEquals(CodeValues.methodCall(variable, "setText", stringVariable), statement);
    }

    @Test
    void testObjectInstancePropertyBindExpression() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(
                                                                                      List.of(new InstancePropertyAttribute(
                                                                                                      "prefWidth",
                                                                                                      new BindExpression.PropertyRead(
                                                                                                              new BindExpression.Variable(
                                                                                                                      "button"),
                                                                                                              "minWidth")),
                                                                                              new IdAttribute(
                                                                                                      "button")),
                                                                                      List.of(), new Value.Empty())),
                                                          Object.class, resolverContainer, EMPTY_PATH, EMPTY_PATH,
                                                          "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(4, initializers.size());
        Statement expressionDeclaration = initializers.get(2);
        Expression.Variable propertyVariable = CodeValues.variable("doubleProperty0");
        assertEquals(CodeValues.declaration(DoubleProperty.class, propertyVariable,
                                            CodeValues.methodCall(variable, "minWidthProperty")),
                     expressionDeclaration);
        Statement statement = initializers.get(3);
        assertEquals(
                CodeValues.methodCall(CodeValues.methodCall(variable, "prefWidthProperty"), "bind", propertyVariable),
                statement);
    }

    @Test
    void testObjectStaticPropertyAttribute() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(
                                                                                      List.of(new StaticPropertyAttribute(
                                                                                              "javafx.scene.layout.GridPane",
                                                                                              "valignment",
                                                                                              new Value.Literal(
                                                                                                      "CENTER"))),
                                                                                      List.of(), new Value.Empty())),
                                                          Object.class, resolverContainer, EMPTY_PATH, EMPTY_PATH,
                                                          "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(2, initializers.size());
        Statement statement = initializers.getLast();
        assertEquals(CodeValues.methodCall(GridPane.class, "setValignment", variable, VPos.CENTER), statement);
    }

    @Test
    void testObjectStaticPropertyElementValue() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(List.of(),
                                                                                                   List.of(new StaticPropertyElement(
                                                                                                           "javafx.scene.layout.GridPane",
                                                                                                           "valignment",
                                                                                                           new ElementContent<>(
                                                                                                                   List.of(),
                                                                                                                   List.of(),
                                                                                                                   new Value.Literal(
                                                                                                                           "CENTER")))),
                                                                                                   new Value.Empty())),
                                                          Object.class, resolverContainer, EMPTY_PATH, EMPTY_PATH,
                                                          "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(2, initializers.size());
        Statement statement = initializers.getLast();
        assertEquals(CodeValues.methodCall(GridPane.class, "setValignment", variable, VPos.CENTER), statement);
    }

    @Test
    void testObjectStaticPropertyElementElement() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(List.of(),
                                                                                                   List.of(new StaticPropertyElement(
                                                                                                           "javafx.scene.layout.GridPane",
                                                                                                           "margin",
                                                                                                           new ElementContent<>(
                                                                                                                   List.of(),
                                                                                                                   List.of(new InstanceElement(
                                                                                                                           Insets.class.getCanonicalName(),
                                                                                                                           new ElementContent<>(
                                                                                                                                   List.of(new InstancePropertyAttribute(
                                                                                                                                           "topRightBottomLeft",
                                                                                                                                           new Value.Literal(
                                                                                                                                                   "10"))),
                                                                                                                                   List.of(),
                                                                                                                                   new Value.Empty()))),
                                                                                                                   new Value.Empty()))),
                                                                                                   new Value.Empty())),
                                                          Object.class, resolverContainer, EMPTY_PATH, EMPTY_PATH,
                                                          "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(4, initializers.size());
        Statement lineBreak = initializers.get(1);
        assertInstanceOf(Statement.LineBreak.class, lineBreak);
        Statement insetsNode = initializers.get(2);
        Expression.Variable insetsVariable = CodeValues.variable("insets0");
        assertEquals(CodeValues.declaration(Insets.class, insetsVariable, CodeValues.newInstance(Insets.class, 10d)),
                     insetsNode);
        Statement statement = initializers.getLast();
        assertEquals(CodeValues.methodCall(GridPane.class, "setMargin", variable, insetsVariable), statement);
    }

    @Test
    void testObjectEventHandlerAttribute() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(
                                                                                      List.of(new EventHandlerAttribute(
                                                                                              "onAction",
                                                                                              new Handler.Method(
                                                                                                      "onActionWithEvent"))),
                                                                                      List.of(), new Value.Empty())),
                                                          EventHandlerMethodController.class, resolverContainer,
                                                          EMPTY_PATH, EMPTY_PATH, "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(2, initializers.size());
        Statement statement = initializers.getLast();
        assertEquals(CodeValues.methodCall(variable, "setOnAction",
                                           CodeValues.methodReference(CodeValues.variable("controller"),
                                                                      "onActionWithEvent")), statement);
    }

    @Test
    void testObjectEventHandlerAttributeThrows() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(
                                                                                      List.of(new EventHandlerAttribute(
                                                                                              "onAction",
                                                                                              new Handler.Method(
                                                                                                      "onActionWithEventThrow"))),
                                                                                      List.of(), new Value.Empty())),
                                                          EventHandlerMethodController.class, resolverContainer,
                                                          EMPTY_PATH, EMPTY_PATH, "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(2, initializers.size());
        Statement statement = initializers.getLast();
        Expression.Lambda.Arrow lambda = CodeValues.lambdaBuilder()
                                                   .untyped(config -> config.parameter("event"))
                                                   .body(body -> body.statement(CodeValues.rethrow(
                                                           CodeValues.methodCall(CodeValues.variable("controller"),
                                                                                 "onActionWithEventThrow",
                                                                                 CodeValues.variable("event")))))
                                                   .build();
        assertEquals(CodeValues.methodCall(variable, "setOnAction", lambda), statement);
    }

    @Test
    void testObjectEventHandlerAttributeWithoutEvent() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(
                                                                                      List.of(new EventHandlerAttribute(
                                                                                              "onAction",
                                                                                              new Handler.Method(
                                                                                                      "onActionWithoutEvent"))),
                                                                                      List.of(), new Value.Empty())),
                                                          EventHandlerMethodController.class, resolverContainer,
                                                          EMPTY_PATH, EMPTY_PATH, "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(2, initializers.size());
        Statement statement = initializers.getLast();
        Expression.Lambda.Arrow lambda = CodeValues.lambdaBuilder()
                                                   .untyped(config -> config.parameter("event"))
                                                   .body(body -> body.statement(
                                                           CodeValues.methodCall(CodeValues.variable("controller"),
                                                                                 "onActionWithoutEvent")))
                                                   .build();
        assertEquals(CodeValues.methodCall(variable, "setOnAction", lambda), statement);
    }

    @Test
    void testObjectEventHandlerAttributeWithoutEventThrows() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(
                                                                                      List.of(new EventHandlerAttribute(
                                                                                              "onAction",
                                                                                              new Handler.Method(
                                                                                                      "onActionWithoutEventThrow"))),
                                                                                      List.of(), new Value.Empty())),
                                                          EventHandlerMethodController.class, resolverContainer,
                                                          EMPTY_PATH, EMPTY_PATH, "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(2, initializers.size());
        Statement statement = initializers.getLast();
        Expression.Lambda.Arrow lambda = CodeValues.lambdaBuilder()
                                                   .untyped(config -> config.parameter("event"))
                                                   .body(body -> body.statement(CodeValues.rethrow(
                                                           CodeValues.methodCall(CodeValues.variable("controller"),
                                                                                 "onActionWithoutEventThrow"))))
                                                   .build();
        assertEquals(CodeValues.methodCall(variable, "setOnAction", lambda), statement);
    }

    @Test
    void testObjectPropertyHandlerChangeMethodAttribute() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(
                                                                                      List.of(new EventHandlerAttribute(
                                                                                              "onTextChange",
                                                                                              new Handler.Method(
                                                                                                      "onTextChange"))),
                                                                                      List.of(), new Value.Empty())),
                                                          ChangeHandlerController.class, resolverContainer, EMPTY_PATH,
                                                          EMPTY_PATH, "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(2, initializers.size());
        Statement statement = initializers.getLast();
        StatementExpression.MethodCall textProperty = CodeValues.methodCall(variable, "textProperty");
        assertEquals(CodeValues.methodCall(textProperty, "addListener",
                                           CodeValues.methodReference(CodeValues.variable("controller"),
                                                                      "onTextChange")), statement);
    }

    @Test
    void testObjectPropertyHandlerChangeMethodPropertyAttribute() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(List.of(),
                                                                                                   List.of(new InstancePropertyElement(
                                                                                                           "text",
                                                                                                           new ElementContent<>(
                                                                                                                   List.of(new EventHandlerAttribute(
                                                                                                                           "onChange",
                                                                                                                           new Handler.Method(
                                                                                                                                   "onTextChange"))),
                                                                                                                   List.of(),
                                                                                                                   new Value.Empty()))),
                                                                                                   new Value.Empty())),
                                                          ChangeHandlerController.class, resolverContainer, EMPTY_PATH,
                                                          EMPTY_PATH, "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(2, initializers.size());
        Statement statement = initializers.getLast();
        StatementExpression.MethodCall textProperty = CodeValues.methodCall(variable, "textProperty");
        assertEquals(CodeValues.methodCall(textProperty, "addListener",
                                           CodeValues.methodReference(CodeValues.variable("controller"),
                                                                      "onTextChange")), statement);
    }

    @Test
    void testObjectPropertyHandlerListChangeMethodAttribute() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(
                                                                                      List.of(new EventHandlerAttribute(
                                                                                              "onStyleClassChange",
                                                                                              new Handler.Method(
                                                                                                      "onStyleClassChange"))),
                                                                                      List.of(), new Value.Empty())),
                                                          ChangeHandlerController.class, resolverContainer, EMPTY_PATH,
                                                          EMPTY_PATH, "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(2, initializers.size());
        Statement statement = initializers.getLast();
        StatementExpression.MethodCall textProperty = CodeValues.methodCall(variable, "getStyleClass");
        assertEquals(CodeValues.methodCall(textProperty, "addListener",
                                           CodeValues.methodReference(CodeValues.variable("controller"),
                                                                      "onStyleClassChange")), statement);
    }

    @Test
    void testObjectPropertyHandlerSetChangeMethodAttribute() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(
                                                                                      List.of(new EventHandlerAttribute(
                                                                                              "onPseudoClassStatesChange",
                                                                                              new Handler.Method(
                                                                                                      "onPseudoClassStatesChange"))),
                                                                                      List.of(), new Value.Empty())),
                                                          ChangeHandlerController.class, resolverContainer, EMPTY_PATH,
                                                          EMPTY_PATH, "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(2, initializers.size());
        Statement statement = initializers.getLast();
        StatementExpression.MethodCall textProperty = CodeValues.methodCall(variable, "getPseudoClassStates");
        assertEquals(CodeValues.methodCall(textProperty, "addListener",
                                           CodeValues.methodReference(CodeValues.variable("controller"),
                                                                      "onPseudoClassStatesChange")), statement);
    }

    @Test
    void testObjectPropertyHandlerMapChangeMethodAttribute() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(
                                                                                      List.of(new EventHandlerAttribute(
                                                                                              "onPropertiesChange",
                                                                                              new Handler.Method(
                                                                                                      "onPropertiesChange"))),
                                                                                      List.of(), new Value.Empty())),
                                                          ChangeHandlerController.class, resolverContainer, EMPTY_PATH,
                                                          EMPTY_PATH, "").getNodeCode();

        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(2, initializers.size());
        Statement statement = initializers.getLast();
        StatementExpression.MethodCall textProperty = CodeValues.methodCall(variable, "getProperties");
        assertEquals(CodeValues.methodCall(textProperty, "addListener",
                                           CodeValues.methodReference(CodeValues.variable("controller"),
                                                                      "onPropertiesChange")), statement);
    }

    @Test
    void testObjectPropertyHandlerListChangeMethodPropertyAttribute() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(List.of(),
                                                                                                   List.of(new InstancePropertyElement(
                                                                                                           "styleClass",
                                                                                                           new ElementContent<>(
                                                                                                                   List.of(new EventHandlerAttribute(
                                                                                                                           "onChange",
                                                                                                                           new Handler.Method(
                                                                                                                                   "onStyleClassChange"))),
                                                                                                                   List.of(),
                                                                                                                   new Value.Empty()))),
                                                                                                   new Value.Empty())),
                                                          ChangeHandlerController.class, resolverContainer, EMPTY_PATH,
                                                          EMPTY_PATH, "").getNodeCode();
        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(2, initializers.size());
        Statement statement = initializers.getLast();
        StatementExpression.MethodCall textProperty = CodeValues.methodCall(variable, "getStyleClass");
        assertEquals(CodeValues.methodCall(textProperty, "addListener",
                                           CodeValues.methodReference(CodeValues.variable("controller"),
                                                                      "onStyleClassChange")), statement);
    }

    @Test
    void testObjectPropertyHandlerSetChangeMethodPropertyAttribute() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(List.of(),
                                                                                                   List.of(new InstancePropertyElement(
                                                                                                           "pseudoClassStates",
                                                                                                           new ElementContent<>(
                                                                                                                   List.of(new EventHandlerAttribute(
                                                                                                                           "onChange",
                                                                                                                           new Handler.Method(
                                                                                                                                   "onPseudoClassStatesChange"))),
                                                                                                                   List.of(),
                                                                                                                   new Value.Empty()))),
                                                                                                   new Value.Empty())),
                                                          ChangeHandlerController.class, resolverContainer, EMPTY_PATH,
                                                          EMPTY_PATH, "").getNodeCode();
        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(2, initializers.size());
        Statement statement = initializers.getLast();
        StatementExpression.MethodCall textProperty = CodeValues.methodCall(variable, "getPseudoClassStates");
        assertEquals(CodeValues.methodCall(textProperty, "addListener",
                                           CodeValues.methodReference(CodeValues.variable("controller"),
                                                                      "onPseudoClassStatesChange")), statement);
    }

    @Test
    void testObjectPropertyHandlerMapChangeMethodPropertyAttribute() {
        Class<?> objectClass = Button.class;
        ObjectNodeCode nodeCode = new ObjectNodeProcessor(new InstanceElement(objectClass.getCanonicalName(),
                                                                              new ElementContent<>(List.of(),
                                                                                                   List.of(new InstancePropertyElement(
                                                                                                           "properties",
                                                                                                           new ElementContent<>(
                                                                                                                   List.of(new EventHandlerAttribute(
                                                                                                                           "onChange",
                                                                                                                           new Handler.Method(
                                                                                                                                   "onPropertiesChange"))),
                                                                                                                   List.of(),
                                                                                                                   new Value.Empty()))),
                                                                                                   new Value.Empty())),
                                                          ChangeHandlerController.class, resolverContainer, EMPTY_PATH,
                                                          EMPTY_PATH, "").getNodeCode();
        Expression.Variable variable = CodeValues.variable("button0");
        assertEquals(objectClass, nodeCode.nodeClass());
        assertEquals(variable, nodeCode.nodeValue());
        List<Statement> initializers = nodeCode.initializers();
        assertEquals(2, initializers.size());
        Statement statement = initializers.getLast();
        StatementExpression.MethodCall textProperty = CodeValues.methodCall(variable, "getProperties");
        assertEquals(CodeValues.methodCall(textProperty, "addListener",
                                           CodeValues.methodReference(CodeValues.variable("controller"),
                                                                      "onPropertiesChange")), statement);
    }
}
