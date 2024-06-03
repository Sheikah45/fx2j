package io.github.sheikah45.fx2j.processor.processor;

import io.github.sheikah45.fx2j.api.Fx2jBuilder;
import io.github.sheikah45.fx2j.processor.FxmlProcessor;
import io.github.sheikah45.fx2j.processor.testutils.CopyObject;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PropertyResourceBundle;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FxmlProcessorTest extends AbstractProcessorTest {

    private static final Path PROCESS_FXML = RESOURCES_ROOT.resolve("fxml/process");

    @Test
    void testRootOnly() throws Exception {
        AnchorPane root = buildAndRetrieveRoot(PROCESS_FXML.resolve("simple-root.fxml"));
        assertNotNull(root);
    }

    private <R> R buildAndRetrieveRoot(Path mainFilePath, Path... supportingFilePaths) throws Exception {
        FxmlProcessor mainProcessor = new FxmlProcessor(mainFilePath, RESOURCES_ROOT, ROOT_PACKAGE, classLoader);
        FxmlProcessor[] supportingProcessors = Arrays.stream(supportingFilePaths)
                                                     .map(path -> new FxmlProcessor(path, RESOURCES_ROOT, ROOT_PACKAGE,
                                                                                    classLoader
                                                     ))
                                                     .toArray(FxmlProcessor[]::new);
        Fx2jBuilder<Object, R> fx2jBuilder = compileAndLoadBuilder(mainProcessor, supportingProcessors);
        fx2jBuilder.build(null, null, null, null);
        return fx2jBuilder.getRoot();
    }

    @Test
    void testStaticProperties() throws Exception {
        AnchorPane root = buildAndRetrieveRoot(PROCESS_FXML.resolve("static-property.fxml"));
        assertNotNull(root);
        assertEquals(1, GridPane.getColumnIndex(root));
        assertEquals(2, GridPane.getRowIndex(root));
    }

    @Test
    void testEnumStaticProperties() throws Exception {
        AnchorPane root = buildAndRetrieveRoot(PROCESS_FXML.resolve("enum-static-property.fxml"));
        assertNotNull(root);
        assertEquals(HPos.CENTER, GridPane.getHalignment(root));
        assertEquals(VPos.TOP, GridPane.getValignment(root));
    }

    @Test
    void testFullyQualifiedNames() throws Exception {
        AnchorPane root = buildAndRetrieveRoot(PROCESS_FXML.resolve("fully-qualified-nodes.fxml"));
        assertNotNull(root);
        assertEquals(1, root.getChildren().size());
    }

    @Test
    void testListProperties() throws Exception {
        AnchorPane root = buildAndRetrieveRoot(PROCESS_FXML.resolve("list-property.fxml"));
        List<Node> children = root.getChildren();
        assertEquals(3, children.size());
        children.forEach(node -> assertSame(Button.class, node.getClass()));
    }

    @Test
    void testDefaultProperties() throws Exception {
        AnchorPane root = buildAndRetrieveRoot(PROCESS_FXML.resolve("default-property.fxml"));
        List<Node> children = root.getChildren();
        assertEquals(1, children.size());
        Button button = (Button) children.getFirst();

        assertEquals("test test", button.getText());
    }

    @Test
    void testPropertyInnerText() throws Exception {
        AnchorPane root = buildAndRetrieveRoot(PROCESS_FXML.resolve("property-inner-text.fxml"));
        List<Node> children = root.getChildren();
        assertEquals(1, children.size());
        Button button = (Button) children.getFirst();

        assertEquals("test", button.getText());
    }

    @Test
    void testDefaultPropertyElement() throws Exception {
        Tab root = buildAndRetrieveRoot(PROCESS_FXML.resolve("default-property-element.fxml"));
        assertInstanceOf(Button.class, root.getContent());
    }

    @Test
    void testPropertyAttribute() throws Exception {
        AnchorPane root = buildAndRetrieveRoot(PROCESS_FXML.resolve("attribute-property.fxml"));
        List<Node> children = root.getChildren();
        assertEquals(1, children.size());
        Button button = (Button) children.getFirst();

        assertEquals("test", button.getText());
    }

    @Test
    void testPropertiesMap() throws Exception {
        AnchorPane root = buildAndRetrieveRoot(PROCESS_FXML.resolve("map-property.fxml"));
        assertEquals(Map.of("test1", "t1", "test2", "123"), root.getProperties());
    }

    @Test
    void testBasicMap() throws Exception {
        Map<Object, Object> root = buildAndRetrieveRoot(PROCESS_FXML.resolve("basic-map.fxml"));
        assertEquals(Map.of("key1", "val1", "key2", "val2", "key3", 3), root);
    }

    @Test
    void testBasicList() throws Exception {
        List<Object> root = buildAndRetrieveRoot(PROCESS_FXML.resolve("basic-list.fxml"));
        assertEquals(List.of("item1", 2), root);
    }

    @Test
    void testObservableList() throws Exception {
        ObservableList<Object> root = buildAndRetrieveRoot(PROCESS_FXML.resolve("observable-list.fxml"));
        assertEquals(List.of("item1", 2), root);
    }

    @Test
    void testInferredMap() throws Exception {
        Map<Object, Object> root = buildAndRetrieveRoot(PROCESS_FXML.resolve("inferred-map.fxml"));
        assertEquals(Map.of("key1", 1, "key2", 2, "key3", 3), root);
    }

    @Test
    void testNestedClassDirect() throws Exception {
        SpinnerValueFactory.IntegerSpinnerValueFactory root = buildAndRetrieveRoot(
                PROCESS_FXML.resolve("nested-class-direct.fxml"));
        assertNotNull(root);
        assertEquals(0, root.getMin());
        assertEquals(10, root.getMax());
    }

    @Test
    void testNestedClassStar() throws Exception {
        SpinnerValueFactory.IntegerSpinnerValueFactory root = buildAndRetrieveRoot(
                PROCESS_FXML.resolve("nested-class-star-import.fxml"));
        assertNotNull(root);
        assertEquals(0, root.getMin());
        assertEquals(10, root.getMax());
    }

    @Test
    void testGenericTypeVariable() throws Exception {
        TableView<Object> root = buildAndRetrieveRoot(PROCESS_FXML.resolve("generic-with-type-variable.fxml"));
        assertNotNull(root);
        assertEquals(1, root.getColumns().size());
    }

    @Test
    void testInclude() throws Exception {
        SplitPane root = buildAndRetrieveRoot(PROCESS_FXML.resolve("include.fxml"),
                                              PROCESS_FXML.resolve("simple-root.fxml"));
        assertNotNull(root);
        assertEquals(1, root.getItems().size());
        assertEquals(AnchorPane.class, root.getItems().getFirst().getClass());
    }

    @Test
    void testConstant() throws Exception {
        int root = buildAndRetrieveRoot(PROCESS_FXML.resolve("constant.fxml"));
        assertEquals(Double.MAX_EXPONENT, root);
    }

    @Test
    void testCopy() throws Exception {
        List<CopyObject> root = buildAndRetrieveRoot(PROCESS_FXML.resolve("copy.fxml"));
        assertEquals(2, root.size());
        assertNotSame(root.get(0), root.get(1));
    }

    @Test
    void testReference() throws Exception {
        List<CopyObject> root = buildAndRetrieveRoot(PROCESS_FXML.resolve("reference.fxml"));
        assertEquals(2, root.size());
        assertSame(root.get(0), root.get(1));
    }

    @Test
    void testFactory() throws Exception {
        ObservableList<Object> root = buildAndRetrieveRoot(PROCESS_FXML.resolve("factory.fxml"));
        assertNotNull(root);
    }

    @Test
    void testArray() throws Exception {
        SplitPane root = buildAndRetrieveRoot(PROCESS_FXML.resolve("array.fxml"));
        root.getItems().add(new Pane());
        root.getItems().add(new Pane());
        root.getItems().add(new Pane());
        root.getItems().add(new Pane());
        root.getItems().add(new Pane());
        assertNotNull(root);
        assertArrayEquals(new double[]{1, 2, 3, 4}, root.getDividerPositions());
    }

    @Test
    void testValue() throws Exception {
        List<Object> root = buildAndRetrieveRoot(PROCESS_FXML.resolve("value.fxml"));
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
    void testDefineBefore() throws Exception {
        List<CopyObject> root = buildAndRetrieveRoot(PROCESS_FXML.resolve("define-before.fxml"));
        assertEquals(1, root.size());
    }

    @Test
    void testDefineAfter() throws Exception {
        List<CopyObject> root = buildAndRetrieveRoot(PROCESS_FXML.resolve("define-after.fxml"));
        assertEquals(1, root.size());
    }

    @Test
    void testVariable() throws Exception {
        Label root = buildAndRetrieveRoot(PROCESS_FXML.resolve("variable.fxml"));
        assertEquals("hello", root.getText());
    }

    @Test
    void testEscapeVariable() throws Exception {
        Label root = buildAndRetrieveRoot(PROCESS_FXML.resolve("escape-variable.fxml"));
        assertEquals("$obj", root.getText());
    }

    @Test
    void testEscapeResources() throws Exception {
        Label root = buildAndRetrieveRoot(PROCESS_FXML.resolve("escape-resources.fxml"));
        assertEquals("%obj", root.getText());
    }

    @Test
    void testProvidedRoot() throws Exception {
        ArrayList<Double> providedRoot = new ArrayList<>();
        FxmlProcessor mainBuilderJavaFile = new FxmlProcessor(PROCESS_FXML.resolve("provided-root.fxml"),
                                                              RESOURCES_ROOT,
                                                              ROOT_PACKAGE, classLoader
        );
        Fx2jBuilder<Object, ArrayList<Double>> fx2jBuilder = compileAndLoadBuilder(mainBuilderJavaFile);
        fx2jBuilder.build(null, providedRoot, null, null);
        ArrayList<Double> root = fx2jBuilder.getRoot();
        assertSame(providedRoot, root);
        assertEquals(1, providedRoot.size());
        assertEquals(1d, providedRoot.getFirst());
    }

    @Test
    void testResources() throws Exception {
        FxmlProcessor mainBuilderJavaFile = new FxmlProcessor(PROCESS_FXML.resolve("resources.fxml"), RESOURCES_ROOT,
                                                              ROOT_PACKAGE, classLoader
        );
        Fx2jBuilder<Object, Label> fx2jBuilder = compileAndLoadBuilder(mainBuilderJavaFile);
        fx2jBuilder.build(null, null, new PropertyResourceBundle(
                Objects.requireNonNull(FxmlProcessorTest.class.getResource("/message.properties")).openStream()), null);
        Label root = fx2jBuilder.getRoot();
        assertEquals("hello", root.getText());
    }

    @Test
    void testSimpleExpression() throws Exception {
        FxmlProcessor mainBuilderJavaFile = new FxmlProcessor(PROCESS_FXML.resolve("simple-expression.fxml"),
                                                              RESOURCES_ROOT,
                                                              ROOT_PACKAGE, classLoader
        );
        Fx2jBuilder<Object, AnchorPane> fx2jBuilder = compileAndLoadBuilder(mainBuilderJavaFile);
        fx2jBuilder.build(null, null, new PropertyResourceBundle(
                Objects.requireNonNull(FxmlProcessorTest.class.getResource("/message.properties")).openStream()), null);
        AnchorPane root = fx2jBuilder.getRoot();
        assertEquals(2, root.getChildren().size());

        Label label = (Label) root.getChildren().getFirst();
        Button button = (Button) root.getChildren().getLast();
        assertTrue(button.textProperty().isBound());
        label.setText("hello");
        assertEquals(label.getText(), button.getText());
    }

    @Test
    void testComplexExpression() throws Exception {
        FxmlProcessor mainBuilderJavaFile = new FxmlProcessor(PROCESS_FXML.resolve("complex-expression.fxml"),
                                                              RESOURCES_ROOT,
                                                              ROOT_PACKAGE, classLoader
        );
        Fx2jBuilder<Object, AnchorPane> fx2jBuilder = compileAndLoadBuilder(mainBuilderJavaFile);
        fx2jBuilder.build(null, null, new PropertyResourceBundle(
                Objects.requireNonNull(FxmlProcessorTest.class.getResource("/message.properties")).openStream()), null);
        AnchorPane root = fx2jBuilder.getRoot();
        assertEquals(1, root.getChildren().size());

        Button button = (Button) root.getChildren().getFirst();
        assertTrue(button.minWidthProperty().isBound());
        button.setPrefWidth(10);
        button.setPrefHeight(2);
        assertEquals(12, button.getMinWidth());
    }

    @Test
    void testRequiredArgs() throws Exception {
        FxmlProcessor mainBuilderJavaFile = new FxmlProcessor(PROCESS_FXML.resolve("required-args.fxml"),
                                                              RESOURCES_ROOT,
                                                              ROOT_PACKAGE, classLoader
        );
        Fx2jBuilder<Object, BarChart<?, ?>> fx2jBuilder = compileAndLoadBuilder(mainBuilderJavaFile);
        fx2jBuilder.build(null, null, new PropertyResourceBundle(
                Objects.requireNonNull(FxmlProcessorTest.class.getResource("/message.properties")).openStream()), null);
        BarChart<?, ?> root = fx2jBuilder.getRoot();
        assertInstanceOf(CategoryAxis.class, root.getXAxis());
        assertInstanceOf(NumberAxis.class, root.getYAxis());
    }

    @Test
    void testStaticPropertyElementValue() throws Exception {
        FxmlProcessor mainBuilderJavaFile = new FxmlProcessor(PROCESS_FXML.resolve("static-property-child.fxml"),
                                                              RESOURCES_ROOT,
                                                              ROOT_PACKAGE, classLoader
        );
        Fx2jBuilder<Object, GridPane> fx2jBuilder = compileAndLoadBuilder(mainBuilderJavaFile);
        fx2jBuilder.build(null, null, new PropertyResourceBundle(
                Objects.requireNonNull(FxmlProcessorTest.class.getResource("/message.properties")).openStream()), null);
        GridPane root = fx2jBuilder.getRoot();
        Node child = root.getChildren().getFirst();
        assertInstanceOf(VBox.class, child);
        Insets margin = GridPane.getMargin(child);
        assertEquals(5, margin.getTop());
        assertEquals(0, margin.getRight());
        assertEquals(0, margin.getLeft());
        assertEquals(0, margin.getBottom());
    }

}
