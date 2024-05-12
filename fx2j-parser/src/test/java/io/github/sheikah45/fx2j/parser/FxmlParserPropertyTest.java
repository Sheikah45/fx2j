package io.github.sheikah45.fx2j.parser;

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
    public void testIdProperty() throws Exception {
        Path filePath = FXML_ROOT.resolve("id.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.attributes();
        assertEquals(1, attributes.size());

        FxmlAttribute property = attributes.getFirst();
        assertEquals(new FxmlAttribute.Property.Id("box"), property);
    }

    @Test
    public void testControllerProperty() throws Exception {
        Path filePath = FXML_ROOT.resolve("controller.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.attributes();
        assertEquals(1, attributes.size());

        FxmlAttribute property = attributes.getFirst();
        assertEquals(new FxmlAttribute.Property.Controller("io.github.sheikah45.fx2j.parser.FxmlParser"), property);
    }

    @Test
    public void testInstanceProperty() throws Exception {
        Path filePath = FXML_ROOT.resolve("instance.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.attributes();
        assertEquals(1, attributes.size());

        FxmlAttribute property = attributes.getFirst();
        assertEquals(new FxmlAttribute.Property.Instance("alignment", new FxmlAttribute.Property.Literal("TOP_RIGHT")),
                     property);
    }

    @Test
    public void testStaticProperty() throws Exception {
        Path filePath = FXML_ROOT.resolve("static.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.attributes();
        assertEquals(1, attributes.size());

        FxmlAttribute property = attributes.getFirst();
        assertEquals(new FxmlAttribute.Property.Static("GridPane", "alignment",
                                                       new FxmlAttribute.Property.Literal("TOP_RIGHT")), property);
    }

    @Test
    public void testQualifiedStaticProperty() throws Exception {
        Path filePath = FXML_ROOT.resolve("qualified-static.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.attributes();
        assertEquals(1, attributes.size());

        FxmlAttribute property = attributes.getFirst();
        assertEquals(new FxmlAttribute.Property.Static("javafx.scene.layout.GridPane", "alignment",
                                                       new FxmlAttribute.Property.Literal("TOP_RIGHT")), property);
    }

    @Test
    public void testEventHandlerProperty() throws Exception {
        Path filePath = FXML_ROOT.resolve("event-handler.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.attributes();
        assertEquals(1, attributes.size());

        FxmlAttribute property = attributes.getFirst();
        assertEquals(new FxmlAttribute.Property.EventHandler("onScroll", new FxmlAttribute.EventHandler.Script(
                "java.lang.System.out.println('scrolled')")), property);
    }

    @Test
    public void testEmptyProperty() throws Exception {
        Path filePath = FXML_ROOT.resolve("empty.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.attributes();
        assertEquals(1, attributes.size());

        FxmlAttribute property = attributes.getFirst();
        assertEquals(new FxmlAttribute.Property.Instance("text", new FxmlAttribute.Property.Empty()), property);
    }

    @Test
    public void testLocationProperty() throws Exception {
        Path filePath = FXML_ROOT.resolve("location.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.attributes();
        assertEquals(1, attributes.size());

        FxmlAttribute property = attributes.getFirst();
        assertEquals(
                new FxmlAttribute.Property.Instance("url", new FxmlAttribute.Property.Location(Path.of("test.png"))),
                property);
    }

    @Test
    public void testResourceProperty() throws Exception {
        Path filePath = FXML_ROOT.resolve("resource.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.attributes();
        assertEquals(1, attributes.size());

        FxmlAttribute property = attributes.getFirst();
        assertEquals(new FxmlAttribute.Property.Instance("text", new FxmlAttribute.Property.Resource("test")),
                     property);
    }

    @Test
    public void testReferenceProperty() throws Exception {
        Path filePath = FXML_ROOT.resolve("reference.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.attributes();
        assertEquals(1, attributes.size());

        FxmlAttribute property = attributes.getFirst();
        assertEquals(new FxmlAttribute.Property.Instance("text", new FxmlAttribute.Property.Reference("test")),
                     property);
    }

    @Test
    public void testEscapeProperty() throws Exception {
        Path filePath = FXML_ROOT.resolve("escape.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.attributes();
        assertEquals(1, attributes.size());

        FxmlAttribute property = attributes.getFirst();
        assertEquals(new FxmlAttribute.Property.Instance("text", new FxmlAttribute.Property.Literal("$test")),
                     property);
    }

    @Test
    public void testExpressionProperty() throws Exception {
        Path filePath = FXML_ROOT.resolve("expression.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.attributes();
        assertEquals(1, attributes.size());

        FxmlAttribute property = attributes.getFirst();
        assertEquals(new FxmlAttribute.Property.Instance("text", new FxmlAttribute.Property.Expression("test.text")),
                     property);
    }

    @Test
    public void testReferenceHandlerProperty() throws Exception {
        Path filePath = FXML_ROOT.resolve("event-handler-reference.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.attributes();
        assertEquals(1, attributes.size());

        FxmlAttribute property = attributes.getFirst();
        assertEquals(new FxmlAttribute.EventHandler("onScroll", new FxmlAttribute.EventHandler.Reference("scroller")),
                     property);
    }

    @Test
    public void testMethodProperty() throws Exception {
        Path filePath = FXML_ROOT.resolve("event-handler-method.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlAttribute> attributes = rootNode.attributes();
        assertEquals(1, attributes.size());

        FxmlAttribute property = attributes.getFirst();
        assertEquals(new FxmlAttribute.EventHandler("onScroll", new FxmlAttribute.EventHandler.Method("scroll")),
                     property);
    }
}