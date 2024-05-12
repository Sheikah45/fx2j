package io.github.sheikah45.fx2j.processor.processor;

import io.github.sheikah45.fx2j.processor.FxmlProcessor;
import io.github.sheikah45.fx2j.processor.ProcessorException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FxmlProcessorFailureTest extends AbstractProcessorTest {

    private static final Path FAIL_FXML = RESOURCES_ROOT.resolve("fxml/failure");

    @Test
    public void testInferredMapWrongType() {
        assertThrows(ProcessorException.class,
                     () -> new FxmlProcessor(FAIL_FXML.resolve("inferred-map.fxml"), RESOURCES_ROOT, ROOT_PACKAGE,
                                             classLoader
                     ));
    }

    @Test
    public void testInferredListWrongType() {
        assertThrows(ProcessorException.class,
                     () -> new FxmlProcessor(FAIL_FXML.resolve("inferred-list.fxml"), RESOURCES_ROOT, ROOT_PACKAGE,
                                             classLoader
                     ));
    }

    @Test
    public void testPrivateController() {
        Path filePath = FAIL_FXML.resolve("private-controller.fxml");
        assertThrows(ProcessorException.class, () -> new FxmlProcessor(filePath, RESOURCES_ROOT, ROOT_PACKAGE,
                                                                       classLoader));
    }

    @Test
    public void testIdNoController() {
        Path filePath = FAIL_FXML.resolve("id-no-controller.fxml");
        assertThrows(ProcessorException.class, () -> new FxmlProcessor(filePath, RESOURCES_ROOT, ROOT_PACKAGE,
                                                                       classLoader));
    }

    @Test
    public void testMapStaticProperty() {
        Path filePath = FAIL_FXML.resolve("map-static-property.fxml");
        assertThrows(ProcessorException.class, () -> new FxmlProcessor(filePath, RESOURCES_ROOT, ROOT_PACKAGE,
                                                                       classLoader));
    }

    @Test
    public void testMapStaticChild() {
        Path filePath = FAIL_FXML.resolve("map-static-child.fxml");
        assertThrows(ProcessorException.class, () -> new FxmlProcessor(filePath, RESOURCES_ROOT, ROOT_PACKAGE,
                                                                       classLoader));
    }

    @Test
    public void testListStaticChild() {
        Path filePath = FAIL_FXML.resolve("list-static-child.fxml");
        assertThrows(ProcessorException.class, () -> new FxmlProcessor(filePath, RESOURCES_ROOT, ROOT_PACKAGE,
                                                                       classLoader));
    }

    @Test
    public void testRelativeResource() {
        Path filePath = FAIL_FXML.resolve("resource-location.fxml");
        assertThrows(ProcessorException.class, () -> new FxmlProcessor(filePath, RESOURCES_ROOT, ROOT_PACKAGE,
                                                                       classLoader));
    }
}
