package io.github.sheikah45.fx2j.processor.internal.utils;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;
import io.github.sheikah45.fx2j.processor.internal.code.CodeType;
import io.github.sheikah45.fx2j.processor.internal.code.CodeValue;

import java.util.List;

public class CodeBlockUtils {

    private static TypeName convertToTypeName(CodeType type) {
        return switch (type) {
            case CodeType.Raw.Primitive(String primitive) -> convertPrimitiveToTypeName(primitive);
            case CodeType.Raw.Array array -> convertToArrayName(array);
            case CodeType.Raw raw -> convertToClassName(raw);
            case CodeType.Parameterized(CodeType.Raw rawType, List<CodeType> arguments) ->
                    ParameterizedTypeName.get(convertToClassName(rawType), arguments.stream()
                                                                                    .map(CodeBlockUtils::convertToTypeName)
                                                                                    .toArray(TypeName[]::new));
            case CodeType.Variable(String name, List<CodeType> upperBounds) -> TypeVariableName.get(name,
                                                                                                    upperBounds.stream()
                                                                                                               .map(CodeBlockUtils::convertToTypeName)
                                                                                                               .toArray(
                                                                                                                       TypeName[]::new));
            case CodeType.Wildcard(List<CodeType> lowerBounds, List<CodeType> upperBounds) when lowerBounds.size() ==
                                                                                                1 &&
                                                                                                upperBounds.isEmpty() ->
                    WildcardTypeName.supertypeOf(convertToTypeName(lowerBounds.getFirst()));
            case CodeType.Wildcard(List<CodeType> lowerBounds, List<CodeType> upperBounds) when upperBounds.size() ==
                                                                                                1 &&
                                                                                                lowerBounds.isEmpty() ->
                    WildcardTypeName.subtypeOf(convertToTypeName(upperBounds.getFirst()));
            case CodeType.Wildcard ignored -> throw new UnsupportedOperationException(
                    "Cannot generate wildcard typeName with multiple bound parameters");
        };
    }

