package io.github.sheikah45.fx2j.processor.processor;

import io.github.sheikah45.fx2j.processor.FxmlProcessor;
import io.github.sheikah45.fx2j.processor.ProcessorException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FxmlProcessorFailureTest extends AbstractProcessorTest {

    private static final Path FAIL_FXML = RESOURCES_ROOT.resolve("fxml/failure");

    @Test
    void testInferredMapWrongType() {
        assertThrows(ProcessorException.class,
                     () -> new FxmlProcessor(FAIL_FXML.resolve("inferred-map.fxml"), RESOURCES_ROOT, ROOT_PACKAGE,
                                             classLoader
                     ));
    }

    @Test
    void testInferredListWrongType() {
        assertThrows(ProcessorException.class,
                     () -> new FxmlProcessor(FAIL_FXML.resolve("inferred-list.fxml"), RESOURCES_ROOT, ROOT_PACKAGE,
                                             classLoader
                     ));
    }

    @Test
    void testPrivateController() {
        Path filePath = FAIL_FXML.resolve("private-controller.fxml");
        assertThrows(ProcessorException.class, () -> new FxmlProcessor(filePath, RESOURCES_ROOT, ROOT_PACKAGE,
                                                                       classLoader));
    }

    @Test
    void testMapStaticProperty() {
        Path filePath = FAIL_FXML.resolve("map-static-property.fxml");
        assertThrows(ProcessorException.class, () -> new FxmlProcessor(filePath, RESOURCES_ROOT, ROOT_PACKAGE,
                                                                       classLoader));
    }

    @Test
    void testMapStaticChild() {
        Path filePath = FAIL_FXML.resolve("map-static-child.fxml");
        assertThrows(ProcessorException.class, () -> new FxmlProcessor(filePath, RESOURCES_ROOT, ROOT_PACKAGE,
                                                                       classLoader));
    }

    @Test
    void testListStaticChild() {
        Path filePath = FAIL_FXML.resolve("list-static-child.fxml");
        assertThrows(ProcessorException.class, () -> new FxmlProcessor(filePath, RESOURCES_ROOT, ROOT_PACKAGE,
                                                                       classLoader));
    }

    @Test
    void testRelativeResource() {
        Path filePath = FAIL_FXML.resolve("resource-location.fxml");
        assertThrows(ProcessorException.class, () -> new FxmlProcessor(filePath, RESOURCES_ROOT, ROOT_PACKAGE,
                                                                       classLoader));
    }
}
