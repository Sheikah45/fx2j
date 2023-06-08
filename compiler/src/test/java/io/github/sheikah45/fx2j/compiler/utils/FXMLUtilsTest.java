package io.github.sheikah45.fx2j.compiler.utils;

import io.github.sheikah45.fx2j.compiler.internal.model.FxmlComponents;
import io.github.sheikah45.fx2j.compiler.internal.model.FxmlNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class FXMLUtilsTest {

    @Test
    public void testReadFxml() {
        Path filePath = Path.of("src/test/resources/fxml/read/test.fxml");
        FxmlComponents fxmlComponents = FXMLUtils.readFxml(filePath);

        FxmlNode rootNode = fxmlComponents.rootNode();
        assertEquals(rootNode.name(), "AnchorPane");

        assertEquals(rootNode.children().size(), 5);

        FxmlNode button1 = rootNode.children().get(0);
        FxmlNode button2 = rootNode.children().get(1);
        FxmlNode button3 = rootNode.children().get(2);
        FxmlNode button4 = rootNode.children().get(3);
        FxmlNode button5 = rootNode.children().get(4);

        assertEquals("Button", button1.name());
        assertTrue(button1.attributes().isEmpty());
        assertTrue(button1.children().isEmpty());
        assertNull(button1.innerText());

        assertEquals("Button", button2.name());
        assertEquals("secondButton", button2.attributes().get("fx:id"));
        assertTrue(button2.children().isEmpty());
        assertNull(button2.innerText());

        assertEquals("Button", button3.name());
        assertTrue(button3.attributes().isEmpty());
        assertTrue(button3.children().isEmpty());
        assertEquals("Third Button", button3.innerText());

        assertEquals("Button", button4.name());
        assertTrue(button4.attributes().isEmpty());
        assertEquals(1, button4.children().size());
        assertEquals("Test After", button4.innerText());

        FxmlNode paddingNode = button4.children().get(0);
        assertEquals("padding", paddingNode.name());
        assertEquals(paddingNode.children().size(), 1);
        FxmlNode insetsNode = paddingNode.children().get(0);
        assertEquals("1", insetsNode.attributes().get("top"));
        assertEquals("2", insetsNode.attributes().get("bottom"));
        assertEquals("3", insetsNode.attributes().get("right"));
        assertEquals("4", insetsNode.attributes().get("left"));

        assertEquals("Button", button5.name());
        assertTrue(button5.attributes().isEmpty());
        assertEquals(1, button5.children().size());
        assertEquals("Empty After", button5.innerText());

        Set<String> imports = fxmlComponents.imports();
        assertEquals(imports.size(), 3);
        assertTrue(imports.contains("javafx.scene.control.Button"));
        assertTrue(imports.contains("javafx.scene.layout.AnchorPane"));
        assertTrue(imports.contains("javafx.geometry.Insets"));
    }
}