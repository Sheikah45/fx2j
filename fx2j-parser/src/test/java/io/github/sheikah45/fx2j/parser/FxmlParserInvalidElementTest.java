package io.github.sheikah45.fx2j.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;


@Execution(ExecutionMode.CONCURRENT)
public class FxmlParserInvalidElementTest {

    private static final Path FXML_ROOT = Path.of("src/test/resources/element/invalid");

    @Test
    public void testEmptyImport() {
        Path filePath = FXML_ROOT.resolve("empty-import.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    public void testEmptyLanguage() {
        Path filePath = FXML_ROOT.resolve("empty-language.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    public void testNonDeclaration() {
        Path filePath = FXML_ROOT.resolve("non-declaration-root.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    public void testMultiDeclaration() {
        Path filePath = FXML_ROOT.resolve("multi-declaration.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    public void testNonCommonPropertyValue() {
        Path filePath = FXML_ROOT.resolve("property-non-common-attribute.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    public void testInclude() {
        Path filePath = FXML_ROOT.resolve("include.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    public void testReference() {
        Path filePath = FXML_ROOT.resolve("reference.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    public void testCopy() {
        Path filePath = FXML_ROOT.resolve("copy.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    public void testDefine() {
        Path filePath = FXML_ROOT.resolve("define.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }
}