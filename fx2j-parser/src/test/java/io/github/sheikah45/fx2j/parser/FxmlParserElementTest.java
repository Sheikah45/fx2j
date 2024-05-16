package io.github.sheikah45.fx2j.parser;

import io.github.sheikah45.fx2j.parser.attribute.InstancePropertyAttribute;
import io.github.sheikah45.fx2j.parser.attribute.NameSpaceAttribute;
import io.github.sheikah45.fx2j.parser.element.ClassInstanceElement;
import io.github.sheikah45.fx2j.parser.element.ConstantElement;
import io.github.sheikah45.fx2j.parser.element.CopyElement;
import io.github.sheikah45.fx2j.parser.element.DeclarationElement;
import io.github.sheikah45.fx2j.parser.element.DefineElement;
import io.github.sheikah45.fx2j.parser.element.FactoryElement;
import io.github.sheikah45.fx2j.parser.element.FxmlElement;
import io.github.sheikah45.fx2j.parser.element.IncludeElement;
import io.github.sheikah45.fx2j.parser.element.InstanceElement;
import io.github.sheikah45.fx2j.parser.element.InstancePropertyElement;
import io.github.sheikah45.fx2j.parser.element.ReferenceElement;
import io.github.sheikah45.fx2j.parser.element.RootElement;
import io.github.sheikah45.fx2j.parser.element.ScriptElement;
import io.github.sheikah45.fx2j.parser.element.ScriptSource;
import io.github.sheikah45.fx2j.parser.element.StaticPropertyElement;
import io.github.sheikah45.fx2j.parser.element.ValueElement;
import io.github.sheikah45.fx2j.parser.property.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Execution(ExecutionMode.CONCURRENT)
public class FxmlParserElementTest {

    private static final NameSpaceAttribute NAME_SPACE_ATTRIBUTE = new NameSpaceAttribute("fx", URI.create(
            "http://javafx.com/fxml"));
    private static final ClassInstanceElement.Content NAME_SPACE_ONLY_CONTENT = new ClassInstanceElement.Content(
            List.of(NAME_SPACE_ATTRIBUTE),
            List.of(),
            new Value.Empty());
    private static final ClassInstanceElement.Content EMPTY_CONTENT = new ClassInstanceElement.Content(List.of(),
                                                                                                      List.of(),
                                                                                                      new Value.Empty());
    private static final Path FXML_ROOT = Path.of("src/test/resources/element/valid");

