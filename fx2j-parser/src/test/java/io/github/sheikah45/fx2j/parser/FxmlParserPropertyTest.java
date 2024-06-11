package io.github.sheikah45.fx2j.parser;

import io.github.sheikah45.fx2j.parser.attribute.ControllerAttribute;
import io.github.sheikah45.fx2j.parser.attribute.DefaultNameSpaceAttribute;
import io.github.sheikah45.fx2j.parser.attribute.EventHandlerAttribute;
import io.github.sheikah45.fx2j.parser.attribute.FxmlAttribute;
import io.github.sheikah45.fx2j.parser.attribute.IdAttribute;
import io.github.sheikah45.fx2j.parser.attribute.InstancePropertyAttribute;
import io.github.sheikah45.fx2j.parser.attribute.NameSpaceAttribute;
import io.github.sheikah45.fx2j.parser.attribute.StaticPropertyAttribute;
import io.github.sheikah45.fx2j.parser.element.DeclarationElement;
import io.github.sheikah45.fx2j.parser.property.BindExpression;
import io.github.sheikah45.fx2j.parser.property.Handler;
import io.github.sheikah45.fx2j.parser.property.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Execution(ExecutionMode.CONCURRENT)
class FxmlParserPropertyTest {

    private static final Path FXML_ROOT = Path.of("src/test/resources/property");
    private static final NameSpaceAttribute NAME_SPACE_ATTRIBUTE = new NameSpaceAttribute("fx", URI.create(
            "http://javafx.com/fxml"));

    @Test
    void testDefaultNamespace() {
        Path filePath = FXML_ROOT.resolve("default-namespace.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<? extends FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new DefaultNameSpaceAttribute(URI.create(
                "http://javafx.com/fxml"))), attributes);
    }

    @Test
    void testIdProperty() {
        Path filePath = FXML_ROOT.resolve("id.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<? extends FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new IdAttribute("box"), NAME_SPACE_ATTRIBUTE), attributes);
    }

    @Test
    void testControllerProperty() {
        Path filePath = FXML_ROOT.resolve("controller.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<? extends FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(
                List.of(new ControllerAttribute("io.github.sheikah45.fx2j.parser.FxmlParser"), NAME_SPACE_ATTRIBUTE),
                attributes);
    }

    @Test
    void testInstanceProperty() {
        Path filePath = FXML_ROOT.resolve("instance.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<? extends FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("alignment", new Value.Literal("TOP_RIGHT")),
                             NAME_SPACE_ATTRIBUTE), attributes);
    }

    @Test
    void testStaticProperty() {
        Path filePath = FXML_ROOT.resolve("static.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<? extends FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new StaticPropertyAttribute("GridPane", "alignment", new Value.Literal("TOP_RIGHT")),
                             NAME_SPACE_ATTRIBUTE),
                     attributes);
    }

    @Test
    void testQualifiedStaticProperty() {
        Path filePath = FXML_ROOT.resolve("qualified-static.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<? extends FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new StaticPropertyAttribute("javafx.scene.layout.GridPane", "alignment",
                                                         new Value.Literal("TOP_RIGHT")), NAME_SPACE_ATTRIBUTE),
                     attributes);
    }

    @Test
    void testEventHandlerProperty() {
        Path filePath = FXML_ROOT.resolve("event-handler.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<? extends FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(
                List.of(new EventHandlerAttribute("onScroll", new Handler.Script(
                        "java.lang.System.out.println('scrolled')")), NAME_SPACE_ATTRIBUTE),
                attributes);
    }

    @Test
    void testEmptyProperty() {
        Path filePath = FXML_ROOT.resolve("empty.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<? extends FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("text", new Value.Literal("")), NAME_SPACE_ATTRIBUTE),
                     attributes);
    }

    @Test
    void testLocationProperty() {
        Path filePath = FXML_ROOT.resolve("location.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<? extends FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("url", new Value.Location(Path.of("test.png"))),
                             NAME_SPACE_ATTRIBUTE),
                     attributes);
    }

    @Test
    void testResourceProperty() {
        Path filePath = FXML_ROOT.resolve("resource.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<? extends FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(
                List.of(new InstancePropertyAttribute("text", new Value.Resource("test")), NAME_SPACE_ATTRIBUTE),
                     attributes);
    }

    @Test
    void testReferenceProperty() {
        Path filePath = FXML_ROOT.resolve("reference.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<? extends FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(
                List.of(new InstancePropertyAttribute("text", new Value.Reference("test")), NAME_SPACE_ATTRIBUTE),
                     attributes);
    }

    @Test
    void testEscapeProperty() {
        Path filePath = FXML_ROOT.resolve("escape.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<? extends FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(
                List.of(new InstancePropertyAttribute("text", new Value.Literal("$test")), NAME_SPACE_ATTRIBUTE),
                     attributes);
    }

    @Test
    void testExpressionProperty() {
        Path filePath = FXML_ROOT.resolve("expression.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<? extends FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("text", new BindExpression.PropertyRead(
                new BindExpression.Variable("test"), "text")), NAME_SPACE_ATTRIBUTE), attributes);
    }

    @Test
    void testComplexExpressionProperty() {
        Path filePath = FXML_ROOT.resolve("complex-expression.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<? extends FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("width", new BindExpression.Add(new BindExpression.Add(
                                     new BindExpression.PropertyRead(
                                             new BindExpression.PropertyRead(new BindExpression.Variable("test"), "text"), "length"),
                                     new BindExpression.Multiply(
                                             new BindExpression.PropertyRead(new BindExpression.Variable("test"), "width"),
                                             new BindExpression.PropertyRead(new BindExpression.Variable("root"), "margin"))),
                                                                                           new BindExpression.Whole(
                                                                                                     10))),
                             NAME_SPACE_ATTRIBUTE), attributes);
    }

    @Test
    void testReferenceHandlerProperty() {
        Path filePath = FXML_ROOT.resolve("event-handler-reference.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<? extends FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new EventHandlerAttribute("onScroll", new Handler.Reference("scroller")),
                             NAME_SPACE_ATTRIBUTE),
                     attributes);
    }

    @Test
    void testEmptyHandlerProperty() {
        Path filePath = FXML_ROOT.resolve("event-handler-empty.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<? extends FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new EventHandlerAttribute("onScroll", new Handler.Empty()), NAME_SPACE_ATTRIBUTE),
                     attributes);
    }

    @Test
    void testMethodProperty() {
        Path filePath = FXML_ROOT.resolve("event-handler-method.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<? extends FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new EventHandlerAttribute("onScroll", new Handler.Method("scroll")),
                             NAME_SPACE_ATTRIBUTE), attributes);
    }
}