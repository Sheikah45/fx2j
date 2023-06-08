package io.github.sheikah45.fx2j.compiler.utils;

import io.github.sheikah45.fx2j.compiler.internal.model.FxmlComponents;
import io.github.sheikah45.fx2j.compiler.internal.model.FxmlNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FXMLUtils {

    public static FxmlComponents readFxml(Path filePath) {
        try {
            Document document = readXmlPlain(filePath);
            return new FxmlComponents(buildFXMLNode(document), extractImports(document),
                                      extractControllerType(document));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Document readXmlPlain(Path filePath) throws ParserConfigurationException, IOException, SAXException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filePath.toFile());
    }

    private static FxmlNode buildFXMLNode(Document document) {
        return populateElement(document.getDocumentElement());
    }

    private static FxmlNode populateElement(Element element) {
        NamedNodeMap attributesNodeMap = element.getAttributes();
        int attrLength = attributesNodeMap.getLength();

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

    private static Set<String> extractImports(Document document) {
        Set<String> imports = new HashSet<>();
        Node startNode = document.getFirstChild();
        while (startNode != null) {
            if ("import".equals(startNode.getNodeName())) {
                imports.add(startNode.getNodeValue());
            }
            startNode = startNode.getNextSibling();
        }

        return Set.copyOf(imports);
    }

    private static String extractControllerType(Document document) {
        Node startNode = document.getFirstChild();
        while (startNode != null) {
            if ("controllerType".equals(startNode.getNodeName())) {
                return startNode.getNodeValue();
            }
            startNode = startNode.getNextSibling();
        }

        return null;
    }
}