    private static ClassName convertToClassName(CodeType.Raw rawType) {
        return switch (rawType) {
            case CodeType.Raw.Array ignored ->
                    throw new UnsupportedOperationException("Cannot convert array type to ClassName");
            case CodeType.Raw.Primitive ignored ->
                    throw new UnsupportedOperationException("Cannot convert primitive type to ClassName");
            case CodeType.Raw.TopLevel(String packageName, String simpleName) -> ClassName.get(packageName, simpleName);
            case CodeType.Raw.Nested(CodeType.Raw ownerType, String simpleName) -> {
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

    private static ArrayTypeName convertToArrayName(CodeType.Raw.Array arrayType) {
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

    public static CodeBlock convertExpressionToCodeBlock(CodeValue.Expression codeValue) {
        return switch (codeValue) {
            case CodeValue.Literal.Null() -> CodeBlock.of("null");
            case CodeValue.Literal.Bool(boolean value) -> CodeBlock.of("$L", value);
            case CodeValue.Literal.Char(char value) -> CodeBlock.of("$L", value);
            case CodeValue.Literal.Byte(byte value) -> CodeBlock.of("$L", value);
            case CodeValue.Literal.Short(short value) -> CodeBlock.of("$L", value);
            case CodeValue.Literal.Int(int value) -> CodeBlock.of("$L", value);
            case CodeValue.Literal.Long(long value) -> CodeBlock.of("$L", value);
            case CodeValue.Literal.Float(float value) -> CodeBlock.of("$L", value);
            case CodeValue.Literal.Double(double value) -> CodeBlock.of("$L", value);
            case CodeValue.Literal.Str(String value) -> CodeBlock.of("$S", value);
            case CodeValue.Variable(String value) -> CodeBlock.of("$L", value);
            case CodeValue.Type(CodeType type) -> CodeBlock.of("$T", convertToTypeName(type));
            case CodeValue.Enum(Enum<?> value) -> CodeBlock.of("$T.$L", value.getDeclaringClass(), value.name());
            case CodeValue.FieldAccess(CodeValue.Expression receiver, String field) ->
                    CodeBlock.of("$L.$L", convertExpressionToCodeBlock(receiver), field);
            case CodeValue.Array.Declared(CodeType componentType, List<? extends CodeValue.Expression> values) -> {
                CodeBlock valuesBlock = values.stream()
                                              .map(CodeBlockUtils::convertExpressionToCodeBlock)
                                              .collect(CodeBlock.joining(", "));
                yield CodeBlock.of("new $T[]{$L}", convertToTypeName(componentType), valuesBlock);
            }
            case CodeValue.Array.Sized(CodeType componentType, int size) ->
                    CodeBlock.of("new $T[$L]", componentType, size);
            case CodeValue.NewInstance(CodeType.Declarable type, List<? extends CodeValue.Expression> args) -> {
                CodeBlock argsBlock = args.stream()
                                          .map(CodeBlockUtils::convertExpressionToCodeBlock)
                                          .collect(CodeBlock.joining(", "));
                yield CodeBlock.of("new $T($L)", convertToTypeName(type), argsBlock);
            }
            case CodeValue.Lambda.MethodReference(CodeValue.Expression receiver, String methodName) ->
                    CodeBlock.of("$L::$L", convertExpressionToCodeBlock(receiver), methodName);
            case CodeValue.Lambda.Arrow.Typed(
                    List<CodeValue.Parameter> parameters,
                    CodeValue.Block(List<? extends CodeValue.Statement> statements)
            ) -> {
                CodeBlock paramBlock = parameters.stream()
                                                 .map(parameter -> CodeBlock.of("$T $L",
                                                                                convertToTypeName(parameter.type()),
                                                                                parameter.identifier()))
                                                 .collect(CodeBlock.joining(", "));
                yield convertToLambda(paramBlock, statements);
            }
            case CodeValue.Lambda.Arrow.Untyped(
                    List<String> parameters, CodeValue.Block(List<? extends CodeValue.Statement> statements)
            ) -> {
                CodeBlock paramBlock = parameters.stream()
                                                 .map(parameter -> CodeBlock.of("$L", parameter))
                                                 .collect(CodeBlock.joining(", "));
                yield convertToLambda(paramBlock, statements);
            }
            case CodeValue.ArrayAccess(CodeValue.Expression receiver, CodeValue.Expression accessor) ->
                    CodeBlock.of("$L[$L]", convertExpressionToCodeBlock(receiver),
                                 convertExpressionToCodeBlock(accessor));
            case CodeValue.StatementExpression statementExpression ->
                    convertStatementExpressionToCodeBlock(statementExpression);
        };
    }

    private static CodeBlock convertToLambda(CodeBlock paramBlock, List<? extends CodeValue.Statement> body) {
        if (body.isEmpty()) {
            return CodeBlock.of("($L) -> {}", paramBlock);
        } else if (body.size() == 1) {
            CodeBlock bodyBlock = switch (body.getFirst()) {
                case CodeValue.Return.Void() -> CodeBlock.of("{}");
                case CodeValue.Return.Value(CodeValue.Expression value) -> convertExpressionToCodeBlock(value);
                case CodeValue.BlockStatement value -> CodeBlock.builder()
                                                                .beginControlFlow("")
                                                                .add(convertStatementToUnterminatedCodeBlock(value))
                                                                .endControlFlow()
                                                                .build();
                case CodeValue.Statement value -> convertStatementToUnterminatedCodeBlock(value);
            };
            return CodeBlock.of("($L) -> $L", paramBlock, bodyBlock);
        } else {
            CodeBlock bodyBlock = body.stream()
                                      .map(CodeBlockUtils::convertStatementToCodeBlock)
                                      .map(codeBlock -> CodeBlock.builder().add("\t").add(codeBlock).build())
                                      .collect(CodeBlock.joining("\n"));
            return CodeBlock.builder().beginControlFlow("($L) -> ", paramBlock).add(bodyBlock).endControlFlow().build();
        }
    }

    public static CodeBlock convertStatementToCodeBlock(CodeValue.Statement codeValue) {
        CodeBlock codeBlock = convertStatementToUnterminatedCodeBlock(codeValue);
        return switch (codeValue) {
            case CodeValue.BlockStatement ignored -> CodeBlock.builder().add(codeBlock).build();
            case CodeValue.LineBreak ignored -> CodeBlock.of("\n");
            default -> CodeBlock.builder().add(codeBlock).add(";\n").build();
        };
    }

    private static CodeBlock convertStatementToUnterminatedCodeBlock(CodeValue.Statement codeValue) {
        return switch (codeValue) {
            case CodeValue.Declaration(CodeType.Declarable type, List<? extends CodeValue.Declarator> declarators) -> {
                CodeBlock declaratorsBlock = declarators.stream().map(declarator -> switch (declarator) {
                    case CodeValue.Assignment<?>(CodeValue.Assignable receiver, CodeValue.Expression initializer) ->
                            CodeBlock.of("$L = $L", convertExpressionToCodeBlock(receiver),
                                         convertExpressionToCodeBlock(initializer));
                    case CodeValue.Variable(String identifier) -> CodeBlock.of("$L", identifier);
                }).collect(CodeBlock.joining(", "));
                yield CodeBlock.of("$T $L", convertToTypeName(type), declaratorsBlock);
            }
            case CodeValue.Assignment(CodeValue.Assignable identifier, CodeValue.Expression value) ->
                    CodeBlock.of("$L = $L", convertExpressionToCodeBlock(identifier),
                                 convertExpressionToCodeBlock(value));
            case CodeValue.Return.Value(CodeValue.Expression value) ->
                    CodeBlock.of("return $L", convertExpressionToCodeBlock(value));
            case CodeValue.Return.Void() -> CodeBlock.of("return");
            case CodeValue.Continue.Unlabeled() -> CodeBlock.of("continue");
            case CodeValue.Continue.Labeled(String label) -> CodeBlock.of("continue $L", label);
            case CodeValue.Break.Unlabeled() -> CodeBlock.of("break");
            case CodeValue.Break.Labeled(String label) -> CodeBlock.of("break $L", label);
            case CodeValue.Throw(CodeValue.Expression exception) ->
                    CodeBlock.of("throw $L", convertExpressionToCodeBlock(exception));
            case CodeValue.LineBreak() -> CodeBlock.of("\n");
            case CodeValue.For.Loop(
                    CodeValue.Expression initializer, CodeValue.Expression termination,
                    List<? extends CodeValue.Expression> incrementors,
                    CodeValue.Block(List<? extends CodeValue.Statement> statements)
            ) -> {
                CodeBlock incrementorBlock = incrementors.stream()
                                                         .map(CodeBlockUtils::convertExpressionToCodeBlock)
                                                         .collect(CodeBlock.joining(", "));
                CodeBlock bodyBlock = statements.stream()
                                                .map(CodeBlockUtils::convertStatementToCodeBlock)
                                                .collect(CodeBlock.joining(""));

                yield CodeBlock.builder()
                               .beginControlFlow("for ($L; $L; $L)", convertExpressionToCodeBlock(initializer),
                                                 convertExpressionToCodeBlock(termination), incrementorBlock)
                               .add(bodyBlock)
                               .endControlFlow()
                               .build();
            }
            case CodeValue.For.Each(
                    CodeValue.Parameter(CodeType.Declarable type, String identifier), CodeValue.Expression parameters,
                    CodeValue.Block(List<? extends CodeValue.Statement> statements)
            ) -> {
                CodeBlock bodyBlock = statements.stream()
                                                .map(CodeBlockUtils::convertStatementToCodeBlock)
                                                .collect(CodeBlock.joining(""));

                yield CodeBlock.builder()
                               .beginControlFlow("for ($T $L : $L)", convertToTypeName(type), identifier,
                                                 convertExpressionToCodeBlock(parameters))
                               .add(bodyBlock)
                               .endControlFlow()
                               .build();
            }
            case CodeValue.Try(
                    List<CodeValue.Resource> resources, CodeValue.Block(List<? extends CodeValue.Statement> statements),
                    List<CodeValue.Catch> catchBlocks,
                    CodeValue.Block(List<? extends CodeValue.Statement> finallyStatements)
            ) -> {

                CodeBlock resourcesBlock = resources.stream().map(resource -> switch (resource) {
                    case CodeValue.Variable(String identifier) -> CodeBlock.of("$L", identifier);
                    case CodeValue.ResourceDeclaration(
                            CodeType.Declarable type, String identifier, CodeValue.Expression initializer
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
                                                .map(CodeBlockUtils::convertStatementToCodeBlock)
                                                .collect(CodeBlock.joining(""));

                builder.add(bodyBlock);

                for (CodeValue.Catch catchBlock : catchBlocks) {
                    CodeBlock exceptionTypesBlock = catchBlock.exceptionTypes()
                                                              .stream()
                                                              .map(CodeBlockUtils::convertToTypeName)
                                                              .map(typeName -> CodeBlock.of("$T", typeName))
                                                              .collect(CodeBlock.joining(" | "));

                    CodeBlock catchCodeBlock = catchBlock.body()
                                                         .statements()
                                                         .stream()
                                                         .map(CodeBlockUtils::convertStatementToCodeBlock)
                                                         .collect(CodeBlock.joining(""));

                    builder.nextControlFlow("catch ($L $L)", exceptionTypesBlock, catchBlock.identifier())
                           .add(catchCodeBlock);
                }

                if (!finallyStatements.isEmpty()) {
                    builder.nextControlFlow("finally");
                    CodeBlock finallyBlock = finallyStatements.stream()
                                                              .map(CodeBlockUtils::convertStatementToCodeBlock)
                                                              .collect(CodeBlock.joining(""));
                    builder.add(finallyBlock);
                } else {
                    builder.endControlFlow();
                }

                yield builder.build();
            }
            case CodeValue.StatementExpression statementExpression -> convertExpressionToCodeBlock(statementExpression);
        };
    }

    private static CodeBlock convertStatementExpressionToCodeBlock(CodeValue.StatementExpression statementExpression) {
        return switch (statementExpression) {
            case CodeValue.Assignment(CodeValue.Assignable receiver, CodeValue.Expression value) ->
                    CodeBlock.of("$L = $L", convertExpressionToCodeBlock(receiver),
                                 convertExpressionToCodeBlock(value));
            case CodeValue.NewInstance(CodeType.Declarable type, List<? extends CodeValue.Expression> args) -> {
                CodeBlock argsBlock = args.stream()
                                          .map(CodeBlockUtils::convertExpressionToCodeBlock)
                                          .collect(CodeBlock.joining(", "));
                yield CodeBlock.of("new $T($L)", convertToTypeName(type), argsBlock);
            }
            case CodeValue.MethodCall(
                    CodeValue.Expression receiver, String methodName, List<? extends CodeValue.Expression> args
            ) -> {
                CodeBlock argsBlock = args.stream()
                                          .map(CodeBlockUtils::convertExpressionToCodeBlock)
                                          .collect(CodeBlock.joining(", "));
                yield CodeBlock.of("$L.$L($L)", convertExpressionToCodeBlock(receiver), methodName, argsBlock);
            }
            case CodeValue.PostDecrement(CodeValue.Assignable receiver) ->
                    CodeBlock.of("$L--", convertExpressionToCodeBlock(receiver));
            case CodeValue.PostIncrement(CodeValue.Assignable receiver) ->
                    CodeBlock.of("$L++", convertExpressionToCodeBlock(receiver));
            case CodeValue.PreDecrement(CodeValue.Assignable receiver) ->
                    CodeBlock.of("--$L", convertExpressionToCodeBlock(receiver));
            case CodeValue.PreIncrement(CodeValue.Assignable receiver) ->
                    CodeBlock.of("++$L", convertExpressionToCodeBlock(receiver));
        };
    }
}
