package io.github.sheikah45.fx2j.processor.internal.code.builder;

import io.github.sheikah45.fx2j.processor.internal.code.CodeValue;
import io.github.sheikah45.fx2j.processor.internal.code.CodeValues;

import java.util.ArrayList;
import java.util.List;

public final class BlockBuilder {

    private final List<CodeValue.Statement> statements = new ArrayList<>();

    public BlockBuilder() {}

    public BlockBuilder statement(CodeValue.Statement... statements) {
        this.statements.addAll(List.of(statements));
        return this;
    }

    public BlockBuilder block(CodeValue.Block block) {
        statements.addAll(block.statements());
        return this;
    }

    public CodeValue.Block build() {
        return CodeValues.block(statements);
    }
}
