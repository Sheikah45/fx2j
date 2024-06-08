package io.github.sheikah45.fx2j.processor.internal.code;

import io.github.sheikah45.fx2j.processor.internal.code.builder.LambdaBuilder;
import io.github.sheikah45.fx2j.processor.internal.code.builder.TryBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public final class CodeValues {

    private static final CodeValue.LineBreak LINE_BREAK = new CodeValue.LineBreak();
    private static final CodeValue.Literal.Null NULL = new CodeValue.Literal.Null();
    private static final CodeValue.Block EMPTY_BLOCK = new CodeValue.Block(List.of());
    private static final CodeValue.Return.Void VOID_RETURN = new CodeValue.Return.Void();

    private CodeValues() {}

    public static CodeValue.LineBreak lineBreak() {
        return LINE_BREAK;
    }

    public static CodeValue.Block block(CodeValue.Statement... statements) {
        return block(List.of(statements));
    }

    public static CodeValue.Block block(List<? extends CodeValue.Statement> statements) {
        if (statements.isEmpty()) {
            return EMPTY_BLOCK;
        }

        return new CodeValue.Block(statements);
    }

    public static CodeValue.Parameter parameter(Type type, String identifier) {
        if (!(CodeTypes.of(type) instanceof CodeType.Declarable declarable)) {
            throw new IllegalArgumentException("Type %s is not declarable".formatted(type));
        }
        return parameter(declarable, identifier);
    }

    public static CodeValue.Parameter parameter(CodeType.Declarable type, String identifier) {
        return new CodeValue.Parameter(type, identifier);
    }

    public static CodeValue.Enum enumValue(java.lang.Enum<?> value) {
        return new CodeValue.Enum(value);
    }

    public static CodeValue.FieldAccess fieldAccess(CodeValue.Expression receiver, String field) {
        return new CodeValue.FieldAccess(receiver, field);
    }

    public static CodeValue.FieldAccess fieldAccess(String receiver, String field) {
        return new CodeValue.FieldAccess(literal(receiver), field);
    }

    public static CodeValue.Literal.Str literal(String value) {
        return new CodeValue.Literal.Str(value);
    }

    public static CodeValue.FieldAccess fieldAccess(Type receiver, String field) {
        return new CodeValue.FieldAccess(type(receiver), field);
    }

    public static CodeValue.Type type(Type codeType) {
        return type(CodeTypes.of(codeType));
    }

    public static CodeValue.Type type(CodeType codeType) {
        return new CodeValue.Type(codeType);
    }

    public static CodeValue.FieldAccess fieldAccess(CodeType.Declarable receiver, String field) {
        return new CodeValue.FieldAccess(type(receiver), field);
    }

    public static CodeValue.FieldAccess fieldAccess(String receiver, Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            throw new IllegalArgumentException("Field %s is static".formatted(field));
        }
        return new CodeValue.FieldAccess(variable(receiver), field.getName());
    }

    public static CodeValue.Variable variable(String identifier) {
        return new CodeValue.Variable(identifier);
    }

    public static CodeValue.FieldAccess fieldAccess(Field field) {
        if (!Modifier.isStatic(field.getModifiers())) {
            throw new IllegalArgumentException("Field %s is not static".formatted(field));
        }
        return new CodeValue.FieldAccess(type(field.getDeclaringClass()), field.getName());
    }

    public static CodeValue.Array.Declared array(Type componentType, Object... values) {
        if (!(CodeTypes.of(componentType) instanceof CodeType.Declarable declarable)) {
            throw new IllegalArgumentException("Type %s is not declarable".formatted(componentType));
        }
        return array(declarable, values);
    }

    public static CodeValue.Array.Declared array(CodeType.Declarable componentType, Object... values) {
        return new CodeValue.Array.Declared(componentType, Arrays.stream(values).map(CodeValues::of).toList());
    }

    public static CodeValue.Expression of(Object value) {
        return switch (value) {
            case CodeValue.Expression expression -> expression;
            case Boolean val -> literal(val);
            case Byte val -> literal(val);
            case Short val -> literal(val);
            case Integer val -> literal(val);
            case Long val -> literal(val);
            case Float val -> literal(val);
            case Double val -> literal(val);
            case Character val -> literal(val);
            case String val -> literal(val);
            case null -> nullValue();
            default -> throw new IllegalArgumentException("Cannot create literal from %s".formatted(value));
        };
    }

    public static CodeValue.Literal.Bool literal(boolean value) {
        return new CodeValue.Literal.Bool(value);
    }

    public static CodeValue.Literal.Byte literal(byte value) {
        return new CodeValue.Literal.Byte(value);
    }

    public static CodeValue.Literal.Short literal(short value) {
        return new CodeValue.Literal.Short(value);
    }

    public static CodeValue.Literal.Int literal(int value) {
        return new CodeValue.Literal.Int(value);
    }

    public static CodeValue.Literal.Long literal(long value) {
        return new CodeValue.Literal.Long(value);
    }

    public static CodeValue.Literal.Float literal(float value) {
        return new CodeValue.Literal.Float(value);
    }

    public static CodeValue.Literal.Double literal(double value) {
        return new CodeValue.Literal.Double(value);
    }

    public static CodeValue.Literal.Char literal(char value) {
        return new CodeValue.Literal.Char(value);
    }

    public static CodeValue.Literal.Null nullValue() {
        return NULL;
    }

    public static CodeValue.Array.Declared array(String qualifiedComponentTypeName, Object... values) {
        int index = qualifiedComponentTypeName.lastIndexOf(".");
        return array(new CodeType.Raw.TopLevel(qualifiedComponentTypeName.substring(0, index),
                                               qualifiedComponentTypeName.substring(index + 1)), values);
    }

    @SafeVarargs
    public static <T> CodeValue.Array.Declared array(T... values) {
        return array(CodeTypes.of(values.getClass().getComponentType()), (Object[]) values);
    }

    public static CodeValue.Array.Sized array(Type componentType, int size) {
        if (!(CodeTypes.of(componentType) instanceof CodeType.Declarable declarable)) {
            throw new IllegalArgumentException("Type %s is not declarable".formatted(componentType));
        }
        return array(declarable, size);
    }

    public static CodeValue.Array.Sized array(CodeType.Declarable componentType, int size) {
        return new CodeValue.Array.Sized(componentType, size);
    }

    public static CodeValue.Array.Sized array(String qualifiedComponentTypeName, int size) {
        int index = qualifiedComponentTypeName.lastIndexOf(".");
        return array(new CodeType.Raw.TopLevel(qualifiedComponentTypeName.substring(0, index),
                                               qualifiedComponentTypeName.substring(index + 1)), size);
    }

    public static CodeValue.Lambda.MethodReference methodReference(CodeType.Declarable type, String methodName) {
        return methodReference(type(type), methodName);
    }

    public static CodeValue.Lambda.MethodReference methodReference(CodeValue.Expression receiver, String methodName) {
        return new CodeValue.Lambda.MethodReference(receiver, methodName);
    }

    public static CodeValue.Lambda.MethodReference methodReference(String identifier, Method method) {
        return methodReference(variable(identifier), method.getName());
    }

    public static CodeValue.Lambda.MethodReference methodReference(String identifier, String methodName) {
        return methodReference(variable(identifier), methodName);
    }

    public static CodeValue.Lambda.MethodReference methodReference(Method method) {
        return methodReference(method.getDeclaringClass(), method.getName());
    }

    public static CodeValue.Lambda.MethodReference methodReference(Type type, String methodName) {
        return methodReference(type(type), methodName);
    }

    public static LambdaBuilder lambdaBuilder() {
        return new LambdaBuilder();
    }

    public static CodeValue.NewInstance newInstance(String className, Object... args) {
        return newInstance(CodeTypes.of(className), args);
    }

    public static CodeValue.NewInstance newInstance(CodeType.Declarable type, Object... args) {
        return new CodeValue.NewInstance(type, Arrays.stream(args).map(CodeValues::of).toList());
    }

    public static CodeValue.NewInstance newInstance(Constructor<?> constructor, Object... args) {
        if (constructor.getParameters().length != args.length) {
            throw new IllegalArgumentException("Provided number of args does not match constructor number of args");
        }
        return newInstance(CodeTypes.of(constructor.getDeclaringClass()), args);
    }

    public static CodeValue.MethodCall methodCall(String receiver, Method method, Object... args) {
        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Method %s is static".formatted(method));
        }
        if (method.getParameters().length != args.length) {
            throw new IllegalArgumentException("Provided number of args does not match method number of args");
        }
        return methodCall(receiver, method.getName(), args);
    }

    public static CodeValue.MethodCall methodCall(String receiver, String methodName, Object... args) {
        return methodCall(variable(receiver), methodName, args);
    }

    public static CodeValue.MethodCall methodCall(CodeValue.Expression receiver, String methodName, Object... args) {
        return new CodeValue.MethodCall(receiver, methodName, Arrays.stream(args).map(CodeValues::of).toList());
    }

    public static CodeValue.MethodCall methodCall(Type type, Method method, Object... args) {
        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Method %s is static".formatted(method));
        }
        if (method.getParameters().length != args.length) {
            throw new IllegalArgumentException("Provided number of args does not match method number of args");
        }
        return methodCall(type, method.getName(), args);
    }

    public static CodeValue.MethodCall methodCall(Type type, String methodName, Object... args) {
        return methodCall(type(type), methodName, args);
    }

    public static CodeValue.MethodCall methodCall(CodeValue.Expression receiver, Method method, Object... args) {
        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Method %s is static".formatted(method));
        }
        if (method.getParameters().length != args.length) {
            throw new IllegalArgumentException("Provided number of args does not match method number of args");
        }
        return methodCall(receiver, method.getName(), args);
    }

    public static CodeValue.MethodCall methodCall(Method method, Object... args) {
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Method %s is not static".formatted(method));
        }
        if (method.getParameters().length != args.length) {
            throw new IllegalArgumentException("Provided number of args does not match method number of args");
        }
        return methodCall(type(method.getDeclaringClass()), method.getName(), args);
    }

    public static CodeValue.Declaration declaration(Type type, java.lang.String identifier) {
        if (!(CodeTypes.of(type) instanceof CodeType.Declarable declarable)) {
            throw new IllegalArgumentException("Type %s is not declarable".formatted(type));
        }
        return declaration(declarable, identifier);
    }

    public static CodeValue.Declaration declaration(CodeType.Declarable type, java.lang.String identifier) {
        return declaration(type, variable(identifier));
    }

    public static CodeValue.Declaration declaration(CodeType.Declarable type, CodeValue.Declarator... declarators) {
        return new CodeValue.Declaration(type, List.of(declarators));
    }

    public static CodeValue.Declaration declaration(Type type, java.lang.String identifier, Object initializer) {
        if (!(CodeTypes.of(type) instanceof CodeType.Declarable declarable)) {
            throw new IllegalArgumentException("Type %s is not declarable".formatted(type));
        }
        return declaration(declarable, identifier, of(initializer));
    }

    public static CodeValue.Declaration declaration(String className, java.lang.String identifier, Object initializer) {
        return declaration(CodeTypes.of(className), assignment(identifier, initializer));
    }

    public static CodeValue.Assignment<CodeValue.Variable> assignment(String identifier, Object value) {
        return assignment(variable(identifier), value);
    }

    public static <T extends CodeValue.Assignable> CodeValue.Assignment<T> assignment(T receiver, Object value) {
        return new CodeValue.Assignment<>(receiver, of(value));
    }

    public static CodeValue.Declaration declaration(CodeType.Declarable type, java.lang.String identifier,
                                                    Object initializer) {
        return declaration(type, assignment(identifier, initializer));
    }

    public static CodeValue.Declaration declaration(Type type, CodeValue.Variable variable) {
        if (!(CodeTypes.of(type) instanceof CodeType.Declarable declarable)) {
            throw new IllegalArgumentException("Type %s is not declarable".formatted(type));
        }
        return declaration(declarable, variable);
    }

    public static CodeValue.Declaration declaration(Type type, CodeValue.Variable variable, Object initializer) {
        if (!(CodeTypes.of(type) instanceof CodeType.Declarable declarable)) {
            throw new IllegalArgumentException("Type %s is not declarable".formatted(type));
        }
        return declaration(declarable, variable, of(initializer));
    }

    public static CodeValue.Declaration declaration(String className, CodeValue.Variable variable, Object initializer) {
        return declaration(CodeTypes.of(className), assignment(variable, initializer));
    }

    public static CodeValue.Declaration declaration(CodeType.Declarable type, CodeValue.Variable variable,
                                                    Object initializer) {
        return declaration(type, assignment(variable, initializer));
    }

    public static CodeValue.Try rethrow(CodeValue.Statement statement) {
        CodeValue.Throw throwsException = CodeValues.throwsException(RuntimeException.class,
                                                                     CodeValues.variable("exception"));
        return CodeValues.tryBuilder()
                         .body(body -> body.statement(statement))
                         .catches(catchBuilder -> catchBuilder.exception(Exception.class)
                                                              .body(body -> body.statement(throwsException)))
                         .build();
    }

    public static CodeValue.Throw throwsException(Type type, Object... args) {
        if (!(CodeTypes.of(type) instanceof CodeType.Declarable declarable)) {
            throw new IllegalArgumentException("Type %s is not declarable".formatted(type));
        }

        return throwsException(newInstance(type, args));
    }

    public static TryBuilder tryBuilder() {
        return new TryBuilder();
    }

    public static CodeValue.Throw throwsException(CodeValue.Expression expression) {
        return new CodeValue.Throw(expression);
    }

    public static CodeValue.NewInstance newInstance(Type type, Object... args) {
        if (!(CodeTypes.of(type) instanceof CodeType.Declarable declarable)) {
            throw new IllegalArgumentException("Type %s is not declarable".formatted(type));
        }
        return newInstance(declarable, args);
    }

    public static CodeValue.Try rethrow(CodeValue.Block block) {
        String exceptionIdentifier = "exception";
        CodeValue.Throw throwsException = CodeValues.throwsException(RuntimeException.class,
                                                                     CodeValues.variable(exceptionIdentifier));
        return CodeValues.tryBuilder()
                         .body(body -> body.block(block))
                         .catches(catchBuilder -> catchBuilder.identifier(exceptionIdentifier)
                                                              .exception(Exception.class)
                                                              .body(body -> body.statement(throwsException)))
                         .build();
    }

    public static CodeValue.Return.Value returns(CodeValue.Expression value) {
        return new CodeValue.Return.Value(value);
    }

    public static CodeValue.Return.Void returns() {
        return VOID_RETURN;
    }
}
