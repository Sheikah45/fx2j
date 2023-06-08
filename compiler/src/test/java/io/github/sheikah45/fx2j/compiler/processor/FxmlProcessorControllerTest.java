package io.github.sheikah45.fx2j.compiler.processor;

import io.github.sheikah45.fx2j.api.Fx2jBuilder;
import io.github.sheikah45.fx2j.compiler.testcontroller.ChangeHandlerController;
import io.github.sheikah45.fx2j.compiler.testcontroller.EventHandlerMethodController;
import io.github.sheikah45.fx2j.compiler.testcontroller.IncludeController;
import io.github.sheikah45.fx2j.compiler.testcontroller.PublicController;
import io.github.sheikah45.fx2j.compiler.testcontroller.SetterController;
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

public class FxmlProcessorControllerTest extends AbstractProcessorTest {

    private final Path controllerFxml = resourcesRoot.resolve("fxml/controller");

    @Test
    public void testControllerType() throws Exception {
        Path filePath = controllerFxml.resolve("controller-type.fxml");
        PublicController controller = buildAndRetrieveController(filePath);
        assertNotNull(controller);
        assertNotNull(controller.button);
    }

    private <C> C buildAndRetrieveController(Path mainFilePath, Path... supportingFilePaths) throws Exception {
        FxmlProcessor mainProcessor = new FxmlProcessor(mainFilePath, resourcesRoot, ROOT_PACKAGE, classLoader);
        FxmlProcessor[] supportingProcessors = Arrays.stream(supportingFilePaths)
                                                     .map(path -> new FxmlProcessor(path, resourcesRoot, ROOT_PACKAGE,
                                                                                    classLoader
                                                     ))
                                                     .toArray(FxmlProcessor[]::new);
        Fx2jBuilder<C, Object> fx2jBuilder = compileAndLoadBuilder(mainProcessor, supportingProcessors);
        fx2jBuilder.build(null, null, null, null);
        return fx2jBuilder.getController();
    }

    @Test
    public void testPublicController() throws Exception {
        Path filePath = controllerFxml.resolve("public-controller.fxml");
        PublicController controller = buildAndRetrieveController(filePath);
        assertNotNull(controller);
        assertNotNull(controller.button);
    }

    @Test
    public void testSetterController() throws Exception {
        Path filePath = controllerFxml.resolve("setter-controller.fxml");
        SetterController controller = buildAndRetrieveController(filePath);
        assertNotNull(controller);
        assertNotNull(controller.getButton());
    }

    @Test
    public void testEventHandlerMethodController() throws Exception {
        Path filePath = controllerFxml.resolve("event-handler-method.fxml");
        EventHandlerMethodController controller = buildAndRetrieveController(filePath);

        assertNotNull(controller);

        assertEquals(0, controller.eventActionCount);
        controller.withEventButton.fire();
        assertEquals(1, controller.eventActionCount);

        assertEquals(0, controller.noEventActionCount);
        controller.withoutEventButton.fire();
        assertEquals(1, controller.noEventActionCount);

        assertEquals(0, controller.contextRequestCount);
        controller.contextButton.getOnContextMenuRequested()
                                .handle(new ContextMenuEvent(null, null, null, 0, 0, 0, 0, false, null));
        assertEquals(1, controller.contextRequestCount);
    }

    @Test
    public void testOnChangeController() throws Exception {
        Path filePath = controllerFxml.resolve("change-handler-controller.fxml");
        ChangeHandlerController controller = buildAndRetrieveController(filePath);

        assertNull(controller.textValue);
        controller.textChangeButton.setText("button");
        assertEquals("button", controller.textValue);

        assertNotNull(controller);
        AnchorPane root = controller.root;
        assertEquals(controller.styleClasses, List.of());
        root.getStyleClass().add("test");
        assertEquals(controller.styleClasses, List.of("test"));

        assertEquals(controller.psudeoStyleClasses, Set.of());
        PseudoClass pseudoClass = PseudoClass.getPseudoClass("test");
        root.pseudoClassStateChanged(pseudoClass, true);
        assertEquals(controller.psudeoStyleClasses, Set.of(pseudoClass));

        assertEquals(controller.properties, Map.of());
        root.getProperties().put("key", "value");
        assertEquals(controller.properties, Map.of("key", "value"));
    }

    @Test
    public void testIncludeController() throws Exception {
        IncludeController controller = buildAndRetrieveController(controllerFxml.resolve("include-controller.fxml"),
                                                                  controllerFxml.resolve("public-controller.fxml"));
        assertNotNull(controller);
        assertNotNull(controller.paneController);
    }

    @Test
    public void testProvidedController() throws Exception {
        PublicController providedController = new PublicController();
        FxmlProcessor mainBuilderJavaFile = new FxmlProcessor(controllerFxml.resolve("public-controller.fxml"),
                                                              resourcesRoot, ROOT_PACKAGE, classLoader
        );
        Fx2jBuilder<PublicController, Object> fx2jBuilder = compileAndLoadBuilder(mainBuilderJavaFile);
        fx2jBuilder.build(providedController, null, null, null);
        PublicController controller = fx2jBuilder.getController();
        assertSame(providedController, controller);
        assertNotNull(providedController.button);
    }

    @Test
    public void testControllerFactory() throws Exception {
        PublicController providedController = new PublicController();
        FxmlProcessor mainBuilderProcessor = new FxmlProcessor(controllerFxml.resolve("public-controller.fxml"),
                                                               resourcesRoot, ROOT_PACKAGE, classLoader
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
    public void testProvidedControllerPrioritized() throws Exception {
        PublicController providedController = new PublicController();
        FxmlProcessor mainBuilderProcessor = new FxmlProcessor(controllerFxml.resolve("public-controller.fxml"),
                                                               resourcesRoot, ROOT_PACKAGE, classLoader
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
