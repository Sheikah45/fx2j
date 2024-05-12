package io.github.sheikah45.fx2j.parser;

import io.github.sheikah45.fx2j.parser.utils.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FxmlParser {

    private static final String FX_ID = "fx:id";
    private static final DocumentBuilder DOCUMENT_BUILDER;

    static {
        try {
            DOCUMENT_BUILDER = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static FxmlComponents readFxml(Path filePath) {
        try {
            Document document = DOCUMENT_BUILDER.parse(filePath.toFile());
            return new FxmlComponents(populateElement(document.getDocumentElement()),
                                      extractProcessingInstructions(document));
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static FxmlNode populateElement(Element element) {
        NamedNodeMap attributesNodeMap = element.getAttributes();
        FxmlElement fxmlElement = createFxmlElement(element);

        int attrLength = attributesNodeMap.getLength();

        List<FxmlAttribute> attributes = IntStream.range(0, attrLength)
                                                  .mapToObj(attributesNodeMap::item)
                                                  .map(FxmlParser::createFxmlAttribute)
                                                  .toList();

        if (!element.hasChildNodes()) {
            return new FxmlNode(fxmlElement, attributes, "", List.of());
        }

        NodeList childNodes = element.getChildNodes();
        int childrenLength = childNodes.getLength();
        List<FxmlNode> children = new ArrayList<>();
        String innerText = "";

        for (int i = 0; i < childrenLength; i++) {
            Node item = childNodes.item(i);
            if (item instanceof Text text && !StringUtils.isNullOrBlank(text.getTextContent())) {
                innerText = text.getTextContent().replaceAll("\\s+", " ").strip();
            } else if (item instanceof Element childElement) {
                children.add(populateElement(childElement));
            }
        }

        return new FxmlNode(fxmlElement, attributes, innerText, children);
    }

    private static FxmlElement createFxmlElement(Element element) {
        return switch (element.getTagName()) {
            case "fx:include" -> createIncludeElement(element);
            case "fx:reference" -> createReferenceElement(element);
            case "fx:copy" -> createCopyElement(element);
            case "fx:root" -> createRootElement(element);
            case "fx:define" -> new FxmlElement.Define();
            case "fx:script" -> new FxmlElement.Script();
            case String tag when tag.matches("[a-z]\\w*") -> new FxmlElement.Property(tag);
            case String tag when tag.matches("(\\w*\\.)*[A-Z]\\w*\\.[a-z]\\w*") -> {
                int separatorIndex = tag.lastIndexOf('.');
                yield new FxmlElement.StaticProperty(tag.substring(0, separatorIndex),
                                                     tag.substring(separatorIndex + 1));
            }
            case String ignored -> createInstanceElement(element);
        };
    }

    private static FxmlElement.Instance createInstanceElement(Element element) {
        String className = element.getTagName();
        String id = removeAndGetValueIfPresent(element, FX_ID).orElse(null);

        String factory = removeAndGetValueIfPresent(element, "fx:factory").orElse(null);
        String value = removeAndGetValueIfPresent(element, "fx:value").orElse(null);
        String constant = removeAndGetValueIfPresent(element, "fx:constant").orElse(null);
        if (Stream.of(factory, value, constant).filter(Objects::nonNull).count() > 1) {
            throw new IllegalStateException("Multiple initialization attributes specified: %s".formatted(element));
        }

        if (factory != null) {
            return new FxmlElement.Instance.Factory(id, className, factory);
        }

        if (value != null) {
            return new FxmlElement.Instance.Value(id, className, value);
        }

        if (constant != null) {
            return new FxmlElement.Instance.Constant(id, className, constant);
        }

        return new FxmlElement.Instance.Simple(id, className);
    }

    private static FxmlElement.Root createRootElement(Element element) {
        String type = removeAndGetValueIfPresent(element, "type").orElse(null);
        String id = removeAndGetValueIfPresent(element, FX_ID).orElse(null);

        return new FxmlElement.Root(id, type);
    }

    private static FxmlElement.Copy createCopyElement(Element element) {
        String source = removeAndGetValueIfPresent(element, "source").orElseThrow(
                () -> new IllegalStateException(
                        "Source attribute not found in fx:reference element: %s".formatted(element)));
        String id = removeAndGetValueIfPresent(element, FX_ID).orElse(null);

        return new FxmlElement.Copy(id, source);
    }

    private static FxmlElement.Reference createReferenceElement(Element element) {
        String source = removeAndGetValueIfPresent(element, "source").orElseThrow(
                () -> new IllegalStateException(
                        "Source attribute not found in fx:reference element: %s".formatted(element)));
        String id = removeAndGetValueIfPresent(element, FX_ID).orElse(null);

        return new FxmlElement.Reference(id, source);
    }

    private static FxmlElement.Include createIncludeElement(Element element) {
        Path source = removeAndGetValueIfPresent(element, "source").map(Path::of)
                                                                   .orElseThrow(
                                                                           () -> new IllegalStateException(
                                                                                   "Source attribute not found in fx:include element: %s".formatted(
                                                                                           element)));

        Path resources = removeAndGetValueIfPresent(element, "resources").map(Path::of).orElse(null);
        Charset charset = removeAndGetValueIfPresent(element, "charset").map(Charset::forName)
                                                                        .orElse(null);
        String id = removeAndGetValueIfPresent(element, FX_ID).orElse(null);

        return new FxmlElement.Include(id, source, resources, charset);
    }

    private static FxmlAttribute createFxmlAttribute(Node node) {
        return switch (node.getNodeName()) {
            case "fx:controller" -> new FxmlAttribute.Controller(node.getNodeValue());
            case String name when name.startsWith("on") ->
                    new FxmlAttribute.EventHandler(name, createHandlerValue(node.getNodeValue()));
            case String name when name.matches("(\\w*\\.)*[A-Z]\\w*\\.[a-z]\\w*") -> {
                int separatorIndex = name.lastIndexOf('.');
                yield new FxmlAttribute.Property.Static(name.substring(0, separatorIndex),
                                                        name.substring(separatorIndex + 1),
                                                        createPropertyValue(node.getNodeValue())
                );
            }
            case String name -> new FxmlAttribute.Property.Instance(name, createPropertyValue(node.getNodeValue()));
        };
    }

    private static FxmlAttribute.Property.Value createPropertyValue(String value) {
        return switch (value) {
            case String val when val.startsWith("@") -> new FxmlAttribute.Property.Location(Path.of(val.substring(1)));
            case String val when val.startsWith("%") -> new FxmlAttribute.Property.Resource(val.substring(1));
            case String val when val.matches("\\$\\{.*}") ->
                    new FxmlAttribute.Property.BindExpression(val.substring(2, val.length() - 1));
            case String val when val.startsWith("$") -> new FxmlAttribute.Property.Reference(val.substring(1));
            case String val when val.startsWith("\\") -> new FxmlAttribute.Property.Literal(val.substring(1));
            case String val -> new FxmlAttribute.Property.Literal(val);
        };
    }

    private static FxmlAttribute.EventHandler.Value createHandlerValue(String value) {
        return switch (value) {
            case String val when val.startsWith("#") -> new FxmlAttribute.EventHandler.Method(val.substring(1));
            case String val when val.startsWith("$") -> new FxmlAttribute.EventHandler.Reference(val.substring(1));
            case String val -> new FxmlAttribute.EventHandler.Script(val);
        };
    }

    private static Map<String, Set<String>> extractProcessingInstructions(Document document) {
        Map<String, Set<String>> processingInstructions = new HashMap<>();
        Node node = document.getFirstChild();
        while (node != null) {
            if (node instanceof ProcessingInstruction processingInstruction) {
                processingInstructions.computeIfAbsent(processingInstruction.getTarget(), key -> new HashSet<>())
                                      .add(processingInstruction.getData());
            }
            node = node.getNextSibling();
        }

        return processingInstructions;
    }

    private static Optional<String> removeAndGetValueIfPresent(Element element, String name) {
        String value = element.getAttribute(name);
        element.removeAttribute(name);
        return value.isBlank() ? Optional.empty() : Optional.of(value);
    }

    public static void main(String[] args) {
        FxmlComponents components = FxmlParser.readFxml(
                Path.of("C:\\Users\\corey\\FAFProjects\\fx2j\\fx2j-processor\\src\\test\\resources\\fxml\\controller\\change-handler-controller.fxml"));
        System.out.println(components);
    }
}
