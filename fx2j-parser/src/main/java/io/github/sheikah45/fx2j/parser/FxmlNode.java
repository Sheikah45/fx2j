package io.github.sheikah45.fx2j.parser;

import io.github.sheikah45.fx2j.parser.utils.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public record FxmlNode(String name, String innerText, Map<String, String> attributes, List<FxmlNode> children) {

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("<").append(name);

        if (!attributes.isEmpty()) {
            String attributes = this.attributes.entrySet()
                                               .stream()
                                               .map(entry -> "%s=\"%s\"".formatted(entry.getKey(), entry.getValue()))
                                               .collect(Collectors.joining(" "));
            stringBuilder.append(" ").append(attributes);
        }

        if (StringUtils.isNullOrBlank(innerText) && children.isEmpty()) {
            stringBuilder.append("/>");
            return stringBuilder.toString();
        }

        stringBuilder.append(">\n");

        if (!StringUtils.isNullOrBlank(innerText)) {
            stringBuilder.append("\t").append(innerText).append("\n");
        }

        if (!children.isEmpty()) {
            String children = this.children.stream()
                                           .map(FxmlNode::toString)
                                           .map(childString -> childString.indent(4))
                                           .collect(Collectors.joining("\n"));
            stringBuilder.append(children);
        }

        stringBuilder.append("</").append(name).append(">");

        return stringBuilder.toString();
    }
}
