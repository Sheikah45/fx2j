package io.github.sheikah45.fx2j.parser;

import io.github.sheikah45.fx2j.parser.attribute.ControllerAttribute;
import io.github.sheikah45.fx2j.parser.attribute.EventHandlerAttribute;
import io.github.sheikah45.fx2j.parser.attribute.FxmlAttribute;
import io.github.sheikah45.fx2j.parser.attribute.IdAttribute;
import io.github.sheikah45.fx2j.parser.attribute.InstancePropertyAttribute;
import io.github.sheikah45.fx2j.parser.attribute.NameSpaceAttribute;
import io.github.sheikah45.fx2j.parser.attribute.StaticPropertyAttribute;
import io.github.sheikah45.fx2j.parser.element.DeclarationElement;
import io.github.sheikah45.fx2j.parser.property.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Execution(ExecutionMode.CONCURRENT)
public class FxmlParserPropertyTest {

    private static final Path FXML_ROOT = Path.of("src/test/resources/property");
    private static final NameSpaceAttribute NAME_SPACE_ATTRIBUTE = new NameSpaceAttribute("fx", URI.create(
            "http://javafx.com/fxml"));

    @Test
    public void testIdProperty() {
        Path filePath = FXML_ROOT.resolve("id.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new IdAttribute("box"), NAME_SPACE_ATTRIBUTE), attributes);
    }

    @Test
    public void testControllerProperty() {
        Path filePath = FXML_ROOT.resolve("controller.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(
                List.of(new ControllerAttribute("io.github.sheikah45.fx2j.parser.FxmlParser"), NAME_SPACE_ATTRIBUTE),
                attributes);
    }

    @Test
    public void testInstanceProperty() {
        Path filePath = FXML_ROOT.resolve("instance.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("alignment", new Value.Literal("TOP_RIGHT")),
                             NAME_SPACE_ATTRIBUTE), attributes);
    }

    @Test
    public void testStaticProperty() {
        Path filePath = FXML_ROOT.resolve("static.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new StaticPropertyAttribute("GridPane", "alignment", new Value.Literal("TOP_RIGHT")),
                             NAME_SPACE_ATTRIBUTE),
                     attributes);
    }

    @Test
    public void testQualifiedStaticProperty() {
        Path filePath = FXML_ROOT.resolve("qualified-static.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new StaticPropertyAttribute("javafx.scene.layout.GridPane", "alignment",
                                                         new Value.Literal("TOP_RIGHT")), NAME_SPACE_ATTRIBUTE),
                     attributes);
    }

    @Test
    public void testEventHandlerProperty() {
        Path filePath = FXML_ROOT.resolve("event-handler.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(
                List.of(new EventHandlerAttribute("onScroll", new Value.Handler.Script(
                        "java.lang.System.out.println('scrolled')")), NAME_SPACE_ATTRIBUTE),
                attributes);
    }

    @Test
    public void testEmptyProperty() {
        Path filePath = FXML_ROOT.resolve("empty.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("text", new Value.Empty()), NAME_SPACE_ATTRIBUTE),
                     attributes);
    }

    @Test
    public void testLocationProperty() {
        Path filePath = FXML_ROOT.resolve("location.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("url", new Value.Location(Path.of("test.png"))),
                             NAME_SPACE_ATTRIBUTE),
                     attributes);
    }

    @Test
    public void testResourceProperty() {
        Path filePath = FXML_ROOT.resolve("resource.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("text", new Value.Resource("test")), NAME_SPACE_ATTRIBUTE),
                     attributes);
    }

    @Test
    public void testReferenceProperty() {
        Path filePath = FXML_ROOT.resolve("reference.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("text", new Value.Reference("test")), NAME_SPACE_ATTRIBUTE),
                     attributes);
    }

    @Test
    public void testEscapeProperty() {
        Path filePath = FXML_ROOT.resolve("escape.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("text", new Value.Literal("$test")), NAME_SPACE_ATTRIBUTE),
                     attributes);
    }

    @Test
    public void testExpressionProperty() {
        Path filePath = FXML_ROOT.resolve("expression.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("text", new Value.Expression.PropertyRead(
                new Value.Expression.Variable("test"), "text")), NAME_SPACE_ATTRIBUTE), attributes);
    }

    @Test
    public void testComplexExpressionProperty() {
        Path filePath = FXML_ROOT.resolve("complex-expression.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("width", new Value.Expression.Add(new Value.Expression.Add(
                                     new Value.Expression.PropertyRead(
                                             new Value.Expression.PropertyRead(new Value.Expression.Variable("test"), "text"), "length"),
                                     new Value.Expression.Multiply(
                                             new Value.Expression.PropertyRead(new Value.Expression.Variable("test"), "width"),
                                             new Value.Expression.PropertyRead(new Value.Expression.Variable("root"), "margin"))),
                                                                                             new Value.Expression.Whole(
                                                                                                     10))),
                             NAME_SPACE_ATTRIBUTE), attributes);
    }

    @Test
    public void testReferenceHandlerProperty() {
        Path filePath = FXML_ROOT.resolve("event-handler-reference.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new EventHandlerAttribute("onScroll", new Value.Handler.Reference("scroller")),
                             NAME_SPACE_ATTRIBUTE),
                     attributes);
    }

    @Test
    public void testEmptyHandlerProperty() {
        Path filePath = FXML_ROOT.resolve("event-handler-empty.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new EventHandlerAttribute("onScroll", new Value.Handler.Empty()), NAME_SPACE_ATTRIBUTE),
                     attributes);
    }

    @Test
    public void testMethodProperty() {
        Path filePath = FXML_ROOT.resolve("event-handler-method.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new EventHandlerAttribute("onScroll", new Value.Handler.Method("scroll")),
                             NAME_SPACE_ATTRIBUTE), attributes);
    }
}