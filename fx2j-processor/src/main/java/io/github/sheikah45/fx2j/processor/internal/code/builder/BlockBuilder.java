package io.github.sheikah45.fx2j.processor.internal.code.builder;

import io.github.sheikah45.fx2j.processor.internal.code.Block;
import io.github.sheikah45.fx2j.processor.internal.code.CodeValues;
import io.github.sheikah45.fx2j.processor.internal.code.Statement;

import java.util.ArrayList;
import java.util.List;

public final class BlockBuilder {

    private final List<Statement> statements = new ArrayList<>();

    public BlockBuilder() {}

    public BlockBuilder statement(Statement... statements) {
        this.statements.addAll(List.of(statements));
        return this;
    }

    public BlockBuilder block(Block.Simple block) {
        statements.addAll(block.statements());
        return this;
    }

    public Block.Simple build() {
        return CodeValues.block(statements);
    }
}
