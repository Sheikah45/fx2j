package io.github.sheikah45.fx2j.processor.internal.utils;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;
import io.github.sheikah45.fx2j.processor.internal.code.Block;
import io.github.sheikah45.fx2j.processor.internal.code.TypeValue;
import io.github.sheikah45.fx2j.processor.internal.code.Declarator;
import io.github.sheikah45.fx2j.processor.internal.code.Expression;
import io.github.sheikah45.fx2j.processor.internal.code.Literal;
import io.github.sheikah45.fx2j.processor.internal.code.Parameter;
import io.github.sheikah45.fx2j.processor.internal.code.Resource;
import io.github.sheikah45.fx2j.processor.internal.code.Statement;
import io.github.sheikah45.fx2j.processor.internal.code.StatementExpression;

import java.util.List;

public class CodeBlockConverter {

    private static TypeName convertToTypeName(TypeValue type) {
        return switch (type) {
            case TypeValue.Raw.Primitive(String primitive) -> convertPrimitiveToTypeName(primitive);
            case TypeValue.Raw.Array array -> convertToArrayName(array);
            case TypeValue.Raw raw -> convertToClassName(raw);
            case TypeValue.Parameterized(TypeValue.Raw rawType, List<TypeValue> arguments) ->
                    ParameterizedTypeName.get(convertToClassName(rawType), arguments.stream()
                                                                                    .map(CodeBlockConverter::convertToTypeName)
                                                                                    .toArray(TypeName[]::new));
            case TypeValue.Variable(String name, List<TypeValue> upperBounds) -> TypeVariableName.get(name,
                                                                                                      upperBounds.stream()
                                                                                                                 .map(CodeBlockConverter::convertToTypeName)
                                                                                                               .toArray(
                                                                                                                       TypeName[]::new));
            case TypeValue.Wildcard(List<TypeValue> lowerBounds, List<TypeValue> upperBounds) when lowerBounds.size() ==
                                                                                                   1 &&
                                                                                                   upperBounds.isEmpty() ->
                    WildcardTypeName.supertypeOf(convertToTypeName(lowerBounds.getFirst()));
            case TypeValue.Wildcard(List<TypeValue> lowerBounds, List<TypeValue> upperBounds) when upperBounds.size() ==
                                                                                                   1 &&
                                                                                                   lowerBounds.isEmpty() ->
                    WildcardTypeName.subtypeOf(convertToTypeName(upperBounds.getFirst()));
            case TypeValue.Wildcard ignored -> throw new UnsupportedOperationException(
                    "Cannot generate wildcard typeName with multiple bound parameters");
        };
    }

    private static ClassName convertToClassName(TypeValue.Raw rawType) {
        return switch (rawType) {
            case TypeValue.Raw.Array ignored ->
                    throw new UnsupportedOperationException("Cannot convert array type to ClassName");
            case TypeValue.Raw.Primitive ignored ->
                    throw new UnsupportedOperationException("Cannot convert primitive type to ClassName");
            case TypeValue.Raw.Top(String packageName, String simpleName) -> ClassName.get(packageName, simpleName);
            case TypeValue.Raw.Nested(TypeValue.Raw ownerType, String simpleName) -> {
                ClassName className = convertToClassName(ownerType);
                if (className.enclosingClassName() != null) {
                    yield ClassName.get(className.enclosingClassName().packageName(),
                                        className.enclosingClassName().simpleName(), className.simpleName(),
                                        simpleName);
                } else {
                    yield ClassName.get(className.packageName(), className.simpleName(), simpleName);
                }
            }
        };
    }

    private static ArrayTypeName convertToArrayName(TypeValue.Raw.Array arrayType) {
        return ArrayTypeName.of(convertToTypeName(arrayType.componentType()));
    }

    private static TypeName convertPrimitiveToTypeName(String primitive) {
        return switch (primitive) {
            case "boolean" -> TypeName.BOOLEAN;
            case "char" -> TypeName.CHAR;
            case "byte" -> TypeName.BYTE;
            case "short" -> TypeName.SHORT;
            case "int" -> TypeName.INT;
            case "long" -> TypeName.LONG;
            case "float" -> TypeName.FLOAT;
            case "double" -> TypeName.DOUBLE;
            case String name -> throw new IllegalArgumentException("%s is not a primitive".formatted(name));
        };
    }

