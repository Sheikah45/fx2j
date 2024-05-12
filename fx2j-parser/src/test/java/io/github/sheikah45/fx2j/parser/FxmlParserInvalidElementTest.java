package io.github.sheikah45.fx2j.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Execution(ExecutionMode.CONCURRENT)
public class FxmlParserInvalidElementTest {

    private static final Path FXML_ROOT = Path.of("src/test/resources/element/invalid");

    @Test
    public void testMultiDeclaration() throws Exception {
        Path filePath = FXML_ROOT.resolve("multi-declaration.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));
    }

    @Test
    public void testInclude() throws Exception {
        Path filePath = FXML_ROOT.resolve("include.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));

    }

    @Test
    public void testReference() throws Exception {
        Path filePath = FXML_ROOT.resolve("reference.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));

    }

    @Test
    public void testCopy() throws Exception {
        Path filePath = FXML_ROOT.resolve("copy.fxml");
        assertThrows(ParseException.class, () -> FxmlParser.readFxml(filePath));

    }
}