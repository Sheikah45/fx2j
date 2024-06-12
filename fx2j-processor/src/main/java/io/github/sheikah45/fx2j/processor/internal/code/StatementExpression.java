package io.github.sheikah45.fx2j.processor.internal.code;

import java.util.List;
import java.util.Objects;

sealed public interface StatementExpression extends Expression, Statement {
    record Empty() implements StatementExpression {}
    record NewInstance(TypeValue.Declarable type, List<? extends Expression> args) implements StatementExpression {
        public NewInstance {
            Objects.requireNonNull(type, "type cannot be null");
            Objects.requireNonNull(args, "args cannot be null");
            args = List.copyOf(args);
        }
    }
    record MethodCall(Expression receiver, String methodName, List<? extends Expression> args)
            implements StatementExpression {
        public MethodCall {
            Objects.requireNonNull(receiver, "receiver cannot be null");
            Objects.requireNonNull(methodName, "methodName cannot be null");
            Objects.requireNonNull(args, "args cannot be null");
            args = List.copyOf(args);
        }
    }
    record Assignment<T extends Assignable>(T receiver, Expression value) implements StatementExpression, Declarator {
        public Assignment {
            Objects.requireNonNull(receiver, "receiver cannot be null");
            Objects.requireNonNull(value, "value cannot be null");
        }
    }
    record PreIncrement(Assignable receiver) implements StatementExpression {
        public PreIncrement {
            Objects.requireNonNull(receiver, "receiver cannot be null");
        }
    }
    record PostIncrement(Assignable receiver) implements StatementExpression {
        public PostIncrement {
            Objects.requireNonNull(receiver, "receiver cannot be null");
        }
    }
    record PreDecrement(Assignable receiver) implements StatementExpression {
        public PreDecrement {
            Objects.requireNonNull(receiver, "receiver cannot be null");
        }
    }
    record PostDecrement(Assignable receiver) implements StatementExpression {
        public PostDecrement {
            Objects.requireNonNull(receiver, "receiver cannot be null");
        }
    }
}
