package io.github.sheikah45.fx2j.parser.property;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.CONCURRENT)
class ExpressionTest {

    @Test
    void testNullLiteral() {
        assertEquals(new BindExpression.Null(), BindExpression.parse("null"));
    }

    @Test
    void testBooleanLiteral() {
        assertEquals(new BindExpression.Boolean(true), BindExpression.parse("true"));
        assertEquals(new BindExpression.Boolean(false), BindExpression.parse("false"));
    }

    @Test
    void testStringLiteral() {
        assertEquals(new BindExpression.String("\"true\""), BindExpression.parse("'\"true\"'"));
        assertEquals(new BindExpression.String("'false'"), BindExpression.parse("\"'false'\""));
    }

    @Test
    void testWholeLiteral() {
        assertEquals(new BindExpression.Whole(10), BindExpression.parse("10"));
    }

    @Test
    void testFractionalLiteral() {
        assertEquals(new BindExpression.Fraction(10.0), BindExpression.parse("10.0"));
        assertEquals(new BindExpression.Fraction(10.0), BindExpression.parse("1.0e1"));
        assertEquals(new BindExpression.Fraction(10.0), BindExpression.parse("1e1"));
        assertEquals(new BindExpression.Fraction(10.0), BindExpression.parse(".1e2"));
    }

    @Test
    void testVariable() {
        assertEquals(new BindExpression.Variable("test"), BindExpression.parse("test"));
    }

    @Test
    void testEnclosed() {
        assertEquals(new BindExpression.Whole(10), BindExpression.parse("(10)"));
    }

    @Test
    void testPropertyRead() {
        assertEquals(new BindExpression.PropertyRead(new BindExpression.Variable("test"), "text"),
                     BindExpression.parse("test.text"));
    }

    @Test
    void testMethodCall() {
        assertEquals(new BindExpression.MethodCall(new BindExpression.Variable("test"), "run",
                                                   List.of(new BindExpression.Variable("a"))),
                     BindExpression.parse("test.run(a)"));
    }

    @Test
    void testCollectionAccess() {
        assertEquals(
                new BindExpression.CollectionAccess(new BindExpression.Variable("test"), new BindExpression.Whole(0)),
                BindExpression.parse("test[0]"));
    }

    @Test
    void testNegate() {
        assertEquals(new BindExpression.Negate(new BindExpression.Whole(10)), BindExpression.parse("-10"));
    }

    @Test
    void testMultiplicative() {
        assertEquals(new BindExpression.Multiply(new BindExpression.Whole(10), new BindExpression.Whole(10)),
                     BindExpression.parse("10*10"));
        assertEquals(new BindExpression.Divide(new BindExpression.Whole(10), new BindExpression.Whole(10)),
                     BindExpression.parse("10/10"));
        assertEquals(new BindExpression.Modulo(new BindExpression.Whole(10), new BindExpression.Whole(10)),
                     BindExpression.parse("10%10"));
    }

    @Test
    void testAdditive() {
        assertEquals(new BindExpression.Add(new BindExpression.Whole(10), new BindExpression.Whole(10)),
                     BindExpression.parse("10+10"));
        assertEquals(new BindExpression.Subtract(new BindExpression.Whole(10), new BindExpression.Whole(10)),
                     BindExpression.parse("10-10"));
    }

    @Test
    void testComparative() {
        assertEquals(new BindExpression.GreaterThan(new BindExpression.Whole(10), new BindExpression.Whole(10)),
                     BindExpression.parse("10>10"));
        assertEquals(new BindExpression.GreaterThanEqual(new BindExpression.Whole(10), new BindExpression.Whole(10)),
                     BindExpression.parse("10>=10"));
        assertEquals(new BindExpression.LessThan(new BindExpression.Whole(10), new BindExpression.Whole(10)),
                     BindExpression.parse("10<10"));
        assertEquals(new BindExpression.LessThanEqual(new BindExpression.Whole(10), new BindExpression.Whole(10)),
                     BindExpression.parse("10<=10"));
        assertEquals(new BindExpression.Equal(new BindExpression.Whole(10), new BindExpression.Whole(10)),
                     BindExpression.parse("10==10"));
        assertEquals(new BindExpression.NotEqual(new BindExpression.Whole(10), new BindExpression.Whole(10)),
                     BindExpression.parse("10!=10"));
    }

    @Test
    void testInvert() {
        assertEquals(new BindExpression.Invert(new BindExpression.Boolean(false)), BindExpression.parse("!false"));
    }

    @Test
    void testLogical() {
        assertEquals(new BindExpression.And(new BindExpression.Boolean(true), new BindExpression.Boolean(true)),
                     BindExpression.parse("true&&true"));
        assertEquals(new BindExpression.Or(new BindExpression.Boolean(true), new BindExpression.Boolean(true)),
                     BindExpression.parse("true||true"));
    }

    @Test
    void testOrderOfOperations() {
        BindExpression.Whole whole10 = new BindExpression.Whole(10);
        assertEquals(new BindExpression.And(new BindExpression.LessThan(
                             new BindExpression.Add(
                                     new BindExpression.Subtract(whole10, new BindExpression.Multiply(whole10, whole10)),
                                     whole10),
                             new BindExpression.Variable("a")), new BindExpression.Boolean(true)),
                     BindExpression.parse("10-10*10+10<a&&true"));
    }

}
