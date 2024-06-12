package io.github.sheikah45.fx2j.processor.internal.code;

import java.util.List;
import java.util.Objects;

sealed public interface BlockStatement extends Statement {
    record Block(List<? extends Statement> statements) {
        public Block {
            Objects.requireNonNull(statements, "statements cannot be null");
            statements = List.copyOf(statements);
        }
    }
    record For(For.Type forType, Block body) implements BlockStatement {
        public For {
            Objects.requireNonNull(forType, "forType cannot be null");
            Objects.requireNonNull(body, "body cannot be null");
        }

        public sealed interface Type {}

        public record BasicDeclaration(Declaration initializer,
                                       Expression termination,
                                       List<? extends Expression> incrementors)
                implements Type {
            public BasicDeclaration {
                Objects.requireNonNull(initializer, "initializer cannot be null");
                Objects.requireNonNull(termination, "termination cannot be null");
                Objects.requireNonNull(incrementors, "incrementors cannot be null");
                incrementors = List.copyOf(incrementors);
            }
        }

        public record BasicStatementExpression(List<? extends StatementExpression> statementExpressions,
                                               Expression termination,
                                               List<? extends Expression> incrementors)
                implements Type {
            public BasicStatementExpression {
                Objects.requireNonNull(statementExpressions, "statementExpressions cannot be null");
                Objects.requireNonNull(termination, "termination cannot be null");
                Objects.requireNonNull(incrementors, "incrementors cannot be null");
                incrementors = List.copyOf(incrementors);
            }
        }

        public record Enhanced(Parameter loopParameter, Expression parameters) implements Type {
            public Enhanced {
                Objects.requireNonNull(loopParameter, "loopParameter cannot be null");
                Objects.requireNonNull(parameters, "parameters cannot be null");
            }
        }
    }
    record Try(List<Resource> resources, Block body, List<Catch> catchBlocks, Block finallyBlock)
            implements BlockStatement {
        public Try {
            Objects.requireNonNull(resources, "resources cannot be null");
            Objects.requireNonNull(body, "body cannot be null");
            Objects.requireNonNull(catchBlocks, "catchBlocks cannot be null");
            Objects.requireNonNull(finallyBlock, "finallyBlock cannot be null");
            resources = List.copyOf(resources);
            catchBlocks = List.copyOf(catchBlocks);
        }

        public record Catch(String identifier, List<? extends TypeValue.Declarable> exceptionTypes, Block body) {}
    }
}
