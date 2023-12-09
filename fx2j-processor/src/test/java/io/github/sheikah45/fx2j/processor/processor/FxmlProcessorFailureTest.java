package io.github.sheikah45.fx2j.processor.processor;

import io.github.sheikah45.fx2j.processor.FxmlProcessor;
import io.github.sheikah45.fx2j.processor.ProcessorException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FxmlProcessorFailureTest extends AbstractProcessorTest {

    private final Path failFxml = resourcesRoot.resolve("fxml/failure");

    @Test
    public void testInferredMapWrongType() {
        assertThrows(ProcessorException.class,
                     () -> new FxmlProcessor(failFxml.resolve("inferred-map.fxml"), resourcesRoot, ROOT_PACKAGE,
                                             classLoader
                     ));
    }

    @Test
    public void testInferredListWrongType() {
        assertThrows(ProcessorException.class,
                     () -> new FxmlProcessor(failFxml.resolve("inferred-list.fxml"), resourcesRoot, ROOT_PACKAGE,
                                             classLoader
                     ));
    }

    @Test
    public void testPrivateController() {
        Path filePath = failFxml.resolve("private-controller.fxml");
        assertThrows(ProcessorException.class, () -> new FxmlProcessor(filePath, resourcesRoot, ROOT_PACKAGE,
                                                                       classLoader));
    }

    @Test
    public void testIdNoController() {
        Path filePath = failFxml.resolve("id-no-controller.fxml");
        assertThrows(ProcessorException.class, () -> new FxmlProcessor(filePath, resourcesRoot, ROOT_PACKAGE,
                                                                       classLoader));
    }

    @Test
    public void testMapStaticProperty() {
        Path filePath = failFxml.resolve("map-static-property.fxml");
        assertThrows(ProcessorException.class, () -> new FxmlProcessor(filePath, resourcesRoot, ROOT_PACKAGE,
                                                                       classLoader));
    }

    @Test
    public void testMapStaticChild() {
        Path filePath = failFxml.resolve("map-static-child.fxml");
        assertThrows(ProcessorException.class, () -> new FxmlProcessor(filePath, resourcesRoot, ROOT_PACKAGE,
                                                                       classLoader));
    }

    @Test
    public void testListStaticChild() {
        Path filePath = failFxml.resolve("list-static-child.fxml");
        assertThrows(ProcessorException.class, () -> new FxmlProcessor(filePath, resourcesRoot, ROOT_PACKAGE,
                                                                       classLoader));
    }

    @Test
    public void testRelativeResource() {
        Path filePath = failFxml.resolve("resource-location.fxml");
        assertThrows(ProcessorException.class, () -> new FxmlProcessor(filePath, resourcesRoot, ROOT_PACKAGE,
                                                                       classLoader));
    }
}