    public static CodeBlock convertExpressionToCodeBlock(Expression codeValue) {
        return switch (codeValue) {
            case Literal.Null() -> CodeBlock.of("null");
            case Literal.Bool(boolean value) -> CodeBlock.of("$L", value);
            case Literal.Char(char value) -> CodeBlock.of("$L", value);
            case Literal.Byte(byte value) -> CodeBlock.of("$L", value);
            case Literal.Short(short value) -> CodeBlock.of("$L", value);
            case Literal.Int(int value) -> CodeBlock.of("$L", value);
            case Literal.Long(long value) -> CodeBlock.of("$L", value);
            case Literal.Float(float value) -> CodeBlock.of("$L", value);
            case Literal.Double(double value) -> CodeBlock.of("$L", value);
            case Literal.Str(String value) -> CodeBlock.of("$S", value);
            case Expression.Variable(String value) -> CodeBlock.of("$L", value);
            case Expression.Type(TypeValue type) -> CodeBlock.of("$T", convertToTypeName(type));
            case Expression.Enum(Enum<?> value) -> CodeBlock.of("$T.$L", value.getDeclaringClass(), value.name());
            case Expression.FieldAccess(Expression receiver, String field) ->
                    CodeBlock.of("$L.$L", convertExpressionToCodeBlock(receiver), field);
            case Expression.Array.Declared(TypeValue componentType, List<? extends Expression> values) -> {
                CodeBlock valuesBlock = values.stream()
                                              .map(CodeBlockConverter::convertExpressionToCodeBlock)
                                              .collect(CodeBlock.joining(", "));
                yield CodeBlock.of("new $T[]{$L}", convertToTypeName(componentType), valuesBlock);
            }
            case Expression.Array.Sized(TypeValue componentType, int size) ->
                    CodeBlock.of("new $T[$L]", componentType, size);
            case StatementExpression.NewInstance(TypeValue.Declarable type, List<? extends Expression> args) -> {
                CodeBlock argsBlock = args.stream()
                                          .map(CodeBlockConverter::convertExpressionToCodeBlock)
                                          .collect(CodeBlock.joining(", "));
                yield CodeBlock.of("new $T($L)", convertToTypeName(type), argsBlock);
            }
            case Expression.Lambda.MethodReference(Expression receiver, String methodName) ->
                    CodeBlock.of("$L::$L", convertExpressionToCodeBlock(receiver), methodName);
            case Expression.Lambda.Arrow.Typed(
                    List<Parameter> parameters,
                    Block.Simple(List<? extends Statement> statements)
            ) -> {
                CodeBlock paramBlock = parameters.stream()
                                                 .map(parameter -> CodeBlock.of("$T $L",
                                                                                convertToTypeName(parameter.type()),
                                                                                parameter.identifier()))
                                                 .collect(CodeBlock.joining(", "));
                yield convertToLambda(paramBlock, statements);
            }
            case Expression.Lambda.Arrow.Untyped(
                    List<String> parameters, Block.Simple(List<? extends Statement> statements)
            ) -> {
                CodeBlock paramBlock = parameters.stream()
                                                 .map(parameter -> CodeBlock.of("$L", parameter))
                                                 .collect(CodeBlock.joining(", "));
                yield convertToLambda(paramBlock, statements);
            }
            case Expression.ArrayAccess(Expression receiver, Expression accessor) ->
                    CodeBlock.of("$L[$L]", convertExpressionToCodeBlock(receiver),
                                 convertExpressionToCodeBlock(accessor));
            case StatementExpression statementExpression ->
                    convertStatementExpressionToCodeBlock(statementExpression);
        };
    }

    private static CodeBlock convertToLambda(CodeBlock paramBlock, List<? extends Statement> body) {
        if (body.isEmpty()) {
            return CodeBlock.of("($L) -> {}", paramBlock);
        } else if (body.size() == 1) {
            CodeBlock bodyBlock = switch (body.getFirst()) {
                case Statement.Return.Void() -> CodeBlock.of("{}");
                case Statement.Return.Value(Expression value) -> convertExpressionToCodeBlock(value);
                case Block value -> CodeBlock.builder()
                                             .beginControlFlow("")
                                             .add(convertStatementToUnterminatedCodeBlock(value))
                                             .endControlFlow()
                                             .build();
                case Statement value -> convertStatementToUnterminatedCodeBlock(value);
            };
            return CodeBlock.of("($L) -> $L", paramBlock, bodyBlock);
        } else {
            CodeBlock bodyBlock = body.stream()
                                      .map(CodeBlockConverter::convertStatementToCodeBlock)
                                      .map(codeBlock -> CodeBlock.builder().add("\t").add(codeBlock).build())
                                      .collect(CodeBlock.joining("\n"));
            return CodeBlock.builder().beginControlFlow("($L) -> ", paramBlock).add(bodyBlock).endControlFlow().build();
        }
    }

