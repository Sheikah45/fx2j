package io.github.sheikah45.fx2j.processor.internal.code.builder;

import io.github.sheikah45.fx2j.processor.internal.code.Block;
import io.github.sheikah45.fx2j.processor.internal.code.TypeValue;
import io.github.sheikah45.fx2j.processor.internal.code.TypeValues;
import io.github.sheikah45.fx2j.processor.internal.code.CodeValues;
import io.github.sheikah45.fx2j.processor.internal.code.Expression;
import io.github.sheikah45.fx2j.processor.internal.code.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class TryBuilder {

    private final List<Block.Try.Catch> catches = new ArrayList<>();

    private List<Resource> resources = List.of();
    private Block.Simple body = CodeValues.block();
    private Block.Simple finallyBlock = CodeValues.block();

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

    public Block.Try build() {
        return new Block.Try(resources, body, catches, finallyBlock);
    }

    public static final class ResourcesBuilder {
        private final List<Resource> resources = new ArrayList<>();

        private ResourcesBuilder() {}

        public ResourcesBuilder resource(String identifier) {
            resources.add(CodeValues.variable(identifier));
            return this;
        }

        public ResourcesBuilder resource(Class<? extends AutoCloseable> type, String identifier,
                                         Expression initializer) {
            resources.add(new Resource.ResourceDeclaration(TypeValues.of(type), identifier, initializer));
            return this;
        }

        public ResourcesBuilder resource(Resource resource) {
            resources.add(resource);
            return this;
        }

        private List<Resource> build() {
            return List.copyOf(resources);
        }
    }

    public static final class CatchBuilder {
        private final List<TypeValue.Declarable> exceptionTypes = new ArrayList<>();

        private String identifier = "exception";
        private Block.Simple body = CodeValues.block();

        private CatchBuilder() {}

        public CatchBuilder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public CatchBuilder exception(Class<? extends Throwable> exceptionType) {
            exceptionTypes.add(TypeValues.of(exceptionType));
            return this;
        }

        public CatchBuilder body(Consumer<BlockBuilder> consumer) {
            BlockBuilder builder = new BlockBuilder();
            consumer.accept(builder);
            body = builder.build();
            return this;
        }

        private Block.Try.Catch build() {
            return new Block.Try.Catch(identifier, exceptionTypes, body);
        }
    }

}
