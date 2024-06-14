package io.github.sheikah45.fx2j.parser.internal.utils;

import io.github.sheikah45.fx2j.parser.attribute.AssignableAttribute;
import io.github.sheikah45.fx2j.parser.attribute.ControllerAttribute;
import io.github.sheikah45.fx2j.parser.attribute.DefaultNameSpaceAttribute;
import io.github.sheikah45.fx2j.parser.attribute.EventHandlerAttribute;
import io.github.sheikah45.fx2j.parser.attribute.FxmlAttribute;
import io.github.sheikah45.fx2j.parser.attribute.IdAttribute;
import io.github.sheikah45.fx2j.parser.attribute.InstancePropertyAttribute;
import io.github.sheikah45.fx2j.parser.attribute.NameSpaceAttribute;
import io.github.sheikah45.fx2j.parser.attribute.StaticPropertyAttribute;
import io.github.sheikah45.fx2j.parser.element.AssignableElement;
import io.github.sheikah45.fx2j.parser.element.ClassInstanceElement;
import io.github.sheikah45.fx2j.parser.element.ConstantElement;
import io.github.sheikah45.fx2j.parser.element.CopyElement;
import io.github.sheikah45.fx2j.parser.element.DefineElement;
import io.github.sheikah45.fx2j.parser.element.ElementContent;
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
import io.github.sheikah45.fx2j.parser.property.BindExpression;
import io.github.sheikah45.fx2j.parser.property.Handler;
import io.github.sheikah45.fx2j.parser.property.Value;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FxmlFormatUtils {
    private static String toAttributeString(String attributeName, String attributeValue) {
        return attributeName + "=\"" + attributeValue + "\"";
    }

    public static String toAttributeString(FxmlAttribute attribute) {
        return switch (attribute) {
            case IdAttribute(String value) -> toAttributeString("fx:id", value);
            case ControllerAttribute(String className) -> toAttributeString("fx:controller", className);
            case NameSpaceAttribute(String namespace, URI location) ->
                    toAttributeString("xmlns:" + namespace, location.toString());
            case DefaultNameSpaceAttribute(URI location) -> toAttributeString("xmlns", location.toString());
            case StaticPropertyAttribute(String className, String property, Value value) ->
                    toAttributeString(className + "." + property, toValueString(value));
            case InstancePropertyAttribute(String property, Value value) ->
                    toAttributeString(property, toValueString(value));
            case EventHandlerAttribute(String eventName, Handler handler) ->
                    toAttributeString(eventName, toHandlerString(handler));
        };
    }

    public static String toElementString(FxmlElement element) {
        return switch (element) {
            case DefineElement(List<ClassInstanceElement> elements) -> {
                StringBuilder fxmlBuilder = new StringBuilder();
                fxmlBuilder.append("<fx:define");
                if (elements.isEmpty()) {
                    fxmlBuilder.append("/>");
                } else {
                    fxmlBuilder.append(">\n");
                    fxmlBuilder.append(FxmlFormatUtils.toElementsString(elements));
                    fxmlBuilder.append("</fx:define>");
                }
                yield fxmlBuilder.toString();
            }
            case ScriptElement(ScriptSource.Inline(String value, Charset charset)) ->
                    "<fx:script charset=\"" + charset.name() + "\">\n" + value.indent(2) + "</fx:script>";
            case ScriptElement(ScriptSource.Reference(Path source, Charset charset)) -> "<fx:script " +
                                                                                        FxmlFormatUtils.toAttributeString(
                                                                                                "charset",
                                                                                                charset.name()) +
                                                                                        " " +
                                                                                        FxmlFormatUtils.toAttributeString(
                                                                                                "source",
                                                                                                source.toString()) +
                                                                                        "/>";
            case StaticPropertyElement(
                    String className, String property, ElementContent<AssignableAttribute, AssignableElement> content
            ) -> FxmlFormatUtils.toFxmlElementString(className + "." + property, content);
            case InstancePropertyElement(
                    String property, ElementContent<AssignableAttribute, AssignableElement> content
            ) -> FxmlFormatUtils.toFxmlElementString(property, content);
            case ReferenceElement(String source, ElementContent<?, ?> content) ->
                    FxmlFormatUtils.toFxmlElementString("fx:reference", content, "source", source);
            case IncludeElement(Path source, Path resources, Charset charset, ElementContent<?, ?> content) ->
                    FxmlFormatUtils.toFxmlElementString("fx:include", content, "source", source.toString(), "charset",
                                                        charset.name());
            case CopyElement(String source, ElementContent<?, ?> content) ->
                    FxmlFormatUtils.toFxmlElementString("fx:copy", content, "source", source);
            case ValueElement(String className, String value, ElementContent<?, ?> content) ->
                    FxmlFormatUtils.toFxmlElementString(className, content, "fx:value", value);
            case RootElement(String type, ElementContent<?, ?> content) ->
                    FxmlFormatUtils.toFxmlElementString("fx:root", content, "type", type);
            case FactoryElement(String factoryClassName, String factoryMethod, ElementContent<?, ?> content) ->
                    FxmlFormatUtils.toFxmlElementString(factoryClassName, content, "fx:factory", factoryMethod);
            case ConstantElement(String className, String member, ElementContent<?, ?> content) ->
                    FxmlFormatUtils.toFxmlElementString(className, content, "fx:constant", member);
            case InstanceElement(String className, ElementContent<?, ?> content) ->
                    FxmlFormatUtils.toFxmlElementString(className, content);
        };
    }

    private static String toElementsString(List<? extends FxmlElement> elements) {
        return elements.stream().map(FxmlElement::toFxml).map(fxml -> fxml.indent(2)).collect(Collectors.joining("\n"));
    }

    private static String toAttributesString(List<? extends FxmlAttribute> attributes) {
        return attributes.stream().map(FxmlFormatUtils::toAttributeString).collect(Collectors.joining(" "));
    }

    private static String toFxmlElementString(String elementName, ElementContent<?, ?> elementContent,
                                              String... additionalAttributes) {
        StringBuilder fxmlBuilder = new StringBuilder();
        fxmlBuilder.append("<").append(elementName);
        for (int i = 0; i < additionalAttributes.length; i += 2) {
            fxmlBuilder.append(" ").append(toAttributeString(additionalAttributes[i], additionalAttributes[i + 1]));
        }
        fxmlBuilder.append(" ").append(FxmlFormatUtils.toAttributesString(elementContent.attributes()));
        if (elementContent.elements().isEmpty() && elementContent.value() instanceof Value.Empty) {
            fxmlBuilder.append("/>");
        } else {
            fxmlBuilder.append(">\n");
            fxmlBuilder.append(FxmlFormatUtils.toElementsString(elementContent.elements()));
            fxmlBuilder.append(toValueString(elementContent.value())).append("\n").append("</fx:root>");
        }
        return fxmlBuilder.toString();
    }

    private static String toHandlerString(Handler handler) {
        return switch (handler) {
            case Handler.Empty() -> "";
            case Handler.Method(String name) -> "#" + name;
            case Handler.Reference(String name) -> "$" + name;
            case Handler.Script(String script) -> script;
        };
    }

    private static String toValueString(Value value) {
        return switch (value) {
            case Value.Empty() -> "";
            case Value.Literal(String val) -> val;
            case Value.Location(Path val) -> "@" + val.toString();
            case Value.Resource(String val) -> "%" + val;
            case Value.Reference(String val) -> "$" + val;
            case BindExpression bindExpression -> toExpressionString(bindExpression);
        };
    }

    private static String toExpressionString(BindExpression value) {
        return switch (value) {
            case BindExpression.Null() -> "null";
            case BindExpression.Boolean(boolean val) -> Boolean.toString(val);
            case BindExpression.Fraction(double val) -> Double.toString(val);
            case BindExpression.Whole(long val) -> Long.toString(val);
            case BindExpression.String(String val) -> "\"" + val + "\"";
            case BindExpression.Variable(String val) -> val;
            case BindExpression.Or(BindExpression left, BindExpression right) ->
                    toOperatorExpressionString(left, right, "||");
            case BindExpression.And(BindExpression left, BindExpression right) ->
                    toOperatorExpressionString(left, right, "&&");
            case BindExpression.NotEqual(BindExpression left, BindExpression right) ->
                    toOperatorExpressionString(left, right, "!=");
            case BindExpression.Equal(BindExpression left, BindExpression right) ->
                    toOperatorExpressionString(left, right, "==");
            case BindExpression.LessThanEqual(BindExpression left, BindExpression right) ->
                    toOperatorExpressionString(left, right, "<=");
            case BindExpression.LessThan(BindExpression left, BindExpression right) ->
                    toOperatorExpressionString(left, right, "<");
            case BindExpression.GreaterThanEqual(BindExpression left, BindExpression right) ->
                    toOperatorExpressionString(left, right, ">=");
            case BindExpression.GreaterThan(BindExpression left, BindExpression right) ->
                    toOperatorExpressionString(left, right, ">");
            case BindExpression.Subtract(BindExpression left, BindExpression right) ->
                    toOperatorExpressionString(left, right, "-");
            case BindExpression.Add(BindExpression left, BindExpression right) ->
                    toOperatorExpressionString(left, right, "+");
            case BindExpression.Modulo(BindExpression left, BindExpression right) ->
                    toOperatorExpressionString(left, right, "%");
            case BindExpression.Divide(BindExpression left, BindExpression right) ->
                    toOperatorExpressionString(left, right, "/");
            case BindExpression.Multiply(BindExpression left, BindExpression right) ->
                    toOperatorExpressionString(left, right, "*");
            case BindExpression.Negate(BindExpression bindExpression) -> "-" + toExpressionString(bindExpression);
            case BindExpression.Invert(BindExpression bindExpression) -> "!" + toExpressionString(bindExpression);
            case BindExpression.CollectionAccess(BindExpression bindExpression, BindExpression key) ->
                    toExpressionString(bindExpression) + "[" + toExpressionString(key) + "]";
            case BindExpression.MethodCall(
                    BindExpression bindExpression, String methodName, List<BindExpression> args
            ) -> toExpressionString(bindExpression) +
                 "." +
                 methodName +
                 "(" +
                 args.stream().map(FxmlFormatUtils::toExpressionString).collect(Collectors.joining(", ")) +
                 ")";
            case BindExpression.PropertyRead(BindExpression bindExpression, String property) ->
                    toExpressionString(bindExpression) + "." + property;
        };
    }

    private static String toOperatorExpressionString(BindExpression left, BindExpression right, String operator) {
        return "(" + toExpressionString(left) + " " + operator + " " + toExpressionString(right) + ")";
    }
}
