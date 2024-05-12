package io.github.sheikah45.fx2j.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Execution(ExecutionMode.CONCURRENT)
public class FxmlParserElementTest {

    private static final Path FXML_ROOT = Path.of("src/test/resources/element/valid");

    @Test
    public void testProcessingInstructions() throws Exception {
        Path filePath = FXML_ROOT.resolve("processing-instructions.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        List<FxmlProcessingInstruction> processingInstructions = fxmlComponents.processingInstructions();
        assertEquals(List.of(new FxmlProcessingInstruction.Language("javascript"),
                             new FxmlProcessingInstruction.Compile(false), new FxmlProcessingInstruction.Compile(true),
                             new FxmlProcessingInstruction.Compile(true),
                             new FxmlProcessingInstruction.Import("javafx.scene.control.Button"),
                             new FxmlProcessingInstruction.Import("javafx.scene.layout.AnchorPane"),
                             new FxmlProcessingInstruction.Custom("test", "test"),
                             new FxmlProcessingInstruction.Custom("test", "notest")), processingInstructions);
    }

    @Test
    public void testInclude() throws Exception {
        Path filePath = FXML_ROOT.resolve("include.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlNode> children = rootNode.children();
        assertEquals(2, children.size());

        FxmlNode first = children.getFirst();
        assertEquals(new FxmlElement.Include(Path.of("included1.fxml"), null, StandardCharsets.UTF_8), first.element());

        FxmlNode last = children.getLast();
        assertEquals(
                new FxmlElement.Include(Path.of("included2.fxml"), Path.of("resource"), StandardCharsets.US_ASCII),
                last.element());
    }

    @Test
    public void testReference() throws Exception {
        Path filePath = FXML_ROOT.resolve("reference.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlNode> children = rootNode.children();
        assertEquals(2, children.size());

        FxmlNode first = children.getFirst();
        assertEquals(new FxmlElement.Reference("reference"), first.element());

        FxmlNode last = children.getLast();
        assertEquals(new FxmlElement.Reference("reference1"), last.element());
    }

    @Test
    public void testCopy() throws Exception {
        Path filePath = FXML_ROOT.resolve("copy.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlNode> children = rootNode.children();
        assertEquals(2, children.size());

        FxmlNode first = children.getFirst();
        assertEquals(new FxmlElement.Copy("reference"), first.element());

        FxmlNode last = children.getLast();
        assertEquals(new FxmlElement.Copy("copy1"), last.element());
    }

    @Test
    public void testDefine() throws Exception {
        Path filePath = FXML_ROOT.resolve("define.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlNode> children = rootNode.children();
        assertEquals(1, children.size());

        FxmlNode defined = children.getFirst();
        assertEquals(new FxmlElement.Define(), defined.element());

        assertEquals(1, defined.children().size());
        FxmlNode child = defined.children().getFirst();
        assertEquals(new FxmlElement.Declaration.Class("VBox"), child.element());
    }

    @Test
    public void testScript() throws Exception {
        Path filePath = FXML_ROOT.resolve("script.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        assertEquals(new FxmlProcessingInstruction.Language("javascript"),
                     fxmlComponents.processingInstructions().getFirst());

        FxmlNode rootNode = fxmlComponents.rootNode();

        List<FxmlNode> children = rootNode.children();
        assertEquals(1, children.size());

        FxmlNode script = children.getFirst();
        assertEquals(new FxmlElement.Script(), script.element());

        assertEquals("function handleButtonAction(event) { java.lang.System.out.println('You clicked me!'); }",
                     script.innerText());
    }

    @Test
    public void testInstanceProperty() throws Exception {
        Path filePath = FXML_ROOT.resolve("instance.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();
        assertEquals(new FxmlElement.Declaration.Class("VBox"), rootNode.element());
        assertEquals("", rootNode.innerText());
        assertEquals(List.of(), rootNode.attributes());

        List<FxmlNode> children = rootNode.children();
        assertEquals(1, children.size());

        FxmlNode property = children.getFirst();
        assertEquals(new FxmlElement.Property.Instance("alignment"), property.element());
        assertEquals("TOP_RIGHT", property.innerText());
    }

    @Test
    public void testStaticProperty() throws Exception {
        Path filePath = FXML_ROOT.resolve("static.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();
        assertEquals(new FxmlElement.Declaration.Class("VBox"), rootNode.element());
        assertEquals("", rootNode.innerText());
        assertEquals(List.of(), rootNode.attributes());

        List<FxmlNode> children = rootNode.children();
        assertEquals(1, children.size());

        FxmlNode property = children.getFirst();
        assertEquals(new FxmlElement.Property.Static("GridPane", "alignment"), property.element());
        assertEquals("TOP_RIGHT", property.innerText());
    }

    @Test
    public void testQualifiedStaticProperty() throws Exception {
        Path filePath = FXML_ROOT.resolve("qualified-static.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();
        assertEquals(new FxmlElement.Declaration.Class("VBox"), rootNode.element());
        assertEquals("", rootNode.innerText());
        assertEquals(List.of(), rootNode.attributes());

        List<FxmlNode> children = rootNode.children();
        assertEquals(1, children.size());

        FxmlNode property = children.getFirst();
        assertEquals(new FxmlElement.Property.Static("javafx.scene.layout.GridPane", "alignment"), property.element());
        assertEquals("TOP_RIGHT", property.innerText());
    }

    @Test
    public void testRoot() throws Exception {
        Path filePath = FXML_ROOT.resolve("root.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();
        assertEquals(new FxmlElement.Declaration.Root("javafx.scene.layout.VBox"), rootNode.element());
    }

    @Test
    public void testValue() throws Exception {
        Path filePath = FXML_ROOT.resolve("value.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();
        assertEquals(new FxmlElement.Declaration.Value("Double", "1"), rootNode.element());
    }

    @Test
    public void testConstant() throws Exception {
        Path filePath = FXML_ROOT.resolve("constant.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();
        assertEquals(new FxmlElement.Declaration.Constant("Double", "NEGATIVE_INFINITY"), rootNode.element());
    }

    @Test
    public void testFactory() throws Exception {
        Path filePath = FXML_ROOT.resolve("factory.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();
        assertEquals(new FxmlElement.Declaration.Factory("List", "of"), rootNode.element());
    }
}