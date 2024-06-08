package io.github.sheikah45.fx2j.processor.internal.code.builder;

import io.github.sheikah45.fx2j.processor.internal.code.CodeType;
import io.github.sheikah45.fx2j.processor.internal.code.CodeValue;
import io.github.sheikah45.fx2j.processor.internal.code.CodeValues;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class LambdaBuilder {

    private LambdaParameterBuilder lambdaParameterBuilder;
    private CodeValue.Block body = CodeValues.block();

    public LambdaBuilder typed(Consumer<LambdaParameterBuilder.Typed> consumer) {
        LambdaParameterBuilder.Typed parameterBuilder = new LambdaParameterBuilder.Typed();
        consumer.accept(parameterBuilder);
        this.lambdaParameterBuilder = parameterBuilder;
        return this;
    }

    public LambdaBuilder untyped(Consumer<LambdaParameterBuilder.Untyped> consumer) {
        LambdaParameterBuilder.Untyped parameterBuilder = new LambdaParameterBuilder.Untyped();
        consumer.accept(parameterBuilder);
        this.lambdaParameterBuilder = parameterBuilder;
        return this;
    }

    public LambdaBuilder body(Consumer<BlockBuilder> consumer) {
        BlockBuilder blockBuilder = new BlockBuilder();
        consumer.accept(blockBuilder);
        body = blockBuilder.build();
        return this;
    }

    public CodeValue.Lambda.Arrow build() {
        return switch (lambdaParameterBuilder) {
            case LambdaParameterBuilder.Typed builder -> new CodeValue.Lambda.Arrow.Typed(builder.build(), body);
            case LambdaParameterBuilder.Untyped builder -> new CodeValue.Lambda.Arrow.Untyped(builder.build(), body);
            case null -> new CodeValue.Lambda.Arrow.Untyped(List.of(), body);
        };
    }

    public sealed interface LambdaParameterBuilder {

        final class Typed implements LambdaParameterBuilder {
            private final List<CodeValue.Parameter> parameters = new ArrayList<>();

            public Typed parameter(Type type, String identifier) {
                parameters.add(CodeValues.parameter(type, identifier));
                return this;
            }

            public Typed parameter(CodeType.Declarable type, String identifier) {
                parameters.add(CodeValues.parameter(type, identifier));
                return this;
            }

            public Typed parameter(CodeValue.Parameter parameter) {
                parameters.add(parameter);
                return this;
            }

            private List<CodeValue.Parameter> build() {
                return parameters;
            }
        }

        final class Untyped implements LambdaParameterBuilder {
            private final List<String> parameters = new ArrayList<>();

            public Untyped parameter(String identifier) {
                parameters.add(identifier);
                return this;
            }

            private List<String> build() {
                return parameters;
            }
        }
    }

}