    @Test
    public void testProcessingInstructions() {
        Path filePath = FXML_ROOT.resolve("processing-instructions.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        List<FxmlProcessingInstruction> processingInstructions = fxmlComponents.rootProcessingInstructions();
        assertEquals(List.of(new FxmlProcessingInstruction.Language("javascript"),
                             new FxmlProcessingInstruction.Compile(false), new FxmlProcessingInstruction.Compile(true),
                             new FxmlProcessingInstruction.Compile(true),
                             new FxmlProcessingInstruction.Import("javafx.scene.control.Button"),
                             new FxmlProcessingInstruction.Import("javafx.scene.layout.AnchorPane"),
                             new FxmlProcessingInstruction.Custom("test", "test"),
                             new FxmlProcessingInstruction.Custom("test", "")), processingInstructions);
    }

    @Test
    public void testInclude() {
        Path filePath = FXML_ROOT.resolve("include.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlElement> children = rootNode.content().children();
        assertEquals(2, children.size());

        FxmlElement first = children.getFirst();
        assertEquals(new IncludeElement(Path.of("included1.fxml"), null, StandardCharsets.UTF_8,
                                        EMPTY_CONTENT),
                     first);

        FxmlElement last = children.getLast();
        assertEquals(new IncludeElement(Path.of("included2.fxml"), Path.of("resource"), StandardCharsets.US_ASCII,
                                        EMPTY_CONTENT), last);
    }

    @Test
    public void testReference() {
        Path filePath = FXML_ROOT.resolve("reference.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlElement> children = rootNode.content().children();
        assertEquals(2, children.size());

        FxmlElement first = children.getFirst();
        assertEquals(new ReferenceElement("reference", EMPTY_CONTENT), first);

        FxmlElement last = children.getLast();
        assertEquals(new ReferenceElement("reference1", EMPTY_CONTENT), last);
    }

    @Test
    public void testCopy() {
        Path filePath = FXML_ROOT.resolve("copy.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlElement> children = rootNode.content().children();
        assertEquals(2, children.size());

        FxmlElement first = children.getFirst();
        assertEquals(new CopyElement("reference", EMPTY_CONTENT), first);

        FxmlElement last = children.getLast();
        assertEquals(new CopyElement("copy1", EMPTY_CONTENT), last);
    }

    @Test
    public void testDefine() {
        Path filePath = FXML_ROOT.resolve("define.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlElement> children = rootNode.content().children();
        assertEquals(1, children.size());

        FxmlElement defined = children.getFirst();
        assertEquals(new DefineElement(List.of(new InstanceElement("VBox", EMPTY_CONTENT))), defined);
    }

    @Test
    public void testScriptInline() {
        Path filePath = FXML_ROOT.resolve("script-inline.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        assertEquals(new FxmlProcessingInstruction.Language("javascript"),
                     fxmlComponents.rootProcessingInstructions().getFirst());

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlElement> children = rootNode.content().children();
        assertEquals(1, children.size());

        assertEquals(new ScriptElement(new ScriptSource.Inline(
                             "function handleButtonAction(event) { java.lang.System.out.println('You clicked me!'); }")),
                     children.getFirst());
    }

    @Test
    public void testScriptReference() {
        Path filePath = FXML_ROOT.resolve("script-reference.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        assertEquals(new FxmlProcessingInstruction.Language("javascript"),
                     fxmlComponents.rootProcessingInstructions().getFirst());

        DeclarationElement rootNode = fxmlComponents.rootNode();

        List<FxmlElement> children = rootNode.content().children();
        assertEquals(1, children.size());

        assertEquals(new ScriptElement(new ScriptSource.Reference(Path.of("test.js"), StandardCharsets.UTF_8)),
                     children.getFirst());
    }

    @Test
    public void testInstanceProperty() {
        Path filePath = FXML_ROOT.resolve("single-property.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();
        assertEquals(new InstanceElement("VBox", new ClassInstanceElement.Content(List.of(NAME_SPACE_ATTRIBUTE),
                                                                                  List.of(new InstancePropertyElement(
                                                                                          "alignment",
                                                                                          new Value.Literal(
                                                                                                  "TOP_RIGHT"))),
                                                                                  new Value.Empty())), rootNode);
    }

    @Test
    public void testPropertyText() {
        Path filePath = FXML_ROOT.resolve("text.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();
        assertEquals(
                new InstanceElement("Label", new ClassInstanceElement.Content(List.of(NAME_SPACE_ATTRIBUTE), List.of(),
                                                                                   new Value.Literal("test"))),
                     rootNode);
    }

    @Test
    public void testMultiPropertyText() {
        Path filePath = FXML_ROOT.resolve("multi-text.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();
        assertEquals(new InstanceElement("Label", new ClassInstanceElement.Content(List.of(NAME_SPACE_ATTRIBUTE),
                                                                                   List.of(new InstancePropertyElement(
                                                                                           "alignment",
                                                                                           new Value.Literal(
                                                                                                   "TOP_LEFT"))),
                                                                                   new Value.Literal("test2"))),
                     rootNode);
    }

    @Test
    public void testPropertyAttribute() {
        Path filePath = FXML_ROOT.resolve("property-attribute.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();
        assertEquals(new InstanceElement("VBox", new ClassInstanceElement.Content(List.of(NAME_SPACE_ATTRIBUTE),
                                                                                  List.of(new InstancePropertyElement(
                                                                                          "properties",
                                                                                          new Value.Attribute(
                                                                                                  new InstancePropertyAttribute(
                                                                                                          "foo",
                                                                                                          new Value.Literal(
                                                                                                                  "123"))))),
                                                                                  new Value.Empty())),
                     rootNode);
    }

    @Test
    public void testPropertyElement() {
        Path filePath = FXML_ROOT.resolve("property-element.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();
        assertEquals(new InstanceElement("VBox", new ClassInstanceElement.Content(List.of(NAME_SPACE_ATTRIBUTE),
                                                                                  List.of(new InstancePropertyElement(
                                                                                          "children",
                                                                                          new Value.Element(
                                                                                                  new InstanceElement(
                                                                                                          "VBox",
                                                                                                          EMPTY_CONTENT)))),
                                                                                  new Value.Empty())), rootNode);
    }

    @Test
    public void testMultiProperty() {
        Path filePath = FXML_ROOT.resolve("multi-property-value.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();
        assertEquals(new InstanceElement("VBox", new ClassInstanceElement.Content(List.of(NAME_SPACE_ATTRIBUTE),
                                                                                  List.of(new InstancePropertyElement(
                                                                                          "alignment",
                                                                                          new Value.Multi(
                                                                                                  List.of(new Value.Attribute(
                                                                                                                  new InstancePropertyAttribute(
                                                                                                                          "value",
                                                                                                                          new Value.Literal(
                                                                                                                                  "TOP_RIGHT"))),
                                                                                                          new Value.Element(
                                                                                                                  new InstanceElement(
                                                                                                                          "Label",
                                                                                                                          EMPTY_CONTENT)),
                                                                                                          new Value.Literal(
                                                                                                                  "text"))))),
                                                                                  new Value.Empty())), rootNode);
    }

    @Test
    public void testEmptyProperty() {
        Path filePath = FXML_ROOT.resolve("empty-property.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();
        assertEquals(new InstanceElement("VBox", new ClassInstanceElement.Content(List.of(NAME_SPACE_ATTRIBUTE),
                                                                                  List.of(new InstancePropertyElement(
                                                                                          "alignment",
                                                                                          new Value.Empty())),
                                                                                  new Value.Empty())),
                     rootNode);
    }

    @Test
    public void testStaticProperty() {
        Path filePath = FXML_ROOT.resolve("static.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();
        assertEquals(new InstanceElement("VBox", new ClassInstanceElement.Content(List.of(NAME_SPACE_ATTRIBUTE),
                                                                                  List.of(new StaticPropertyElement(
                                                                                          "GridPane", "alignment",
                                                                                          new Value.Literal(
                                                                                                  "TOP_RIGHT"))),
                                                                                  new Value.Empty())), rootNode);
    }

    @Test
    public void testQualifiedStaticProperty() {
        Path filePath = FXML_ROOT.resolve("qualified-static.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();
        assertEquals(new InstanceElement("VBox", new ClassInstanceElement.Content(List.of(NAME_SPACE_ATTRIBUTE),
                                                                                  List.of(new StaticPropertyElement(
                                                                                          "javafx.scene.layout.GridPane",
                                                                                          "alignment",
                                                                                          new Value.Literal(
                                                                                                  "TOP_RIGHT"))),
                                                                                  new Value.Empty())), rootNode);
    }

    @Test
    public void testRoot() {
        Path filePath = FXML_ROOT.resolve("root.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();
        assertEquals(new RootElement("javafx.scene.layout.VBox", NAME_SPACE_ONLY_CONTENT), rootNode);
    }

    @Test
    public void testValue() {
        Path filePath = FXML_ROOT.resolve("value.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();
        assertEquals(new ValueElement("Double", new Value.Literal("1"), NAME_SPACE_ONLY_CONTENT), rootNode);
    }

    @Test
    public void testConstant() {
        Path filePath = FXML_ROOT.resolve("constant.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();
        assertEquals(new ConstantElement("Double", "NEGATIVE_INFINITY", NAME_SPACE_ONLY_CONTENT), rootNode);
    }

    @Test
    public void testFactory() {
        Path filePath = FXML_ROOT.resolve("factory.fxml");
        FxmlComponents fxmlComponents = FxmlParser.readFxml(filePath);

        DeclarationElement rootNode = fxmlComponents.rootNode();
        assertEquals(new FactoryElement("List", "of", NAME_SPACE_ONLY_CONTENT), rootNode);
    }
}