package io.github.sheikah45.fx2j.parser;

import io.github.sheikah45.fx2j.parser.attribute.ControllerAttribute;
import io.github.sheikah45.fx2j.parser.attribute.EventHandlerAttribute;
import io.github.sheikah45.fx2j.parser.attribute.FxmlAttribute;
import io.github.sheikah45.fx2j.parser.attribute.IdAttribute;
import io.github.sheikah45.fx2j.parser.attribute.InstancePropertyAttribute;
import io.github.sheikah45.fx2j.parser.attribute.StaticPropertyAttribute;
import io.github.sheikah45.fx2j.parser.element.DeclarationElement;
import io.github.sheikah45.fx2j.parser.property.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Execution(ExecutionMode.CONCURRENT)
public class FxmlParserPropertyTest {

    private static final Path FXML_ROOT = Path.of("src/test/resources/property");

    @Test
    public void testIdProperty() {
        Path filePath = FXML_ROOT.resolve("id.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new IdAttribute("box")), attributes);
    }

    @Test
    public void testControllerProperty() {
        Path filePath = FXML_ROOT.resolve("controller.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new ControllerAttribute("io.github.sheikah45.fx2j.parser.FxmlParser")), attributes);
    }

    @Test
    public void testInstanceProperty() {
        Path filePath = FXML_ROOT.resolve("instance.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("alignment", new Value.Literal("TOP_RIGHT"))), attributes);
    }

    @Test
    public void testStaticProperty() {
        Path filePath = FXML_ROOT.resolve("static.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new StaticPropertyAttribute("GridPane", "alignment", new Value.Literal("TOP_RIGHT"))),
                     attributes);
    }

    @Test
    public void testQualifiedStaticProperty() {
        Path filePath = FXML_ROOT.resolve("qualified-static.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new StaticPropertyAttribute("javafx.scene.layout.GridPane", "alignment",
                                                         new Value.Literal("TOP_RIGHT"))), attributes);
    }

    @Test
    public void testEventHandlerProperty() {
        Path filePath = FXML_ROOT.resolve("event-handler.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(
                List.of(new EventHandlerAttribute("onScroll", new Value.Handler.Script(
                        "java.lang.System.out.println('scrolled')"))),
                attributes);
    }

    @Test
    public void testEmptyProperty() {
        Path filePath = FXML_ROOT.resolve("empty.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("text", new Value.Empty())), attributes);
    }

    @Test
    public void testLocationProperty() {
        Path filePath = FXML_ROOT.resolve("location.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("url", new Value.Location(Path.of("test.png")))),
                     attributes);
    }

    @Test
    public void testResourceProperty() {
        Path filePath = FXML_ROOT.resolve("resource.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("text", new Value.Resource("test"))), attributes);
    }

    @Test
    public void testReferenceProperty() {
        Path filePath = FXML_ROOT.resolve("reference.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("text", new Value.Reference("test"))), attributes);
    }

    @Test
    public void testEscapeProperty() {
        Path filePath = FXML_ROOT.resolve("escape.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("text", new Value.Literal("$test"))), attributes);
    }

    @Test
    public void testExpressionProperty() {
        Path filePath = FXML_ROOT.resolve("expression.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new InstancePropertyAttribute("text", new Value.Expression("test.text"))), attributes);
    }

    @Test
    public void testReferenceHandlerProperty() {
        Path filePath = FXML_ROOT.resolve("event-handler-reference.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new EventHandlerAttribute("onScroll", new Value.Handler.Reference("scroller"))),
                     attributes);
    }

    @Test
    public void testMethodProperty() {
        Path filePath = FXML_ROOT.resolve("event-handler-method.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.content().attributes();
        assertEquals(List.of(new EventHandlerAttribute("onScroll", new Value.Handler.Method("scroll"))), attributes);
    }
}