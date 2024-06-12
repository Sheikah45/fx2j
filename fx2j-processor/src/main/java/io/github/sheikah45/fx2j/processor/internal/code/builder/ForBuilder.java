package io.github.sheikah45.fx2j.processor.internal.code.builder;

import io.github.sheikah45.fx2j.processor.internal.code.BlockStatement;
import io.github.sheikah45.fx2j.processor.internal.code.CodeValues;
import io.github.sheikah45.fx2j.processor.internal.code.Declarator;
import io.github.sheikah45.fx2j.processor.internal.code.Expression;
import io.github.sheikah45.fx2j.processor.internal.code.Parameter;
import io.github.sheikah45.fx2j.processor.internal.code.Statement;
import io.github.sheikah45.fx2j.processor.internal.code.StatementExpression;
import io.github.sheikah45.fx2j.processor.internal.code.TypeValue;
import io.github.sheikah45.fx2j.processor.internal.code.TypeValues;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ForBuilder {

    private ForTypeBuilder forTypeBuilder;
    private BlockStatement.Block body = CodeValues.block();

    public ForBuilder() {}

    public ForBuilder body(Consumer<BlockBuilder> consumer) {
        BlockBuilder builder = new BlockBuilder();
        consumer.accept(builder);
        body = builder.build();
        return this;
    }

    public ForBuilder enhanced(Consumer<ForTypeBuilder.Enhanced> consumer) {
        ForTypeBuilder.Enhanced builder = new ForTypeBuilder.Enhanced();
        consumer.accept(builder);
        forTypeBuilder = builder;
        return this;
    }

    public ForBuilder basic(Consumer<ForTypeBuilder.Basic> consumer) {
        ForTypeBuilder.Basic builder = new ForTypeBuilder.Basic();
        consumer.accept(builder);
        forTypeBuilder = builder;
        return this;
    }

    public BlockStatement.For build() {
        BlockStatement.For.Type type = switch (forTypeBuilder) {
            case ForTypeBuilder.Basic builder -> builder.build();
            case ForTypeBuilder.Enhanced builder -> builder.build();
            case null -> new BlockStatement.For.BasicStatementExpression(List.of(), CodeValues.empty(), List.of());
        };
        return new BlockStatement.For(type, body);
    }

    public sealed interface ForTypeBuilder {

        final class Basic implements ForTypeBuilder {
            private final List<Expression> incrementors = new ArrayList<>();
            private InitializerBuilder initializerBuilder;
            private Expression terminator = CodeValues.empty();

            private Basic() {}

            public <T> Basic localVariable(
                    Consumer<InitializerBuilder.LocalVariableDeclarationInitializerBuilder> consumer) {
                InitializerBuilder.LocalVariableDeclarationInitializerBuilder declarationBuilder = new InitializerBuilder.LocalVariableDeclarationInitializerBuilder();
                consumer.accept(declarationBuilder);
                initializerBuilder = declarationBuilder;
                return this;
            }

            public <T> Basic statementExpression(
                    Consumer<InitializerBuilder.StatementExpressionInitializerBuilder> consumer) {
                InitializerBuilder.StatementExpressionInitializerBuilder statementExpressionBuilder = new InitializerBuilder.StatementExpressionInitializerBuilder();
                new InitializerBuilder.StatementExpressionInitializerBuilder();
                consumer.accept(statementExpressionBuilder);
                initializerBuilder = statementExpressionBuilder;
                return this;
            }

            public Basic terminator(Expression terminator) {
                this.terminator = terminator;
                return this;
            }

            public Basic incrementor(Expression incrementor) {
                incrementors.add(incrementor);
                return this;
            }

            private BlockStatement.For.Type build() {
                return switch (initializerBuilder) {
                    case InitializerBuilder.LocalVariableDeclarationInitializerBuilder builder ->
                            new BlockStatement.For.BasicDeclaration(builder.build(), terminator, incrementors);
                    case InitializerBuilder.StatementExpressionInitializerBuilder builder ->
                            new BlockStatement.For.BasicStatementExpression(builder.build(), terminator, incrementors);
                    case null -> new BlockStatement.For.BasicStatementExpression(List.of(), terminator, incrementors);
                };
            }

            public sealed interface InitializerBuilder {

                final class StatementExpressionInitializerBuilder implements InitializerBuilder {
                    private final List<StatementExpression> statementExpressions = new ArrayList<>();

                    public StatementExpressionInitializerBuilder statementExpression(
                            StatementExpression statementExpression) {
                        statementExpressions.add(statementExpression);
                        return this;
                    }

                    private List<StatementExpression> build() {
                        return List.copyOf(statementExpressions);
                    }
                }

                final class LocalVariableDeclarationInitializerBuilder implements InitializerBuilder {
                    private final List<Declarator> declarators = new ArrayList<>();
                    private TypeValue.Declarable type = TypeValues.of(Object.class);

                    public LocalVariableDeclarationInitializerBuilder type(Class<?> type) {
                        this.type = TypeValues.of(type);
                        return this;
                    }

                    public LocalVariableDeclarationInitializerBuilder type(ParameterizedType type) {
                        this.type = TypeValues.of(type);
                        return this;
                    }

                    public LocalVariableDeclarationInitializerBuilder declaration(String identifier) {
                        declarators.add(CodeValues.variable(identifier));
                        return this;
                    }

                    public LocalVariableDeclarationInitializerBuilder declaration(String identifier, Object value) {
                        declarators.add(CodeValues.assignment(identifier, value));
                        return this;
                    }

                    public LocalVariableDeclarationInitializerBuilder declaration(Expression.Assignable assignable,
                                                                                  Object value) {
                        declarators.add(CodeValues.assignment(assignable, value));
                        return this;
                    }

                    private Statement.Declaration build() {
                        return new Statement.Declaration(type, declarators);
                    }
                }

            }
        }

        final class Enhanced implements ForTypeBuilder {
            private Parameter parameter = CodeValues.parameter(Object.class, "object");
            private Expression source = CodeValues.empty();

            private Enhanced() {}

            public Enhanced identifier(Type type, String identifier) {
                parameter = CodeValues.parameter(type, identifier);
                return this;
            }

            public Enhanced source(Expression expression) {
                source = expression;
                return this;
            }

            private BlockStatement.For.Enhanced build() {
                return new BlockStatement.For.Enhanced(parameter, source);
            }
        }
    }

}
