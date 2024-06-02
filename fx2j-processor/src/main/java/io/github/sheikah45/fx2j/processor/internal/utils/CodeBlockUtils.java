package io.github.sheikah45.fx2j.processor.internal.utils;

import com.squareup.javapoet.CodeBlock;
import io.github.sheikah45.fx2j.processor.internal.model.CodeValue;

import java.lang.reflect.Type;
import java.util.List;

public class CodeBlockUtils {

    public static CodeBlock convertToCodeBlock(CodeValue codeValue) {
        return switch (codeValue) {
            case CodeValue.Null() -> CodeBlock.of("null");
            case CodeValue.Char(char value) -> CodeBlock.of("$L", value);
            case CodeValue.Literal(String value) -> CodeBlock.of("$L", value);
            case CodeValue.String(String value) -> CodeBlock.of("$S", value);
            case CodeValue.Type(Type type) -> CodeBlock.of("$T", type);
            case CodeValue.Enum(Enum<?> value) -> CodeBlock.of("$T.$L", value.getDeclaringClass(), value.name());
            case CodeValue.FieldAccess(CodeValue receiver, String field) ->
                    CodeBlock.of("$L.$L", convertToCodeBlock(receiver), field);
            case CodeValue.ArrayInitialization.Declared(Type componentType, List<CodeValue> values) -> {
                CodeBlock valuesBlock = values.stream()
                                              .map(CodeBlockUtils::convertToCodeBlock)
                                              .collect(CodeBlock.joining(", "));
                yield CodeBlock.of("new $T[]{$L}", componentType, valuesBlock);
            }
            case CodeValue.ArrayInitialization.Sized(Type componentType, int size) ->
                    CodeBlock.of("new $T[$L]", componentType, size);
            case CodeValue.MethodCall(CodeValue receiver, String methodName, List<CodeValue> args) -> {
                CodeBlock argsBlock = args.stream()
                                          .map(CodeBlockUtils::convertToCodeBlock)
                                          .collect(CodeBlock.joining(", "));
                yield CodeBlock.of("$L.$L($L)", convertToCodeBlock(receiver), methodName, argsBlock);
            }
            case CodeValue.Assignment(Type type, String identifier, CodeValue value) ->
                    CodeBlock.of("$T $L = $L", type, identifier, convertToCodeBlock(value));
        };
    }

}
