package io.github.sheikah45.fx2j.processor.internal.resolve;

import io.github.sheikah45.fx2j.parser.property.Expression;
import io.github.sheikah45.fx2j.processor.internal.model.CodeValue;
import io.github.sheikah45.fx2j.processor.internal.model.ExpressionResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpressionResolverTest extends AbstractResolverTest {

    private final ExpressionResolver expressionResolver = resolverContainer.getExpressionResolver();

    @Test
    void testResolveNull() {
        assertEquals(new ExpressionResult(Object.class, "null", List.of()),
                     expressionResolver.resolveExpression(new Expression.Null()));
    }

    @Test
    void testResolveWhole() {
        assertEquals(new ExpressionResult(long.class, "long0", List.of(new CodeValue.Assignment(long.class, "long0",
                                                                                                new CodeValue.Literal(
                                                                                                        String.valueOf(
                                                                                                                1))))),
                     expressionResolver.resolveExpression(new Expression.Whole(1)));
    }

}
