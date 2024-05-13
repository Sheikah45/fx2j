package io.github.sheikah45.fx2j.processor.internal.utils;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

public class StringUtils {

    public static String replaceLast(String string, String target, String replacement) {
        return substringBeforeLast(string, target) + replacement + substringAfterLast(string, target);
    }

    public static String substringAfterLast(String string, String delimiter) {
        int index = string.lastIndexOf(delimiter);
        if (index == -1) {
            return string;
        }
        return string.substring(index + 1);
    }

    public static String camelCase(String text) {
        String startString = text.substring(0, 1).toLowerCase();
        String endString = text.substring(1);
        return startString + endString;
    }

    public static String fxmlFileToBuilderClass(Path filePath) {
        String className = filePath.getFileName().toString().replace(".fxml", "");
        return StringUtils.delimitedToCapitalize(className) + "Builder";
    }

    public static String substringBeforeLast(String string, String delimiter) {
        int index = string.lastIndexOf(delimiter);
        if (index == -1) {
            return string;
        }
        return string.substring(0, index);
    }

    public static String delimitedToCapitalize(String text) {
        String[] textParts = text.split("[_-]");
        StringBuilder stringBuilder = new StringBuilder();
        for (String snakeTextPart : textParts) {
            stringBuilder.append(StringUtils.capitalize(snakeTextPart));
        }
        return stringBuilder.toString();
    }

    public static String capitalize(String text) {
        String startString = text.substring(0, 1).toUpperCase();
        String endString = text.substring(1);
        return startString + endString;
    }

    public static String fxmlFileToPackageName(Path filePath) {
        return filePath.getParent().toString().replace(File.separatorChar, '.').toLowerCase(Locale.ROOT);
    }
}
