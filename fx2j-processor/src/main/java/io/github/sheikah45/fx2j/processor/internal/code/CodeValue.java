package io.github.sheikah45.fx2j.processor.internal.code;

import java.util.List;
import java.util.Objects;

public sealed interface CodeValue {

    sealed interface Expression extends CodeValue {}
    sealed interface Assignable extends Expression {}
    sealed interface Statement extends CodeValue {}
    sealed interface BlockStatement extends Statement {}
    sealed interface StatementExpression extends Expression, Statement {}
    sealed interface Resource {}
    sealed interface Declarator {}

    sealed interface Literal extends Expression {
        record Null() implements Literal {}
        record Bool(boolean value) implements Literal {}
        record Char(char value) implements Literal {}
        record Byte(byte value) implements Literal {}
        record Short(short value) implements Literal {}
        record Int(int value) implements Literal {}
        record Long(long value) implements Literal {}
        record Float(float value) implements Literal {}
        record Double(double value) implements Literal {}
        record Str(String value) implements Literal {
            public Str {
                Objects.requireNonNull(value, "value cannot be null");
            }
        }
    }

    sealed interface Array extends Expression {
        record Declared(CodeType.Declarable componentType, List<? extends Expression> values) implements
                Array {
            public Declared {
                Objects.requireNonNull(componentType, "componentType cannot be null");
                Objects.requireNonNull(values, "values cannot be null");
                values = List.copyOf(values);
            }
        }
        record Sized(CodeType.Declarable componentType, int size) implements Array {
            public Sized {
                Objects.requireNonNull(componentType, "componentType cannot be null");
            }
        }
    }

    sealed interface Lambda extends Expression {
        sealed interface Arrow extends Lambda {
            record Typed(List<CodeValue.Parameter> parameters, Block body) implements Arrow {
                public Typed {
                    Objects.requireNonNull(parameters, "parameters cannot be null");
                    Objects.requireNonNull(body, "body cannot be null");
                    parameters = List.copyOf(parameters);
                }
            }
            record Untyped(List<String> parameters, Block body) implements Arrow {
                public Untyped {
                    Objects.requireNonNull(parameters, "receiver cannot be null");
                    Objects.requireNonNull(body, "body cannot be null");
                    parameters = List.copyOf(parameters);
                }
            }
        }
        record MethodReference(CodeValue.Expression receiver, String methodName) implements Lambda {
            public MethodReference {
                Objects.requireNonNull(receiver, "receiver cannot be null");
                Objects.requireNonNull(methodName, "methodName cannot be null");
            }
        }

    }

    sealed interface Return extends Statement {
        record Value(CodeValue.Expression value) implements Return {
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

    sealed interface For extends BlockStatement {
        record Loop(Expression initializer, Expression termination, List<? extends Expression> incrementors, Block body)
                implements For {
            public Loop {
                Objects.requireNonNull(initializer, "initializer cannot be null");
                Objects.requireNonNull(termination, "termination cannot be null");
                Objects.requireNonNull(incrementors, "incrementors cannot be null");
                incrementors = List.copyOf(incrementors);
            }
        }

        record Each(Parameter loopParameter, Expression parameters, Block body) implements For {
            public Each {
                Objects.requireNonNull(loopParameter, "loopParameter cannot be null");
                Objects.requireNonNull(parameters, "parameters cannot be null");
                Objects.requireNonNull(body, "body cannot be null");
            }
        }
    }
    record Variable(String identifier) implements Assignable, Resource, Declarator {
        public Variable {
            Objects.requireNonNull(identifier, "identifier cannot be null");
        }
    }
    record Type(CodeType type) implements Expression {
        public Type {
            Objects.requireNonNull(type, "type cannot be null");
        }
    }
    record Enum(java.lang.Enum<?> value) implements Expression {
        public Enum {
            Objects.requireNonNull(value, "value cannot be null");
        }
    }
    record FieldAccess(Expression receiver, java.lang.String field) implements Assignable {
        public FieldAccess {
            Objects.requireNonNull(receiver, "receiver cannot be null");
            Objects.requireNonNull(field, "field cannot be null");
        }
    }
    record ArrayAccess(Expression receiver, Expression accessor) implements Assignable {
        public ArrayAccess {
            Objects.requireNonNull(receiver, "receiver cannot be null");
            Objects.requireNonNull(accessor, "accessor cannot be null");
        }
    }
    record NewInstance(CodeType.Declarable type, List<? extends Expression> args) implements StatementExpression {
        public NewInstance {
            Objects.requireNonNull(type, "type cannot be null");
            Objects.requireNonNull(args, "args cannot be null");
            args = List.copyOf(args);
        }
    }
    record MethodCall(Expression receiver, java.lang.String methodName, List<? extends Expression> args)
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
    record Declaration(CodeType.Declarable type, List<? extends Declarator> declarators) implements Statement {}
    record LineBreak() implements Statement {}
    record Block(List<? extends CodeValue.Statement> statements) {
        public Block {
            Objects.requireNonNull(statements, "statements cannot be null");
            statements = List.copyOf(statements);
        }
    }
    record ResourceDeclaration(CodeType.Declarable type, String identifier, Expression initializer)
            implements Resource {}
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
    }
    record Catch(String identifier, List<? extends CodeType.Declarable> exceptionTypes, Block body) {}
    record Throw(CodeValue.Expression exception) implements Statement {}

    record Parameter(CodeType.Declarable type, String identifier) {}
}
