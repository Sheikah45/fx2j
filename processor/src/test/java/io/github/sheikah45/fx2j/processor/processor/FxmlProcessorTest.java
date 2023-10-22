package io.github.sheikah45.fx2j.processor.processor;

import io.github.sheikah45.fx2j.api.Fx2jBuilder;
import io.github.sheikah45.fx2j.processor.FxmlProcessor;
import io.github.sheikah45.fx2j.processor.testutils.CopyObject;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public class FxmlProcessorTest extends AbstractProcessorTest {

    private final Path processFxml = resourcesRoot.resolve("fxml/process");

    @Test
    public void testRootOnly() throws Exception {
        AnchorPane root = buildAndRetrieveRoot(processFxml.resolve("simple-root.fxml"));
        assertNotNull(root);
    }

    private <R> R buildAndRetrieveRoot(Path mainFilePath, Path... supportingFilePaths) throws Exception {
        FxmlProcessor mainProcessor = new FxmlProcessor(mainFilePath, resourcesRoot, ROOT_PACKAGE, classLoader);
        FxmlProcessor[] supportingProcessors = Arrays.stream(supportingFilePaths)
                                                     .map(path -> new FxmlProcessor(path, resourcesRoot, ROOT_PACKAGE,
                                                                                    classLoader
                                                     ))
                                                     .toArray(FxmlProcessor[]::new);
        Fx2jBuilder<Object, R> fx2jBuilder = compileAndLoadBuilder(mainProcessor, supportingProcessors);
        fx2jBuilder.build(null, null, null, null);
        return fx2jBuilder.getRoot();
    }

    @Test
    public void testStaticProperties() throws Exception {
        AnchorPane root = buildAndRetrieveRoot(processFxml.resolve("static-property.fxml"));
        assertNotNull(root);
        assertEquals(1, GridPane.getColumnIndex(root));
        assertEquals(2, GridPane.getRowIndex(root));
    }

    @Test
    public void testEnumStaticProperties() throws Exception {
        AnchorPane root = buildAndRetrieveRoot(processFxml.resolve("enum-static-property.fxml"));
        assertNotNull(root);
        assertEquals(HPos.CENTER, GridPane.getHalignment(root));
        assertEquals(VPos.TOP, GridPane.getValignment(root));
    }

    @Test
    public void testFullyQualifiedNames() throws Exception {
        AnchorPane root = buildAndRetrieveRoot(processFxml.resolve("fully-qualified-nodes.fxml"));
        assertNotNull(root);
        assertEquals(1, root.getChildren().size());
    }

    @Test
    public void testListProperties() throws Exception {
        AnchorPane root = buildAndRetrieveRoot(processFxml.resolve("list-property.fxml"));
        List<Node> children = root.getChildren();
        assertEquals(3, children.size());
        children.forEach(node -> assertSame(Button.class, node.getClass()));
    }

    @Test
    public void testDefaultProperties() throws Exception {
        AnchorPane root = buildAndRetrieveRoot(processFxml.resolve("default-property.fxml"));
        List<Node> children = root.getChildren();
        assertEquals(1, children.size());
        Button button = (Button) children.get(0);

        assertEquals("test test", button.getText());
    }

    @Test
    public void testPropertyInnerText() throws Exception {
        AnchorPane root = buildAndRetrieveRoot(processFxml.resolve("property-inner-text.fxml"));
        List<Node> children = root.getChildren();
        assertEquals(1, children.size());
        Button button = (Button) children.get(0);

        assertEquals("test", button.getText());
    }

    @Test
    public void testPropertyAttribute() throws Exception {
        AnchorPane root = buildAndRetrieveRoot(processFxml.resolve("attribute-property.fxml"));
        List<Node> children = root.getChildren();
        assertEquals(1, children.size());
        Button button = (Button) children.get(0);

        assertEquals("test", button.getText());
    }

    @Test
    public void testPropertiesMap() throws Exception {
        AnchorPane root = buildAndRetrieveRoot(processFxml.resolve("map-property.fxml"));
        assertEquals(Map.of("test1", "t1", "test2", "123"), root.getProperties());
    }

    @Test
    public void testBasicMap() throws Exception {
        Map<Object, Object> root = buildAndRetrieveRoot(processFxml.resolve("basic-map.fxml"));
        assertEquals(Map.of("key1", "val1", "key2", "val2", "key3", 3), root);
    }

    @Test
    public void testBasicList() throws Exception {
        List<Object> root = buildAndRetrieveRoot(processFxml.resolve("basic-list.fxml"));
        assertEquals(List.of("item1", 2), root);
    }

    @Test
    public void testInferredMap() throws Exception {
        Map<Object, Object> root = buildAndRetrieveRoot(processFxml.resolve("inferred-map.fxml"));
        assertEquals(Map.of("key1", 1, "key2", 2, "key3", 3), root);
    }

    @Test
    public void testNestedClassDirect() throws Exception {
        SpinnerValueFactory.IntegerSpinnerValueFactory root = buildAndRetrieveRoot(
                processFxml.resolve("nested-class-direct.fxml"));
        assertNotNull(root);
        assertEquals(0, root.getMin());
        assertEquals(10, root.getMax());
    }

    @Test
    public void testNestedClassStar() throws Exception {
        SpinnerValueFactory.IntegerSpinnerValueFactory root = buildAndRetrieveRoot(
                processFxml.resolve("nested-class-star-import.fxml"));
        assertNotNull(root);
        assertEquals(0, root.getMin());
        assertEquals(10, root.getMax());
    }

    @Test
    public void testGenericTypeVariable() throws Exception {
        TableView<Object> root = buildAndRetrieveRoot(processFxml.resolve("generic-with-type-variable.fxml"));
        assertNotNull(root);
        assertEquals(1, root.getColumns().size());
    }

    @Test
    public void testInclude() throws Exception {
        SplitPane root = buildAndRetrieveRoot(processFxml.resolve("include.fxml"),
                                              processFxml.resolve("simple-root.fxml"));
        assertNotNull(root);
        assertEquals(1, root.getItems().size());
        assertEquals(AnchorPane.class, root.getItems().get(0).getClass());
    }

    @Test
    public void testConstant() throws Exception {
        int root = buildAndRetrieveRoot(processFxml.resolve("constant.fxml"));
        assertEquals(Double.MAX_EXPONENT, root);
    }

    @Test
    public void testCopy() throws Exception {
        List<CopyObject> root = buildAndRetrieveRoot(processFxml.resolve("copy.fxml"));
        assertEquals(2, root.size());
        assertNotSame(root.get(0), root.get(1));
    }

    @Test
    public void testReference() throws Exception {
        List<CopyObject> root = buildAndRetrieveRoot(processFxml.resolve("reference.fxml"));
        assertEquals(2, root.size());
        assertSame(root.get(0), root.get(1));
    }

    @Test
    public void testFactory() throws Exception {
        ObservableList<Object> root = buildAndRetrieveRoot(processFxml.resolve("factory.fxml"));
        assertNotNull(root);
    }

    @Test
    public void testArray() throws Exception {
        SplitPane root = buildAndRetrieveRoot(processFxml.resolve("array.fxml"));
        root.getItems().add(new Pane());
        root.getItems().add(new Pane());
        root.getItems().add(new Pane());
        root.getItems().add(new Pane());
        root.getItems().add(new Pane());
        assertNotNull(root);
        assertArrayEquals(new double[]{1, 2, 3, 4}, root.getDividerPositions());
    }

    @Test
    public void testValue() throws Exception {
        List<Object> root = buildAndRetrieveRoot(processFxml.resolve("value.fxml"));
        assertNotNull(root);
        assertEquals(9, root.size());
        assertEquals(Duration.valueOf("1s"), root.get(0));
        assertEquals(1d, root.get(1));
        assertEquals(Double.POSITIVE_INFINITY, root.get(2));
        assertEquals(Double.NEGATIVE_INFINITY, root.get(3));
        assertEquals(Double.NaN, root.get(4));
        assertEquals(Float.POSITIVE_INFINITY, root.get(5));
        assertEquals(Float.NEGATIVE_INFINITY, root.get(6));
        assertEquals(Float.NaN, root.get(7));
        assertEquals(VPos.CENTER, root.get(8));
    }

    @Test
    public void testDefineBefore() throws Exception {
        List<CopyObject> root = buildAndRetrieveRoot(processFxml.resolve("define-before.fxml"));
        assertEquals(1, root.size());
    }

    @Test
    public void testDefineAfter() throws Exception {
        List<CopyObject> root = buildAndRetrieveRoot(processFxml.resolve("define-after.fxml"));
        assertEquals(1, root.size());
    }

    @Test
    public void testVariable() throws Exception {
        Label root = buildAndRetrieveRoot(processFxml.resolve("variable.fxml"));
        assertEquals("hello", root.getText());
    }

    @Test
    public void testEscapeVariable() throws Exception {
        Label root = buildAndRetrieveRoot(processFxml.resolve("escape-variable.fxml"));
        assertEquals("$obj", root.getText());
    }

    @Test
    public void testEscapeResources() throws Exception {
        Label root = buildAndRetrieveRoot(processFxml.resolve("escape-resources.fxml"));
        assertEquals("%obj", root.getText());
    }

    @Test
    public void testProvidedRoot() throws Exception {
        ArrayList<Double> providedRoot = new ArrayList<>();
        FxmlProcessor mainBuilderJavaFile = new FxmlProcessor(processFxml.resolve("provided-root.fxml"), resourcesRoot,
                                                              ROOT_PACKAGE, classLoader
        );
        Fx2jBuilder<Object, ArrayList<Double>> fx2jBuilder = compileAndLoadBuilder(mainBuilderJavaFile);
        fx2jBuilder.build(null, providedRoot, null, null);
        ArrayList<Double> root = fx2jBuilder.getRoot();
        assertSame(providedRoot, root);
        assertEquals(1, providedRoot.size());
        assertEquals(1d, providedRoot.get(0));
    }

    @Test
    public void testResources() throws Exception {
        FxmlProcessor mainBuilderJavaFile = new FxmlProcessor(processFxml.resolve("resources.fxml"), resourcesRoot,
                                                              ROOT_PACKAGE, classLoader
        );
        Fx2jBuilder<Object, Label> fx2jBuilder = compileAndLoadBuilder(mainBuilderJavaFile);
        fx2jBuilder.build(null, null, new PropertyResourceBundle(
                FxmlProcessorTest.class.getResource("/message.properties").openStream()), null);
        Label root = fx2jBuilder.getRoot();
        assertEquals("hello", root.getText());
    }

}
