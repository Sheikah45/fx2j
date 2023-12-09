package io.github.sheikah45.fx2j.parser;

import io.github.sheikah45.fx2j.parser.utils.StringUtils;
import org.w3c.dom.Attr;
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FxmlParser {

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
            return new FxmlComponents(extractFxmlNode(document), extractProcessingInstructions(document));
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static FxmlNode extractFxmlNode(Document document) {
        return populateElement(document.getDocumentElement());
    }

    private static FxmlNode populateElement(Element element) {
        NamedNodeMap attributesNodeMap = element.getAttributes();
        int attrLength = attributesNodeMap.getLength();
        for (int i = 0; i < attrLength; i++) {
            Attr attribute = (Attr) attributesNodeMap.item(i);
            attribute.getName();
        }

        Map<String, String> attributes = IntStream.range(0, attrLength)
                                                  .mapToObj(attributesNodeMap::item)
                                                  .collect(Collectors.toMap(Node::getNodeName, Node::getNodeValue));

        if (!element.hasChildNodes()) {
            return new FxmlNode(element.getTagName(), null, attributes, List.of());
        }
        NodeList childNodes = element.getChildNodes();
        int childrenLength = childNodes.getLength();
        List<FxmlNode> children = new ArrayList<>();
        String innerText = null;

        for (int i = 0; i < childrenLength; i++) {
            Node item = childNodes.item(i);
            if (item instanceof Text text && !StringUtils.isNullOrBlank(text.getTextContent())) {
                innerText = text.getTextContent().trim().replaceAll("\\s+", " ");
            } else if (item instanceof Element childElement) {
                children.add(populateElement(childElement));
            }
        }

        return new FxmlNode(element.getTagName(), innerText, attributes, children);
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

        return Map.copyOf(processingInstructions);
    }

    public static void main(String[] args) {
        FxmlParser.readFxml(
                Path.of("C:\\Users\\corey\\FAFProjects\\fx2j\\fx2j-processor\\src\\test\\resources\\fxml\\controller\\change-handler-controller.fxml"));
    }
}
