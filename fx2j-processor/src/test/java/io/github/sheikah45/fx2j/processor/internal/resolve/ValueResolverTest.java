package io.github.sheikah45.fx2j.processor.internal.resolve;

import io.github.sheikah45.fx2j.parser.property.Expression;
import io.github.sheikah45.fx2j.parser.property.Value;
import io.github.sheikah45.fx2j.processor.FxmlProcessor;
import io.github.sheikah45.fx2j.processor.internal.model.CodeValue;
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
        assertEquals(new CodeValue.Literal(value.toString()),
                     valueResolver.coerceDefaultValue(new NamedArgValue(clazz, "", "")));
    }

    @ParameterizedTest
    @ArgumentsSource(DefaultValueProvider.class)
    void testCoerceDefaultValueBlankBoxed(Class<?> clazz) {
        assertEquals(new CodeValue.Null(), valueResolver.coerceDefaultValue(
                new NamedArgValue(resolverContainer.getTypeResolver().wrapType(clazz), "", "")));
    }

    @Test
    void testCoerceDefaultValueNotBlank() {
        assertEquals(new CodeValue.String("hello"),
                     valueResolver.coerceDefaultValue(new NamedArgValue(String.class, "", "hello")));
    }

    @Test
    void testCoerceImpossible() {
        assertThrows(UnsupportedOperationException.class, () -> valueResolver.resolveCodeValue(Class.class, "aa"));
    }


    @Test
    void testCoerceObject() {
        assertEquals(new CodeValue.String("hello"), valueResolver.resolveCodeValue(Object.class, "hello"));
    }

    @Test
    void testCoerceChar() {
        assertEquals(new CodeValue.Char('a'), valueResolver.resolveCodeValue(char.class, "a"));
        assertEquals(new CodeValue.Char('a'), valueResolver.resolveCodeValue(Character.class, "a"));
        assertThrows(IllegalArgumentException.class, () -> valueResolver.resolveCodeValue(Character.class, "aa"));
        assertThrows(IllegalArgumentException.class, () -> valueResolver.resolveCodeValue(Character.class, ""));
    }

    @Test
    void testCoerceString() {
        assertEquals(new CodeValue.String("hello"), valueResolver.resolveCodeValue(String.class, "hello"));
    }

    @Test
    void testCoerceArray() {
        assertEquals(new CodeValue.ArrayInitialization.Declared(String.class, List.of(new CodeValue.String("hello"),
                                                                                      new CodeValue.String("world"))),
                     valueResolver.resolveCodeValue(String[].class, "hello,world"));
    }

    @Test
    void testParseMethod() {
        assertEquals(new CodeValue.Literal("100.0"), valueResolver.resolveCodeValue(double.class, "100"));
        assertEquals(new CodeValue.Literal("100.0"), valueResolver.resolveCodeValue(Double.class, "100"));
    }

    @Test
    void testValueOfMethod() {
        assertEquals(new CodeValue.MethodCall(new CodeValue.Type(Duration.class), "valueOf",
                                              List.of(new CodeValue.String("1s"))),
                     valueResolver.resolveCodeValue(Duration.class, "1s"));
        assertThrows(UnsupportedOperationException.class, () -> valueResolver.resolveCodeValue(Duration.class, ""));
    }

    @Test
    void testStringParsingMethods() throws Exception {
        assertEquals(new CodeValue.Literal("100.0"),
                     valueResolver.resolveCodeValue(Double.class.getMethod("parseDouble", String.class), "100"));
        assertEquals(new CodeValue.FieldAccess(new CodeValue.Type(Double.class), "POSITIVE_INFINITY"),
                     valueResolver.resolveCodeValue(Double.class.getMethod("parseDouble", String.class), "Infinity"));
        assertEquals(new CodeValue.FieldAccess(new CodeValue.Type(Double.class), "NEGATIVE_INFINITY"),
                     valueResolver.resolveCodeValue(Double.class.getMethod("parseDouble", String.class), "-Infinity"));
        assertEquals(new CodeValue.FieldAccess(new CodeValue.Type(Double.class), "NaN"),
                     valueResolver.resolveCodeValue(Double.class.getMethod("parseDouble", String.class), "NaN"));
        assertEquals(new CodeValue.FieldAccess(new CodeValue.Type(Float.class), "POSITIVE_INFINITY"),
                     valueResolver.resolveCodeValue(Float.class.getMethod("parseFloat", String.class), "Infinity"));
        assertEquals(new CodeValue.FieldAccess(new CodeValue.Type(Float.class), "NEGATIVE_INFINITY"),
                     valueResolver.resolveCodeValue(Float.class.getMethod("parseFloat", String.class), "-Infinity"));
        assertEquals(new CodeValue.FieldAccess(new CodeValue.Type(Float.class), "NaN"),
                     valueResolver.resolveCodeValue(Float.class.getMethod("parseFloat", String.class), "NaN"));
        assertEquals(new CodeValue.Enum(VPos.BASELINE),
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
        assertEquals(new CodeValue.Null(), valueResolver.resolveCodeValue(Object.class, new Value.Empty()));
    }

    @Test
    void testResolveReference() {
        resolverContainer.getNameResolver().storeIdType("a", Double.class);
        assertThrows(IllegalArgumentException.class,
                     () -> valueResolver.resolveCodeValue(Float.class, new Value.Reference("a")));
        assertEquals(new CodeValue.Literal("a"),
                     valueResolver.resolveCodeValue(Double.class, new Value.Reference("a")));
    }

    @Test
    void testResolveResource() {
        assertThrows(UnsupportedOperationException.class,
                     () -> valueResolver.resolveCodeValue(Float.class, new Value.Resource("a")));
        assertEquals(new CodeValue.MethodCall(new CodeValue.Literal(FxmlProcessor.RESOURCES_NAME), "getString",
                                              List.of(new CodeValue.String("a"))),
                     valueResolver.resolveCodeValue(String.class, new Value.Resource("a")));
    }

    @Test
    void testResolveLiteral() {
        assertEquals(new CodeValue.String("a"),
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
