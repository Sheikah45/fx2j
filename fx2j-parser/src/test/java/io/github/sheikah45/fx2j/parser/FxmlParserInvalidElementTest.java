package io.github.sheikah45.fx2j.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;


@Execution(ExecutionMode.CONCURRENT)
class FxmlParserInvalidElementTest {

    private static final Path FXML_ROOT = Path.of("src/test/resources/element/invalid");

    @Test
    void testEmptyImport() {
        Path filePath = FXML_ROOT.resolve("empty-import.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testEmptyLanguage() {
        Path filePath = FXML_ROOT.resolve("empty-language.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testNonDeclaration() {
        Path filePath = FXML_ROOT.resolve("non-declaration-root.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testMultiDeclaration() {
        Path filePath = FXML_ROOT.resolve("multi-declaration.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testNonAssignablePropertyAttribute() {
        Path filePath = FXML_ROOT.resolve("property-non-assignable-attribute.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testNonAssignablePropertyChild() {
        Path filePath = FXML_ROOT.resolve("property-non-assignable-child.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testInclude() {
        Path filePath = FXML_ROOT.resolve("include.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testReference() {
        Path filePath = FXML_ROOT.resolve("reference.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testCopy() {
        Path filePath = FXML_ROOT.resolve("copy.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testDefineValue() {
        Path filePath = FXML_ROOT.resolve("define-value.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testDefineProperty() {
        Path filePath = FXML_ROOT.resolve("define-property.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testDefineAttribute() {
        Path filePath = FXML_ROOT.resolve("define-attribute.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testScriptSourceChild() {
        Path filePath = FXML_ROOT.resolve("script-source-child.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testScriptSourceProperty() {
        Path filePath = FXML_ROOT.resolve("script-source-property.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testScriptSourceValue() {
        Path filePath = FXML_ROOT.resolve("script-source-value.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testScriptInlineChild() {
        Path filePath = FXML_ROOT.resolve("script-inline-child.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testScriptInlineProperty() {
        Path filePath = FXML_ROOT.resolve("script-inline-property.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testScriptInlineValue() {
        Path filePath = FXML_ROOT.resolve("script-inline-value.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testStaticPropertyAttribute() {
        Path filePath = FXML_ROOT.resolve("static-property-attribute.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    void testStaticPropertyChild() {
        Path filePath = FXML_ROOT.resolve("static-property-child.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }
}