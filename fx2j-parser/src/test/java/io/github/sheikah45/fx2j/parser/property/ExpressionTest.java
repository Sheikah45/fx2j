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
        assertEquals(new Expression.Null(), Expression.parse("null"));
    }

    @Test
    void testBooleanLiteral() {
        assertEquals(new Expression.Boolean(true), Expression.parse("true"));
        assertEquals(new Expression.Boolean(false), Expression.parse("false"));
    }

    @Test
    void testStringLiteral() {
        assertEquals(new Expression.String("\"true\""), Expression.parse("'\"true\"'"));
        assertEquals(new Expression.String("'false'"), Expression.parse("\"'false'\""));
    }

    @Test
    void testWholeLiteral() {
        assertEquals(new Expression.Whole(10), Expression.parse("10"));
    }

    @Test
    void testFractionalLiteral() {
        assertEquals(new Expression.Fraction(10.0), Expression.parse("10.0"));
        assertEquals(new Expression.Fraction(10.0), Expression.parse("1.0e1"));
        assertEquals(new Expression.Fraction(10.0), Expression.parse("1e1"));
        assertEquals(new Expression.Fraction(10.0), Expression.parse(".1e2"));
    }

    @Test
    void testVariable() {
        assertEquals(new Expression.Variable("test"), Expression.parse("test"));
    }

    @Test
    void testEnclosed() {
        assertEquals(new Expression.Whole(10), Expression.parse("(10)"));
    }

    @Test
    void testPropertyRead() {
        assertEquals(new Expression.PropertyRead(new Expression.Variable("test"), "text"),
                     Expression.parse("test.text"));
    }

    @Test
    void testMethodCall() {
        assertEquals(new Expression.MethodCall(new Expression.Variable("test"), "run",
                                               List.of(new Expression.Variable("a"))), Expression.parse("test.run(a)"));
    }

    @Test
    void testCollectionAccess() {
        assertEquals(new Expression.CollectionAccess(new Expression.Variable("test"), new Expression.Whole(0)),
                     Expression.parse("test[0]"));
    }

    @Test
    void testNegate() {
        assertEquals(new Expression.Negate(new Expression.Whole(10)), Expression.parse("-10"));
    }

    @Test
    void testMultiplicative() {
        assertEquals(new Expression.Multiply(new Expression.Whole(10), new Expression.Whole(10)),
                     Expression.parse("10*10"));
        assertEquals(new Expression.Divide(new Expression.Whole(10), new Expression.Whole(10)),
                     Expression.parse("10/10"));
        assertEquals(new Expression.Modulo(new Expression.Whole(10), new Expression.Whole(10)),
                     Expression.parse("10%10"));
    }

    @Test
    void testAdditive() {
        assertEquals(new Expression.Add(new Expression.Whole(10), new Expression.Whole(10)), Expression.parse("10+10"));
        assertEquals(new Expression.Subtract(new Expression.Whole(10), new Expression.Whole(10)),
                     Expression.parse("10-10"));
    }

    @Test
    void testComparative() {
        assertEquals(new Expression.GreaterThan(new Expression.Whole(10), new Expression.Whole(10)),
                     Expression.parse("10>10"));
        assertEquals(new Expression.GreaterThanEqual(new Expression.Whole(10), new Expression.Whole(10)),
                     Expression.parse("10>=10"));
        assertEquals(new Expression.LessThan(new Expression.Whole(10), new Expression.Whole(10)),
                     Expression.parse("10<10"));
        assertEquals(new Expression.LessThanEqual(new Expression.Whole(10), new Expression.Whole(10)),
                     Expression.parse("10<=10"));
        assertEquals(new Expression.Equal(new Expression.Whole(10), new Expression.Whole(10)),
                     Expression.parse("10==10"));
        assertEquals(new Expression.NotEqual(new Expression.Whole(10), new Expression.Whole(10)),
                     Expression.parse("10!=10"));
    }

    @Test
    void testInvert() {
        assertEquals(new Expression.Invert(new Expression.Boolean(false)), Expression.parse("!false"));
    }

    @Test
    void testLogical() {
        assertEquals(new Expression.And(new Expression.Boolean(true), new Expression.Boolean(true)),
                     Expression.parse("true&&true"));
        assertEquals(new Expression.Or(new Expression.Boolean(true), new Expression.Boolean(true)),
                     Expression.parse("true||true"));
    }

    @Test
    void testOrderOfOperations() {
        Expression.Whole whole10 = new Expression.Whole(10);
        assertEquals(new Expression.And(new Expression.LessThan(
                new Expression.Add(new Expression.Subtract(whole10, new Expression.Multiply(whole10, whole10)),
                                   whole10),
                new Expression.Variable("a")), new Expression.Boolean(true)), Expression.parse("10-10*10+10<a&&true"));
    }

}
