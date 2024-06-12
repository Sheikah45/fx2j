package io.github.sheikah45.fx2j.processor.internal.code;

import java.util.List;
import java.util.Objects;

sealed public interface Statement
        permits BlockStatement, Statement.Declaration, Statement.LineBreak, Statement.Throw, Statement.Break,
        Statement.Continue, Statement.Return, StatementExpression {
    sealed interface Return extends Statement {
        record Value(Expression value) implements Return {
            public Value {
                Objects.requireNonNull(value, "value cannot be null");
            }
        }

        record Void() implements Return {}
    }
    sealed interface Break extends Statement {
        record Labeled(String label) implements Break {
            public Labeled {
                Objects.requireNonNull(label, "label cannot be null");
            }
        }

        record Unlabeled() implements Break {}
    }
    sealed interface Continue extends Statement {
        record Labeled(String label) implements Continue {
            public Labeled {
                Objects.requireNonNull(label, "label cannot be null");
            }
        }

        record Unlabeled() implements Continue {}
    }
    record Declaration(TypeValue.Declarable type, List<? extends Declarator> declarators) implements Statement {}
    record LineBreak() implements Statement {}
    record Throw(Expression exception) implements Statement {}
}