    public static CodeBlock convertStatementToCodeBlock(Statement codeValue) {
        CodeBlock codeBlock = convertStatementToUnterminatedCodeBlock(codeValue);
        return switch (codeValue) {
            case Block ignored -> CodeBlock.builder().add(codeBlock).build();
            case Statement.LineBreak ignored -> CodeBlock.of("\n");
            default -> CodeBlock.builder().add(codeBlock).add(";\n").build();
        };
    }

    private static CodeBlock convertStatementToUnterminatedCodeBlock(Statement codeValue) {
        return switch (codeValue) {
            case Statement.Declaration(TypeValue.Declarable type, List<? extends Declarator> declarators) -> {
                CodeBlock declaratorsBlock = declarators.stream().map(declarator -> switch (declarator) {
                    case StatementExpression.Assignment<?>(Expression.Assignable receiver, Expression initializer) ->
                            CodeBlock.of("$L = $L", convertExpressionToCodeBlock(receiver),
                                         convertExpressionToCodeBlock(initializer));
                    case Expression.Variable(String identifier) -> CodeBlock.of("$L", identifier);
                }).collect(CodeBlock.joining(", "));
                yield CodeBlock.of("$T $L", convertToTypeName(type), declaratorsBlock);
            }
            case StatementExpression.Assignment(Expression.Assignable identifier, Expression value) ->
                    CodeBlock.of("$L = $L", convertExpressionToCodeBlock(identifier),
                                 convertExpressionToCodeBlock(value));
            case Statement.Return.Value(Expression value) ->
                    CodeBlock.of("return $L", convertExpressionToCodeBlock(value));
            case Statement.Return.Void() -> CodeBlock.of("return");
            case Statement.Continue.Unlabeled() -> CodeBlock.of("continue");
            case Statement.Continue.Labeled(String label) -> CodeBlock.of("continue $L", label);
            case Statement.Break.Unlabeled() -> CodeBlock.of("break");
            case Statement.Break.Labeled(String label) -> CodeBlock.of("break $L", label);
            case Statement.Throw(Expression exception) ->
                    CodeBlock.of("throw $L", convertExpressionToCodeBlock(exception));
            case Statement.LineBreak() -> CodeBlock.of("\n");
            case Block.For.Loop(
                    Expression initializer, Expression termination,
                    List<? extends Expression> incrementors,
                    Block.Simple(List<? extends Statement> statements)
            ) -> {
                CodeBlock incrementorBlock = incrementors.stream()
                                                         .map(CodeBlockConverter::convertExpressionToCodeBlock)
                                                         .collect(CodeBlock.joining(", "));
                CodeBlock bodyBlock = statements.stream()
                                                .map(CodeBlockConverter::convertStatementToCodeBlock)
                                                .collect(CodeBlock.joining(""));

                yield CodeBlock.builder()
                               .beginControlFlow("for ($L; $L; $L)", convertExpressionToCodeBlock(initializer),
                                                 convertExpressionToCodeBlock(termination), incrementorBlock)
                               .add(bodyBlock)
                               .endControlFlow()
                               .build();
            }
            case Block.For.Each(
                    Parameter(TypeValue.Declarable type, String identifier), Expression parameters,
                    Block.Simple(List<? extends Statement> statements)
            ) -> {
                CodeBlock bodyBlock = statements.stream()
                                                .map(CodeBlockConverter::convertStatementToCodeBlock)
                                                .collect(CodeBlock.joining(""));

                yield CodeBlock.builder()
                               .beginControlFlow("for ($T $L : $L)", convertToTypeName(type), identifier,
                                                 convertExpressionToCodeBlock(parameters))
                               .add(bodyBlock)
                               .endControlFlow()
                               .build();
            }
            case Block.Try(
                    List<Resource> resources, Block.Simple(List<? extends Statement> statements),
                    List<Block.Try.Catch> catchBlocks,
                    Block.Simple(List<? extends Statement> finallyStatements)
            ) -> {

                CodeBlock resourcesBlock = resources.stream().map(resource -> switch (resource) {
                    case Expression.Variable(String identifier) -> CodeBlock.of("$L", identifier);
                    case Resource.ResourceDeclaration(
                            TypeValue.Declarable type, String identifier, Expression initializer
                    ) -> CodeBlock.of("$T $L = $L", convertToTypeName(type), identifier,
                                      convertExpressionToCodeBlock(initializer));
                }).collect(CodeBlock.joining("; "));

                CodeBlock.Builder builder = CodeBlock.builder();
                if (!resourcesBlock.isEmpty()) {
                    builder.beginControlFlow("try ($L)", resourcesBlock);
                } else {
                    builder.beginControlFlow("try");
                }

                CodeBlock bodyBlock = statements.stream()
                                                .map(CodeBlockConverter::convertStatementToCodeBlock)
                                                .collect(CodeBlock.joining(""));

                builder.add(bodyBlock);

                for (Block.Try.Catch catchBlock : catchBlocks) {
                    CodeBlock exceptionTypesBlock = catchBlock.exceptionTypes()
                                                              .stream()
                                                              .map(CodeBlockConverter::convertToTypeName)
                                                              .map(typeName -> CodeBlock.of("$T", typeName))
                                                              .collect(CodeBlock.joining(" | "));

                    CodeBlock catchCodeBlock = catchBlock.body()
                                                         .statements()
                                                         .stream()
                                                         .map(CodeBlockConverter::convertStatementToCodeBlock)
                                                         .collect(CodeBlock.joining(""));

                    builder.nextControlFlow("catch ($L $L)", exceptionTypesBlock, catchBlock.identifier())
                           .add(catchCodeBlock);
                }

                if (!finallyStatements.isEmpty()) {
                    builder.nextControlFlow("finally");
                    CodeBlock finallyBlock = finallyStatements.stream()
                                                              .map(CodeBlockConverter::convertStatementToCodeBlock)
                                                              .collect(CodeBlock.joining(""));
                    builder.add(finallyBlock);
                } else {
                    builder.endControlFlow();
                }

                yield builder.build();
            }
            case StatementExpression statementExpression -> convertExpressionToCodeBlock(statementExpression);
        };
    }

