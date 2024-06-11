package io.github.sheikah45.fx2j.processor.processor;

import io.github.sheikah45.fx2j.api.Fx2jBuilder;
import io.github.sheikah45.fx2j.processor.FxmlProcessor;
import io.github.sheikah45.fx2j.processor.testcontroller.ChangeHandlerController;
import io.github.sheikah45.fx2j.processor.testcontroller.EventHandlerMethodController;
import io.github.sheikah45.fx2j.processor.testcontroller.IncludeController;
import io.github.sheikah45.fx2j.processor.testcontroller.PublicController;
import io.github.sheikah45.fx2j.processor.testcontroller.SetterController;
import javafx.css.PseudoClass;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.AnchorPane;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class FxmlProcessorControllerTest extends AbstractProcessorTest {

    private static final Path CONTROLLER_FXML = RESOURCES_ROOT.resolve("fxml/controller");

    @Test
    void testControllerType() throws Exception {
        Path filePath = CONTROLLER_FXML.resolve("controller-type.fxml");
        PublicController controller = buildAndRetrieveController(filePath);
        assertNotNull(controller);
        assertNotNull(controller.button);
    }

    private <C> C buildAndRetrieveController(Path mainFilePath, Path... supportingFilePaths) throws Exception {
        FxmlProcessor mainProcessor = new FxmlProcessor(mainFilePath, RESOURCES_ROOT, ROOT_PACKAGE, classLoader);
        FxmlProcessor[] supportingProcessors = Arrays.stream(supportingFilePaths)
                                                     .map(path -> new FxmlProcessor(path, RESOURCES_ROOT, ROOT_PACKAGE,
                                                                                    classLoader
                                                     ))
                                                     .toArray(FxmlProcessor[]::new);
        Fx2jBuilder<C, Object> fx2jBuilder = compileAndLoadBuilder(mainProcessor, supportingProcessors);
        fx2jBuilder.build(null, null, null, null);
        return fx2jBuilder.getController();
    }

    @Test
    void testPublicController() throws Exception {
        Path filePath = CONTROLLER_FXML.resolve("public-controller.fxml");
        PublicController controller = buildAndRetrieveController(filePath);
        assertNotNull(controller);
        assertNotNull(controller.button);
    }

    @Test
    void testSetterController() throws Exception {
        Path filePath = CONTROLLER_FXML.resolve("setter-controller.fxml");
        SetterController controller = buildAndRetrieveController(filePath);
        assertNotNull(controller);
        assertNotNull(controller.getButton());
    }

    @Test
    void testEventHandlerMethodController() throws Exception {
        Path filePath = CONTROLLER_FXML.resolve("event-handler-method.fxml");
        EventHandlerMethodController controller = buildAndRetrieveController(filePath);

        assertNotNull(controller);

        assertEquals(0, controller.eventActionCount);
        controller.withEventButton.fire();
        assertEquals(1, controller.eventActionCount);

        assertEquals(0, controller.noEventActionCount);
        controller.withoutEventButton.fire();
        assertEquals(1, controller.noEventActionCount);

        assertEquals(0, controller.throwsEventCount);
        controller.throwingEventButton.fire();
        assertEquals(1, controller.throwsEventCount);

        assertEquals(0, controller.throwsNoEventCount);
        controller.throwingNoEventButton.fire();
        assertEquals(1, controller.throwsNoEventCount);

        assertEquals(0, controller.contextRequestCount);
        controller.contextButton.getOnContextMenuRequested()
                                .handle(new ContextMenuEvent(null, null, null, 0, 0, 0, 0, false, null));
        assertEquals(1, controller.contextRequestCount);
    }

    @Test
    void testOnChangeController() throws Exception {
        Path filePath = CONTROLLER_FXML.resolve("change-handler-controller.fxml");
        ChangeHandlerController controller = buildAndRetrieveController(filePath);

        assertNull(controller.textValue);
        controller.textChangeButton.setText("button");
        assertEquals("button", controller.textValue);

        assertNotNull(controller);
        AnchorPane root = controller.root;
        assertEquals(controller.styleClasses, List.of());
        root.getStyleClass().add("test");
        assertEquals(controller.styleClasses, List.of("test"));

        assertEquals(controller.pseudoClassStates, Set.of());
        PseudoClass pseudoClass = PseudoClass.getPseudoClass("test");
        root.pseudoClassStateChanged(pseudoClass, true);
        assertEquals(controller.pseudoClassStates, Set.of(pseudoClass));

        assertEquals(controller.properties, Map.of());
        root.getProperties().put("key", "value");
        assertEquals(controller.properties, Map.of("key", "value"));
    }

    @Test
    void testIncludeController() throws Exception {
        IncludeController controller = buildAndRetrieveController(CONTROLLER_FXML.resolve("include-controller.fxml"),
                                                                  CONTROLLER_FXML.resolve("public-controller.fxml"));
        assertNotNull(controller);
        assertNotNull(controller.paneController);
    }

    @Test
    void testProvidedController() throws Exception {
        PublicController providedController = new PublicController();
        FxmlProcessor mainBuilderJavaFile = new FxmlProcessor(CONTROLLER_FXML.resolve("public-controller.fxml"),
                                                              RESOURCES_ROOT, ROOT_PACKAGE, classLoader
        );
        Fx2jBuilder<PublicController, Object> fx2jBuilder = compileAndLoadBuilder(mainBuilderJavaFile);
        fx2jBuilder.build(providedController, null, null, null);
        PublicController controller = fx2jBuilder.getController();
        assertSame(providedController, controller);
        assertNotNull(providedController.button);
    }

    @Test
    void testControllerFactory() throws Exception {
        PublicController providedController = new PublicController();
        FxmlProcessor mainBuilderProcessor = new FxmlProcessor(CONTROLLER_FXML.resolve("public-controller.fxml"),
                                                               RESOURCES_ROOT, ROOT_PACKAGE, classLoader
        );
        Fx2jBuilder<PublicController, Object> fx2jBuilder = compileAndLoadBuilder(mainBuilderProcessor);
        fx2jBuilder.build(null, null, null, clazz -> {
            if (clazz == PublicController.class) {
                return providedController;
            } else {
                return null;
            }
        });
        PublicController controller = fx2jBuilder.getController();
        assertSame(providedController, controller);
        assertNotNull(providedController.button);
    }

    @Test
    void testProvidedControllerPrioritized() throws Exception {
        PublicController providedController = new PublicController();
        FxmlProcessor mainBuilderProcessor = new FxmlProcessor(CONTROLLER_FXML.resolve("public-controller.fxml"),
                                                               RESOURCES_ROOT, ROOT_PACKAGE, classLoader
        );
        Fx2jBuilder<PublicController, Object> fx2jBuilder = compileAndLoadBuilder(mainBuilderProcessor);
        fx2jBuilder.build(providedController, null, null, clazz -> {
            if (clazz == PublicController.class) {
                return new PublicController();
            } else {
                return null;
            }
        });
        PublicController controller = fx2jBuilder.getController();
        assertSame(providedController, controller);
        assertNotNull(providedController.button);
    }
}
