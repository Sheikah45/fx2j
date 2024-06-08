package io.github.sheikah45.fx2j.processor.internal.code.builder;

import io.github.sheikah45.fx2j.processor.internal.code.CodeType;
import io.github.sheikah45.fx2j.processor.internal.code.CodeTypes;
import io.github.sheikah45.fx2j.processor.internal.code.CodeValue;
import io.github.sheikah45.fx2j.processor.internal.code.CodeValues;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class TryBuilder {

    private final List<CodeValue.Catch> catches = new ArrayList<>();

    private List<CodeValue.Resource> resources = List.of();
    private CodeValue.Block body = CodeValues.block();
    private CodeValue.Block finallyBlock = CodeValues.block();

    public TryBuilder() {}

    public TryBuilder with(Consumer<ResourcesBuilder> consumer) {
        ResourcesBuilder builder = new ResourcesBuilder();
        consumer.accept(builder);
        resources = builder.build();
        return this;
    }

    public TryBuilder body(Consumer<BlockBuilder> consumer) {
        BlockBuilder builder = new BlockBuilder();
        consumer.accept(builder);
        body = builder.build();
        return this;
    }

    public TryBuilder catches(Consumer<CatchBuilder> consumer) {
        CatchBuilder builder = new CatchBuilder();
        consumer.accept(builder);
        catches.add(builder.build());
        return this;
    }

    public TryBuilder finallyRuns(Consumer<BlockBuilder> consumer) {
        BlockBuilder builder = new BlockBuilder();
        consumer.accept(builder);
        finallyBlock = builder.build();
        return this;
    }

    public CodeValue.Try build() {
        return new CodeValue.Try(resources, body, catches, finallyBlock);
    }

    public static final class ResourcesBuilder {
        private final List<CodeValue.Resource> resources = new ArrayList<>();

        private ResourcesBuilder() {}

        public ResourcesBuilder resource(String identifier) {
            resources.add(CodeValues.variable(identifier));
            return this;
        }

        public ResourcesBuilder resource(Class<? extends AutoCloseable> type, String identifier,
                                         CodeValue.Expression initializer) {
            resources.add(new CodeValue.ResourceDeclaration(CodeTypes.of(type), identifier, initializer));
            return this;
        }

        public ResourcesBuilder resource(CodeValue.Resource resource) {
            resources.add(resource);
            return this;
        }

        private List<CodeValue.Resource> build() {
            return List.copyOf(resources);
        }
    }

    public static final class CatchBuilder {
        private final List<CodeType.Declarable> exceptionTypes = new ArrayList<>();

        private String identifier = "exception";
        private CodeValue.Block body = CodeValues.block();

        private CatchBuilder() {}

        public CatchBuilder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public CatchBuilder exception(Class<? extends Throwable> exceptionType) {
            exceptionTypes.add(CodeTypes.of(exceptionType));
            return this;
        }

        public CatchBuilder body(Consumer<BlockBuilder> consumer) {
            BlockBuilder builder = new BlockBuilder();
            consumer.accept(builder);
            body = builder.build();
            return this;
        }

        private CodeValue.Catch build() {
            return new CodeValue.Catch(identifier, exceptionTypes, body);
        }
    }

}
