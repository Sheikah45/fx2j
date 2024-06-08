package io.github.sheikah45.fx2j.processor.internal.resolve;

import io.github.sheikah45.fx2j.parser.property.Expression;
import io.github.sheikah45.fx2j.parser.property.Value;
import io.github.sheikah45.fx2j.processor.FxmlProcessor;
import io.github.sheikah45.fx2j.processor.internal.code.CodeTypes;
import io.github.sheikah45.fx2j.processor.internal.code.CodeValue;
import io.github.sheikah45.fx2j.processor.internal.code.CodeValues;
import io.github.sheikah45.fx2j.processor.internal.model.NamedArgValue;
import javafx.geometry.VPos;
import javafx.util.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValueResolverTest extends AbstractResolverTest {

    private final ValueResolver valueResolver = resolverContainer.getValueResolver();


    @ParameterizedTest
    @ArgumentsSource(DefaultValueProvider.class)
    void testCoerceDefaultValueBlankPrimitive(Class<?> clazz, Object value) {
        assertEquals(CodeValues.of(value),
                     valueResolver.coerceDefaultValue(new NamedArgValue(clazz, "", "")));
    }

    @ParameterizedTest
    @ArgumentsSource(DefaultValueProvider.class)
    void testCoerceDefaultValueBlankBoxed(Class<?> clazz) {
        assertEquals(CodeValues.nullValue(), valueResolver.coerceDefaultValue(
                new NamedArgValue(resolverContainer.getTypeResolver().wrapType(clazz), "", "")));
    }

    @Test
    void testCoerceDefaultValueNotBlank() {
        assertEquals(CodeValues.literal("hello"),
                     valueResolver.coerceDefaultValue(new NamedArgValue(String.class, "", "hello")));
    }

    @Test
    void testCoerceImpossible() {
        assertThrows(UnsupportedOperationException.class, () -> valueResolver.resolveCodeValue(Class.class, "aa"));
    }


    @Test
    void testCoerceObject() {
        assertEquals(CodeValues.literal("hello"), valueResolver.resolveCodeValue(Object.class, "hello"));
    }

    @Test
    void testCoerceChar() {
        assertEquals(CodeValues.literal('a'), valueResolver.resolveCodeValue(char.class, "a"));
        assertEquals(CodeValues.literal('a'), valueResolver.resolveCodeValue(Character.class, "a"));
        assertThrows(IllegalArgumentException.class, () -> valueResolver.resolveCodeValue(Character.class, "aa"));
        assertThrows(IllegalArgumentException.class, () -> valueResolver.resolveCodeValue(Character.class, ""));
    }

    @Test
    void testCoerceString() {
        assertEquals(CodeValues.literal("hello"), valueResolver.resolveCodeValue(String.class, "hello"));
    }

    @Test
    void testCoerceArray() {
        assertEquals(new CodeValue.Array.Declared(CodeTypes.of(String.class), List.of(CodeValues.literal("hello"),
                                                                                      CodeValues.literal("world"))),
                     valueResolver.resolveCodeValue(String[].class, "hello,world"));
    }

    @Test
    void testParseMethod() {
        assertEquals(CodeValues.literal(100d), valueResolver.resolveCodeValue(double.class, "100"));
        assertEquals(CodeValues.literal(100d), valueResolver.resolveCodeValue(Double.class, "100"));
    }

    @Test
    void testValueOfMethod() {
        assertEquals(CodeValues.methodCall(Duration.class, "valueOf",
                                           CodeValues.literal("1s")),
                     valueResolver.resolveCodeValue(Duration.class, "1s"));
        assertThrows(UnsupportedOperationException.class, () -> valueResolver.resolveCodeValue(Duration.class, ""));
    }

    @Test
    void testStringParsingMethods() throws Exception {
        assertEquals(CodeValues.literal(100d),
                     valueResolver.resolveCodeValue(Double.class.getMethod("parseDouble", String.class), "100"));
        assertEquals(CodeValues.fieldAccess(Double.class, "POSITIVE_INFINITY"),
                     valueResolver.resolveCodeValue(Double.class.getMethod("parseDouble", String.class), "Infinity"));
        assertEquals(CodeValues.fieldAccess(Double.class, "NEGATIVE_INFINITY"),
                     valueResolver.resolveCodeValue(Double.class.getMethod("parseDouble", String.class), "-Infinity"));
        assertEquals(CodeValues.fieldAccess(Double.class, "NaN"),
                     valueResolver.resolveCodeValue(Double.class.getMethod("parseDouble", String.class), "NaN"));
        assertEquals(CodeValues.fieldAccess(Float.class, "POSITIVE_INFINITY"),
                     valueResolver.resolveCodeValue(Float.class.getMethod("parseFloat", String.class), "Infinity"));
        assertEquals(CodeValues.fieldAccess(Float.class, "NEGATIVE_INFINITY"),
                     valueResolver.resolveCodeValue(Float.class.getMethod("parseFloat", String.class), "-Infinity"));
        assertEquals(CodeValues.fieldAccess(Float.class, "NaN"),
                     valueResolver.resolveCodeValue(Float.class.getMethod("parseFloat", String.class), "NaN"));
        assertEquals(CodeValues.enumValue(VPos.BASELINE),
                     valueResolver.resolveCodeValue(VPos.class.getMethod("valueOf", String.class), "BASELINE"));
    }

    @Test
    void testStringParsingMethodFails() {
        assertThrows(InvocationTargetException.class,
                     () -> valueResolver.resolveCodeValue(Double.class.getMethod("parseDouble", String.class), ""));
    }

    @Test
    void testStringParsingMethodNotStatic() {
        assertThrows(IllegalArgumentException.class,
                     () -> valueResolver.resolveCodeValue(Double.class.getMethod("doubleValue"), ""));
    }

    @Test
    void testStringParsingMethodNotString() {
        assertThrows(IllegalArgumentException.class,
                     () -> valueResolver.resolveCodeValue(Character.class.getMethod("valueOf", char.class), ""));
    }

    @Test
    void testStringParsingMethodMultipleParameters() {
        assertThrows(IllegalArgumentException.class,
                     () -> valueResolver.resolveCodeValue(
                             String.class.getMethod("format", String.class, Object[].class), ""));
    }

    @Test
    void testResolveEmptyValue() {
        assertEquals(CodeValues.nullValue(), valueResolver.resolveCodeValue(Object.class, new Value.Empty()));
    }

    @Test
    void testResolveReference() {
        resolverContainer.getNameResolver().storeIdType("a", Double.class);
        assertThrows(IllegalArgumentException.class,
                     () -> valueResolver.resolveCodeValue(Float.class, new Value.Reference("a")));
        assertEquals(CodeValues.variable("a"),
                     valueResolver.resolveCodeValue(Double.class, new Value.Reference("a")));
    }

    @Test
    void testResolveResource() {
        assertThrows(UnsupportedOperationException.class,
                     () -> valueResolver.resolveCodeValue(Float.class, new Value.Resource("a")));
        assertEquals(CodeValues.methodCall(CodeValues.variable(FxmlProcessor.RESOURCES_NAME), "getString",
                                           "a"),
                     valueResolver.resolveCodeValue(String.class, new Value.Resource("a")));
    }

    @Test
    void testResolveLiteral() {
        assertEquals(CodeValues.literal("a"),
                     valueResolver.resolveCodeValue(String.class, new Value.Literal("a")));
    }

    @Test
    void testResolveExpression() {
        assertThrows(UnsupportedOperationException.class,
                     () -> valueResolver.resolveCodeValue(Float.class, new Expression.Null()));
    }

    @Test
    void testResolveLocation() {
        assertThrows(UnsupportedOperationException.class,
                     () -> valueResolver.resolveCodeValue(Float.class, new Value.Location(Path.of(""))));
    }

    private static class DefaultValueProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return ValueResolver.DEFAULTS_MAP.entrySet()
                                             .stream()
                                             .map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
        }
    }

}
