package io.github.sheikah45.fx2j.parser;

import io.github.sheikah45.fx2j.parser.utils.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FxmlParser {

    public static FxmlComponents readFxml(Path filePath) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newDefaultInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(filePath.toFile());
            return new FxmlComponents(populateElement(document.getDocumentElement()),
                                      extractProcessingInstructions(document));
        } catch (Exception e) {
            throw new ParseException("Error parsing document", e);
        }
    }

    private static FxmlNode populateElement(Element element) {
        NamedNodeMap attributesNodeMap = element.getAttributes();
        FxmlElement fxmlElement = createFxmlElement(element);

        int attrLength = attributesNodeMap.getLength();

        List<FxmlAttribute> attributes = IntStream.range(0, attrLength)
                                                  .mapToObj(attributesNodeMap::item)
                                                  .filter(item -> !item.getNodeName().startsWith("xmlns"))
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
            case String tag when tag.matches("[a-z]\\w*") -> new FxmlElement.Property.Instance(tag);
            case String tag when tag.matches("(\\w*\\.)*[A-Z]\\w*\\.[a-z]\\w*") -> {
                int separatorIndex = tag.lastIndexOf('.');
                yield new FxmlElement.Property.Static(tag.substring(0, separatorIndex),
                                                     tag.substring(separatorIndex + 1));
            }
            default -> createInstanceElement(element);
        };
    }

    private static FxmlElement.Declaration createInstanceElement(Element element) {
        String className = element.getTagName();
        String factory = removeAndGetValueIfPresent(element, "fx:factory").orElse(null);
        String value = removeAndGetValueIfPresent(element, "fx:value").orElse(null);
        String constant = removeAndGetValueIfPresent(element, "fx:constant").orElse(null);
        if (Stream.of(factory, value, constant).filter(Objects::nonNull).count() > 1) {
            throw new ParseException("Multiple initialization attributes specified: %s".formatted(element));
        }

        if (factory != null) {
            return new FxmlElement.Declaration.Factory(className, factory);
        }

        if (value != null) {
            return new FxmlElement.Declaration.Value(className, value);
        }

        if (constant != null) {
            return new FxmlElement.Declaration.Constant(className, constant);
        }

        return new FxmlElement.Declaration.Class(className);
    }

    private static FxmlElement.Root createRootElement(Element element) {
        String type = removeAndGetValueIfPresent(element, "type").orElse(Object.class.getCanonicalName());
        return new FxmlElement.Root(type);
    }

    private static FxmlElement.Copy createCopyElement(Element element) {
        String source = removeAndGetValueIfPresent(element, "source").orElseThrow(
                () -> new ParseException(
                        "Source attribute not found in fx:reference element: %s".formatted(element)));
        return new FxmlElement.Copy(source);
    }

    private static FxmlElement.Reference createReferenceElement(Element element) {
        String source = removeAndGetValueIfPresent(element, "source").orElseThrow(
                () -> new ParseException(
                        "Source attribute not found in fx:reference element: %s".formatted(element)));
        return new FxmlElement.Reference(source);
    }

    private static FxmlElement.Include createIncludeElement(Element element) {
        Path source = removeAndGetValueIfPresent(element, "source").map(Path::of)
                                                                   .orElseThrow(
                                                                           () -> new ParseException(
                                                                                   "Source attribute not found in fx:include element: %s".formatted(
                                                                                           element)));

        Path resources = removeAndGetValueIfPresent(element, "resources").map(Path::of).orElse(null);
        Charset charset = removeAndGetValueIfPresent(element, "charset").map(Charset::forName)
                                                                        .orElse(StandardCharsets.UTF_8);
        return new FxmlElement.Include(source, resources, charset);
    }

    private static FxmlAttribute createFxmlAttribute(Node node) {
        return switch (node.getNodeName()) {
            case "fx:id" -> new FxmlAttribute.Id(node.getNodeValue());
            case "fx:controller" -> new FxmlAttribute.Controller(node.getNodeValue());
            case String name when name.startsWith("on") ->
                    new FxmlAttribute.EventHandler(name, createEventHandlerValue(node.getNodeValue()));
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
                    new FxmlAttribute.Property.Expression(val.substring(2, val.length() - 1));
            case String val when val.startsWith("$") -> new FxmlAttribute.Property.Reference(val.substring(1));
            case String val when val.startsWith("\\") -> new FxmlAttribute.Property.Literal(val.substring(1));
            case String val when val.isBlank() -> new FxmlAttribute.Property.Empty();
            case String val -> new FxmlAttribute.Property.Literal(val);
        };
    }

    private static FxmlAttribute.EventHandler.Value createEventHandlerValue(String value) {
        return switch (value) {
            case String val when val.startsWith("#") -> new FxmlAttribute.EventHandler.Method(val.substring(1));
            case String val when val.startsWith("$") -> new FxmlAttribute.EventHandler.Reference(val.substring(1));
            case String val -> new FxmlAttribute.EventHandler.Script(val);
        };
    }

    private static List<FxmlProcessingInstruction> extractProcessingInstructions(Document document) {
        List<FxmlProcessingInstruction> processingInstructions = new ArrayList<>();
        Node node = document.getFirstChild();
        while (node != null) {
            if (node instanceof ProcessingInstruction processingInstruction) {
                processingInstructions.add(switch (processingInstruction.getTarget()) {
                    case "import" -> new FxmlProcessingInstruction.Import(processingInstruction.getData());
                    case "language" -> new FxmlProcessingInstruction.Language(processingInstruction.getData());
                    case "compile" -> new FxmlProcessingInstruction.Compile(
                            !"false".equalsIgnoreCase(processingInstruction.getData()));
                    case String target -> new FxmlProcessingInstruction.Custom(target, processingInstruction.getData());
                });
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
}
