package io.github.sheikah45.fx2j.processor.internal.code;

import java.util.List;
import java.util.Objects;

sealed public interface Block extends Statement {
    sealed interface For extends Block {
        record Loop(Expression initializer,
                    Expression termination,
                    List<? extends Expression> incrementors,
                    Simple body)
                implements For {
            public Loop {
                Objects.requireNonNull(initializer, "initializer cannot be null");
                Objects.requireNonNull(termination, "termination cannot be null");
                Objects.requireNonNull(incrementors, "incrementors cannot be null");
                incrementors = List.copyOf(incrementors);
            }
        }

        record Each(Parameter loopParameter, Expression parameters, Simple body) implements For {
            public Each {
                Objects.requireNonNull(loopParameter, "loopParameter cannot be null");
                Objects.requireNonNull(parameters, "parameters cannot be null");
                Objects.requireNonNull(body, "body cannot be null");
            }
        }
    }
    record Try(List<Resource> resources, Simple body, List<Catch> catchBlocks, Simple finallyBlock)
            implements Block {
        public Try {
            Objects.requireNonNull(resources, "resources cannot be null");
            Objects.requireNonNull(body, "body cannot be null");
            Objects.requireNonNull(catchBlocks, "catchBlocks cannot be null");
            Objects.requireNonNull(finallyBlock, "finallyBlock cannot be null");
            resources = List.copyOf(resources);
            catchBlocks = List.copyOf(catchBlocks);
        }

        public record Catch(String identifier, List<? extends TypeValue.Declarable> exceptionTypes, Simple body) {}
    }
    record Simple(List<? extends Statement> statements) {
        public Simple {
            Objects.requireNonNull(statements, "statements cannot be null");
            statements = List.copyOf(statements);
        }
    }
}
