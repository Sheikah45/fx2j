package io.github.sheikah45.fx2j.processor.utils;

import io.github.sheikah45.fx2j.processor.internal.utils.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
class StringUtilsTest {
    @Test
    void testSubstringBeforeLastWithDot() {
        assertEquals("this.is.a", StringUtils.substringBeforeLast("this.is.a.test", "."));
    }

    @Test
    void testSubstringBeforeLastWithNoDelimiter() {
        assertEquals("this.is.a.test", StringUtils.substringBeforeLast("this.is.a.test", "-"));
    }

    @Test
    void testSubstringAfterLast() {
        assertEquals("string", StringUtils.substringAfterLast("this.is.a.test.string", "."));
    }

    @Test
    void testSubstringAfterLastWithNoDelimiter() {
        assertEquals("this.is.a.test", StringUtils.substringAfterLast("this.is.a.test", "-"));
    }

    @Test
    void testCapitalizeLowerCase() {
        assertEquals("HelloWorld", StringUtils.capitalize("helloWorld"));
    }

    @Test
    public void testCapitalizeAlreadyCapitalized() {
        assertEquals("HelloWorld", StringUtils.capitalize("HelloWorld"));
    }

    @Test
    void testCamelCaseUpperCase() {
        assertEquals("helloWorld", StringUtils.camelCase("HelloWorld"));
    }

    @Test
    void testCamelCaseAlreadyLowerCase() {
        assertEquals("helloWorld", StringUtils.camelCase("helloWorld"));
    }

    @Test
    void testDelimitedToCapitalizeUnderscore() {
        assertEquals("HelloWorld", StringUtils.delimitedToCapitalize("hello_world"));
    }

    @Test
    void testDelimitedToCapitalizeDash() {
        assertEquals("JavaIsCool", StringUtils.delimitedToCapitalize("java-is-cool"));
    }

    @Test
    void testDelimitedToCapitalizeMixed() {
        assertEquals("JavaIsCool", StringUtils.delimitedToCapitalize("java_is-cool"));
    }

    @Test
    void testFxmlFileToBuilderClass() {
        Path filePath = Paths.get("src/main/resources/fxmlTestFile.fxml");
        assertEquals("FxmlTestFileBuilder", StringUtils.fxmlFileToBuilderClass(filePath));
    }

    @Test
    void testFxmlFileToPackageName() {
        Path filePath = Paths.get("myapp/test/fxml/test.fxml");
        assertEquals("myapp.test.fxml", StringUtils.fxmlFileToPackageName(filePath));
    }
}
