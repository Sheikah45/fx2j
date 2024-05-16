package io.github.sheikah45.fx2j.parser;

import io.github.sheikah45.fx2j.parser.antlr.BindExpressionLexer;
import io.github.sheikah45.fx2j.parser.antlr.BindExpressionParser;
import io.github.sheikah45.fx2j.parser.antlr.BindExpressionVisitorImpl;
import io.github.sheikah45.fx2j.parser.attribute.ControllerAttribute;
import io.github.sheikah45.fx2j.parser.attribute.EventHandlerAttribute;
import io.github.sheikah45.fx2j.parser.attribute.FxmlAttribute;
import io.github.sheikah45.fx2j.parser.attribute.IdAttribute;
import io.github.sheikah45.fx2j.parser.attribute.InstancePropertyAttribute;
import io.github.sheikah45.fx2j.parser.attribute.NameSpaceAttribute;
import io.github.sheikah45.fx2j.parser.attribute.StaticPropertyAttribute;
import io.github.sheikah45.fx2j.parser.element.ClassInstanceElement;
import io.github.sheikah45.fx2j.parser.element.ConstantElement;
import io.github.sheikah45.fx2j.parser.element.CopyElement;
import io.github.sheikah45.fx2j.parser.element.DeclarationElement;
import io.github.sheikah45.fx2j.parser.element.DefineElement;
import io.github.sheikah45.fx2j.parser.element.FactoryElement;
import io.github.sheikah45.fx2j.parser.element.FxmlElement;
import io.github.sheikah45.fx2j.parser.element.IncludeElement;
import io.github.sheikah45.fx2j.parser.element.InstanceElement;
import io.github.sheikah45.fx2j.parser.element.InstancePropertyElement;
import io.github.sheikah45.fx2j.parser.element.ReferenceElement;
import io.github.sheikah45.fx2j.parser.element.RootElement;
import io.github.sheikah45.fx2j.parser.element.ScriptElement;
import io.github.sheikah45.fx2j.parser.element.ScriptSource;
import io.github.sheikah45.fx2j.parser.element.StaticPropertyElement;
import io.github.sheikah45.fx2j.parser.element.ValueElement;
import io.github.sheikah45.fx2j.parser.property.Value;
import io.github.sheikah45.fx2j.parser.utils.StringUtils;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URI;
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
            Element element = document.getDocumentElement();
            FxmlElement fxmlElement = createFxmlElement(element);
            if (!(fxmlElement instanceof DeclarationElement declarationElement)) {
                throw new ParseException("Root element is not a new declaration of an instance of a class");
            }
            return new FxmlComponents(declarationElement, extractProcessingInstructions(document));
        } catch (Exception e) {
            throw new ParseException("Error parsing document", e);
        }
    }

    private static String retrieveInnerText(Element element) {
        NodeList childNodes = element.getChildNodes();
        int childrenLength = childNodes.getLength();

        return IntStream.range(0, childrenLength)
                        .sorted()
                        .map(index -> childrenLength - index - 1)
                        .mapToObj(childNodes::item)
                        .filter(Text.class::isInstance)
                        .map(Text.class::cast)
                        .map(Text::getTextContent)
                        .filter(text -> !StringUtils.isNullOrBlank(text))
                        .map(text -> text.replaceAll("\\s+", " ").strip())
                        .findFirst()
                        .orElse("");
    }

    private static ClassInstanceElement.Content createContent(Element element) {
        List<FxmlAttribute> attributes = createAttributes(element);
        List<FxmlElement> children = createChildren(element);
        String text = retrieveInnerText(element);
        return new ClassInstanceElement.Content(attributes, children, createPropertyValue(text));
    }

    private static Value createPropertyValue(Element element) {
        List<Value.Single> values = new ArrayList<>();

        createAttributes(element).stream().map(attribute -> {
            if (!(attribute instanceof FxmlAttribute.CommonAttribute commonAttribute)) {
                throw new ParseException("property attribute contains a non common attribute");
            }

            return commonAttribute;
        }).map(io.github.sheikah45.fx2j.parser.property.Value.Attribute::new).forEach(values::add);

        createChildren(element)
                .stream()
                .map(io.github.sheikah45.fx2j.parser.property.Value.Element::new)
                .forEach(values::add);

        Value.Single innerValue = createPropertyValue(retrieveInnerText(element));
        if (!(innerValue instanceof Value.Empty)) {
            values.add(innerValue);
        }

        if (values.isEmpty()) {
            return new Value.Empty();
        }

        if (values.size() == 1) {
            return values.getFirst();
        }

        return new Value.Multi(values);
    }

    private static List<FxmlAttribute> createAttributes(Element element) {
        NamedNodeMap attributesNodeMap = element.getAttributes();
        int attrLength = attributesNodeMap.getLength();
        return IntStream.range(0, attrLength)
                        .mapToObj(attributesNodeMap::item)
                        .filter(Attr.class::isInstance)
                        .map(Attr.class::cast)
                        .map(FxmlParser::createFxmlAttribute)
                        .toList();
    }

    private static List<FxmlElement> createChildren(Element element) {
        NodeList childNodes = element.getChildNodes();
        int childrenLength = childNodes.getLength();

        return IntStream.range(0, childrenLength)
                        .mapToObj(childNodes::item)
                        .filter(Element.class::isInstance)
                        .map(Element.class::cast)
                        .map(FxmlParser::createFxmlElement)
                        .toList();
    }

    private static FxmlElement createFxmlElement(Element element) {
        return switch (element.getTagName()) {
            case "fx:include" -> createIncludeElement(element);
            case "fx:reference" -> createReferenceElement(element);
            case "fx:copy" -> createCopyElement(element);
            case "fx:root" -> createRootElement(element);
            case "fx:define" -> createDefineElement(element);
            case "fx:script" -> createScriptElement(element);
            case String tag when tag.matches("[a-z]\\w*") ->
                    new InstancePropertyElement(tag, createPropertyValue(element));
            case String tag when tag.matches("(\\w*\\.)*[A-Z]\\w*\\.[a-z]\\w*") -> {
                Value value = createPropertyValue(element);
                int separatorIndex = tag.lastIndexOf('.');
                String className = tag.substring(0, separatorIndex);
                String property = tag.substring(separatorIndex + 1);
                yield new StaticPropertyElement(className, property, value);
            }
            default -> createInstanceElement(element);
        };
    }

    private static ClassInstanceElement createInstanceElement(Element element) {
        String className = element.getTagName();
        String factory = removeAndGetValueIfPresent(element, "fx:factory").orElse(null);
        String value = removeAndGetValueIfPresent(element, "fx:value").orElse(null);
        String constant = removeAndGetValueIfPresent(element, "fx:constant").orElse(null);
        if (Stream.of(factory, value, constant).filter(Objects::nonNull).count() > 1) {
            throw new ParseException("Multiple initialization attributes specified: %s".formatted(element));
        }

        ClassInstanceElement.Content content = createContent(element);

        if (factory != null) {
            return new FactoryElement(className, factory, content);
        }

        if (value != null) {
            return new ValueElement(className, createPropertyValue(value), content);
        }

        if (constant != null) {
            return new ConstantElement(className, constant, content);
        }

        return new InstanceElement(className, content);
    }

    private static RootElement createRootElement(Element element) {
        String type = removeAndGetValueIfPresent(element, "type").orElse(Object.class.getCanonicalName());
        return new RootElement(type, createContent(element));
    }

    private static DefineElement createDefineElement(Element element) {
        List<ClassInstanceElement> children = createChildren(element).stream().map(child -> {
            if (!(child instanceof ClassInstanceElement classInstanceElement)) {
                throw new ParseException("define element contains a non class instance element");
            }

            return classInstanceElement;
        }).toList();

        return new DefineElement(children);
    }

    private static ScriptElement createScriptElement(Element element) {
        return removeAndGetValueIfPresent(element, "source").map(Path::of).map(source -> {
            Optional<String> charset = removeAndGetValueIfPresent(element, "charset");
            return new ScriptElement(new ScriptSource.Reference(source, charset.map(Charset::forName).orElse(null)));
        }).orElseGet(() -> new ScriptElement(new ScriptSource.Inline(retrieveInnerText(element))));
    }

    private static CopyElement createCopyElement(Element element) {
        String source = removeAndGetValueIfPresent(element, "source").orElseThrow(
                () -> new ParseException("Source attribute not found in fx:reference element: %s".formatted(element)));
        return new CopyElement(source, createContent(element));
    }

    private static ReferenceElement createReferenceElement(Element element) {
        String source = removeAndGetValueIfPresent(element, "source").orElseThrow(
                () -> new ParseException("Source attribute not found in fx:reference element: %s".formatted(element)));
        return new ReferenceElement(source, createContent(element));
    }

    private static IncludeElement createIncludeElement(Element element) {
        Path source = removeAndGetValueIfPresent(element, "source").map(Path::of)
                                                                   .orElseThrow(() -> new ParseException(
                                                                           "Source attribute not found in fx:include element: %s".formatted(
                                                                                   element)));

        Path resources = removeAndGetValueIfPresent(element, "resources").map(Path::of).orElse(null);
        Charset charset = removeAndGetValueIfPresent(element, "charset").map(Charset::forName)
                                                                        .orElse(StandardCharsets.UTF_8);

        return new IncludeElement(source, resources, charset, createContent(element));
    }

    private static Value.Single createPropertyValue(String value) {
        return switch (value) {
            case String val when val.startsWith("@") -> new Value.Location(Path.of(val.substring(1)));
            case String val when val.startsWith("%") -> new Value.Resource(val.substring(1));
            case String val when val.matches("\\$\\{.*}") -> createExpressionValue(val);
            case String val when val.startsWith("$") -> new Value.Reference(val.substring(1));
            case String val when val.startsWith("\\") -> new Value.Literal(val.substring(1));
            case String val when val.isBlank() -> new Value.Empty();
            case String val -> new Value.Literal(val);
        };
    }

    private static Value.Expression createExpressionValue(String val) {
        CodePointCharStream charStream = CharStreams.fromString(val.substring(2, val.length() - 1));
        BindExpressionLexer expressionLexer = new BindExpressionLexer(charStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(expressionLexer);
        BindExpressionParser expressionParser = new BindExpressionParser(commonTokenStream);
        return expressionParser.expression().accept(new BindExpressionVisitorImpl());
    }

    private static FxmlAttribute createFxmlAttribute(Attr attr) {
        return switch (attr.getName()) {
            case "fx:id" -> new IdAttribute(attr.getValue());
            case "fx:controller" -> new ControllerAttribute(attr.getValue());
            case String name when name.startsWith("xmlns:") ->
                    new NameSpaceAttribute(name.substring(6), URI.create(attr.getValue()));
            case String name when name.startsWith("on") ->
                    new EventHandlerAttribute(name, createEventHandler(attr.getValue()));
            case String name when name.matches("(\\w*\\.)*[A-Z]\\w*\\.[a-z]\\w*") -> {
                int separatorIndex = name.lastIndexOf('.');
                yield new StaticPropertyAttribute(name.substring(0, separatorIndex), name.substring(separatorIndex + 1),
                                                  createPropertyValue(attr.getValue()));
            }
            case String name -> new InstancePropertyAttribute(name, createPropertyValue(attr.getValue()));
        };
    }

    private static Value.Handler createEventHandler(String value) {
        return switch (value) {
            case String val when val.startsWith("#") -> new Value.Method(val.substring(1));
            case String val when val.startsWith("$") -> new Value.Reference(val.substring(1));
            case String val when val.isBlank() -> new Value.Empty();
            case String val -> new Value.Script(val);
        };
    }

    private static List<FxmlProcessingInstruction> extractProcessingInstructions(Document document) {

        NodeList childNodes = document.getChildNodes();
        int childrenLength = childNodes.getLength();

        List<FxmlProcessingInstruction> processingInstructions = new ArrayList<>();
        for (int i = 0; i < childrenLength; i++) {
            Node item = childNodes.item(i);
            if (item instanceof ProcessingInstruction processingInstruction) {
                processingInstructions.add(switch (processingInstruction.getTarget()) {
                    case "import" -> new FxmlProcessingInstruction.Import(processingInstruction.getData());
                    case "language" -> new FxmlProcessingInstruction.Language(processingInstruction.getData());
                    case "compile" -> new FxmlProcessingInstruction.Compile(
                            !"false".equalsIgnoreCase(processingInstruction.getData()));
                    case String target -> new FxmlProcessingInstruction.Custom(target, processingInstruction.getData());
                });
            }
        }

        return processingInstructions;
    }

    private static Optional<String> removeAndGetValueIfPresent(Element element, String name) {
        String value = element.getAttribute(name);
        element.removeAttribute(name);
        return value.isBlank() ? Optional.empty() : Optional.of(value);
    }
}