    private static CodeBlock convertStatementExpressionToCodeBlock(StatementExpression statementExpression) {
        return switch (statementExpression) {
            case StatementExpression.Assignment(Expression.Assignable receiver, Expression value) ->
                    CodeBlock.of("$L = $L", convertExpressionToCodeBlock(receiver),
                                 convertExpressionToCodeBlock(value));
            case StatementExpression.NewInstance(TypeValue.Declarable type, List<? extends Expression> args) -> {
                CodeBlock argsBlock = args.stream()
                                          .map(CodeBlockConverter::convertExpressionToCodeBlock)
                                          .collect(CodeBlock.joining(", "));
                yield CodeBlock.of("new $T($L)", convertToTypeName(type), argsBlock);
            }
            case StatementExpression.MethodCall(
                    Expression receiver, String methodName, List<? extends Expression> args
            ) -> {
                CodeBlock argsBlock = args.stream()
                                          .map(CodeBlockConverter::convertExpressionToCodeBlock)
                                          .collect(CodeBlock.joining(", "));
                yield CodeBlock.of("$L.$L($L)", convertExpressionToCodeBlock(receiver), methodName, argsBlock);
            }
            case StatementExpression.PostDecrement(Expression.Assignable receiver) ->
                    CodeBlock.of("$L--", convertExpressionToCodeBlock(receiver));
            case StatementExpression.PostIncrement(Expression.Assignable receiver) ->
                    CodeBlock.of("$L++", convertExpressionToCodeBlock(receiver));
            case StatementExpression.PreDecrement(Expression.Assignable receiver) ->
                    CodeBlock.of("--$L", convertExpressionToCodeBlock(receiver));
            case StatementExpression.PreIncrement(Expression.Assignable receiver) ->
                    CodeBlock.of("++$L", convertExpressionToCodeBlock(receiver));
        };
    }
}
