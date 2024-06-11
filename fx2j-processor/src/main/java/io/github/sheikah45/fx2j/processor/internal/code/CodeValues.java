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

    private static final Statement.LineBreak LINE_BREAK = new Statement.LineBreak();
    private static final Literal.Null NULL = new Literal.Null();
    private static final Block.Simple EMPTY_BLOCK = new Block.Simple(List.of());
    private static final Statement.Return.Void VOID_RETURN = new Statement.Return.Void();

    private CodeValues() {}

    public static Statement.LineBreak lineBreak() {
        return LINE_BREAK;
    }

    public static Block.Simple block(Statement... statements) {
        return block(List.of(statements));
    }

    public static Block.Simple block(List<? extends Statement> statements) {
        if (statements.isEmpty()) {
            return EMPTY_BLOCK;
        }

        return new Block.Simple(statements);
    }

    public static Parameter parameter(Type type, String identifier) {
        if (!(TypeValues.of(type) instanceof TypeValue.Declarable declarable)) {
            throw new IllegalArgumentException("Type %s is not declarable".formatted(type));
        }
        return parameter(declarable, identifier);
    }

    public static Parameter parameter(TypeValue.Declarable type, String identifier) {
        return new Parameter(type, identifier);
    }

    public static Expression.FieldAccess fieldAccess(Expression receiver, String field) {
        return new Expression.FieldAccess(receiver, field);
    }

    public static Expression.FieldAccess fieldAccess(String receiver, String field) {
        return new Expression.FieldAccess(literal(receiver), field);
    }

    public static Literal.Str literal(String value) {
        return new Literal.Str(value);
    }

    public static Expression.FieldAccess fieldAccess(Type receiver, String field) {
        return new Expression.FieldAccess(type(receiver), field);
    }

    public static Expression.Type type(Type codeType) {
        return type(TypeValues.of(codeType));
    }

    public static Expression.Type type(TypeValue codeType) {
        return new Expression.Type(codeType);
    }

    public static Expression.FieldAccess fieldAccess(TypeValue.Declarable receiver, String field) {
        return new Expression.FieldAccess(type(receiver), field);
    }

    public static Expression.FieldAccess fieldAccess(String receiver, Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            throw new IllegalArgumentException("Field %s is static".formatted(field));
        }
        return new Expression.FieldAccess(variable(receiver), field.getName());
    }

    public static Expression.Variable variable(String identifier) {
        return new Expression.Variable(identifier);
    }

    public static Expression.FieldAccess fieldAccess(Field field) {
        if (!Modifier.isStatic(field.getModifiers())) {
            throw new IllegalArgumentException("Field %s is not static".formatted(field));
        }
        return new Expression.FieldAccess(type(field.getDeclaringClass()), field.getName());
    }

    public static Expression.Array.Declared array(Type componentType, Object... values) {
        if (!(TypeValues.of(componentType) instanceof TypeValue.Declarable declarable)) {
            throw new IllegalArgumentException("Type %s is not declarable".formatted(componentType));
        }
        return array(declarable, values);
    }

    public static Expression.Array.Declared array(TypeValue.Declarable componentType, Object... values) {
        return new Expression.Array.Declared(componentType, Arrays.stream(values).map(CodeValues::of).toList());
    }

    public static Expression of(Object value) {
        return switch (value) {
            case Expression expression -> expression;
            case Boolean val -> literal(val);
            case Byte val -> literal(val);
            case Short val -> literal(val);
            case Integer val -> literal(val);
            case Long val -> literal(val);
            case Float val -> literal(val);
            case Double val -> literal(val);
            case Character val -> literal(val);
            case String val -> literal(val);
            case Enum<?> val -> enumValue(val);
            case null -> nullValue();
            default -> throw new IllegalArgumentException("Cannot create literal from %s".formatted(value));
        };
    }

    public static Literal.Bool literal(boolean value) {
        return new Literal.Bool(value);
    }

    public static Literal.Byte literal(byte value) {
        return new Literal.Byte(value);
    }

    public static Literal.Short literal(short value) {
        return new Literal.Short(value);
    }

    public static Literal.Int literal(int value) {
        return new Literal.Int(value);
    }

    public static Literal.Long literal(long value) {
        return new Literal.Long(value);
    }

    public static Literal.Float literal(float value) {
        return new Literal.Float(value);
    }

    public static Literal.Double literal(double value) {
        return new Literal.Double(value);
    }

    public static Literal.Char literal(char value) {
        return new Literal.Char(value);
    }

    public static Expression.Enum enumValue(java.lang.Enum<?> value) {
        return new Expression.Enum(value);
    }

    public static Literal.Null nullValue() {
        return NULL;
    }

    public static Expression.Array.Declared array(String qualifiedComponentTypeName, Object... values) {
        int index = qualifiedComponentTypeName.lastIndexOf(".");
        return array(new TypeValue.Raw.Top(qualifiedComponentTypeName.substring(0, index),
                                           qualifiedComponentTypeName.substring(index + 1)), values);
    }

    @SafeVarargs
    public static <T> Expression.Array.Declared array(T... values) {
        return array(TypeValues.of(values.getClass().getComponentType()), (Object[]) values);
    }

    public static Expression.Array.Sized array(Type componentType, int size) {
        if (!(TypeValues.of(componentType) instanceof TypeValue.Declarable declarable)) {
            throw new IllegalArgumentException("Type %s is not declarable".formatted(componentType));
        }
        return array(declarable, size);
    }

    public static Expression.Array.Sized array(TypeValue.Declarable componentType, int size) {
        return new Expression.Array.Sized(componentType, size);
    }

    public static Expression.Array.Sized array(String qualifiedComponentTypeName, int size) {
        int index = qualifiedComponentTypeName.lastIndexOf(".");
        return array(new TypeValue.Raw.Top(qualifiedComponentTypeName.substring(0, index),
                                           qualifiedComponentTypeName.substring(index + 1)), size);
    }

    public static Expression.Lambda.MethodReference methodReference(TypeValue.Declarable type, String methodName) {
        return methodReference(type(type), methodName);
    }

    public static Expression.Lambda.MethodReference methodReference(Expression receiver, String methodName) {
        return new Expression.Lambda.MethodReference(receiver, methodName);
    }

    public static Expression.Lambda.MethodReference methodReference(String identifier, Method method) {
        return methodReference(variable(identifier), method.getName());
    }

    public static Expression.Lambda.MethodReference methodReference(String identifier, String methodName) {
        return methodReference(variable(identifier), methodName);
    }

    public static Expression.Lambda.MethodReference methodReference(Method method) {
        return methodReference(method.getDeclaringClass(), method.getName());
    }

    public static Expression.Lambda.MethodReference methodReference(Type type, String methodName) {
        return methodReference(type(type), methodName);
    }

    public static LambdaBuilder lambdaBuilder() {
        return new LambdaBuilder();
    }

    public static StatementExpression.NewInstance newInstance(String className, Object... args) {
        return newInstance(TypeValues.of(className), args);
    }

    public static StatementExpression.NewInstance newInstance(TypeValue.Declarable type, Object... args) {
        return new StatementExpression.NewInstance(type, Arrays.stream(args).map(CodeValues::of).toList());
    }

    public static StatementExpression.NewInstance newInstance(Constructor<?> constructor, Object... args) {
        if (constructor.getParameters().length != args.length) {
            throw new IllegalArgumentException("Provided number of args does not match constructor number of args");
        }
        return newInstance(TypeValues.of(constructor.getDeclaringClass()), args);
    }

    public static StatementExpression.MethodCall methodCall(String receiver, Method method, Object... args) {
        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Method %s is static".formatted(method));
        }
        if (method.getParameters().length != args.length) {
            throw new IllegalArgumentException("Provided number of args does not match method number of args");
        }
        return methodCall(receiver, method.getName(), args);
    }

    public static StatementExpression.MethodCall methodCall(String receiver, String methodName, Object... args) {
        return methodCall(variable(receiver), methodName, args);
    }

    public static StatementExpression.MethodCall methodCall(Expression receiver, String methodName, Object... args) {
        return new StatementExpression.MethodCall(receiver, methodName,
                                                  Arrays.stream(args).map(CodeValues::of).toList());
    }

    public static StatementExpression.MethodCall methodCall(Type type, Method method, Object... args) {
        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Method %s is static".formatted(method));
        }
        if (method.getParameters().length != args.length) {
            throw new IllegalArgumentException("Provided number of args does not match method number of args");
        }
        return methodCall(type, method.getName(), args);
    }

    public static StatementExpression.MethodCall methodCall(Type type, String methodName, Object... args) {
        return methodCall(type(type), methodName, args);
    }

    public static StatementExpression.MethodCall methodCall(Expression receiver, Method method, Object... args) {
        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Method %s is static".formatted(method));
        }
        if (method.getParameters().length != args.length) {
            throw new IllegalArgumentException("Provided number of args does not match method number of args");
        }
        return methodCall(receiver, method.getName(), args);
    }

    public static StatementExpression.MethodCall methodCall(Method method, Object... args) {
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Method %s is not static".formatted(method));
        }
        if (method.getParameters().length != args.length) {
            throw new IllegalArgumentException("Provided number of args does not match method number of args");
        }
        return methodCall(type(method.getDeclaringClass()), method.getName(), args);
    }

    public static Statement.Declaration declaration(Type type, java.lang.String identifier) {
        if (!(TypeValues.of(type) instanceof TypeValue.Declarable declarable)) {
            throw new IllegalArgumentException("Type %s is not declarable".formatted(type));
        }
        return declaration(declarable, identifier);
    }

    public static Statement.Declaration declaration(TypeValue.Declarable type, java.lang.String identifier) {
        return declaration(type, variable(identifier));
    }

    public static Statement.Declaration declaration(TypeValue.Declarable type, Declarator... declarators) {
        return new Statement.Declaration(type, List.of(declarators));
    }

    public static Statement.Declaration declaration(Type type, java.lang.String identifier, Object initializer) {
        if (!(TypeValues.of(type) instanceof TypeValue.Declarable declarable)) {
            throw new IllegalArgumentException("Type %s is not declarable".formatted(type));
        }
        return declaration(declarable, identifier, of(initializer));
    }

    public static Statement.Declaration declaration(String className, java.lang.String identifier, Object initializer) {
        return declaration(TypeValues.of(className), assignment(identifier, initializer));
    }

    public static StatementExpression.Assignment<Expression.Variable> assignment(String identifier, Object value) {
        return assignment(variable(identifier), value);
    }

    public static <T extends Expression.Assignable> StatementExpression.Assignment<T> assignment(T receiver,
                                                                                                 Object value) {
        return new StatementExpression.Assignment<>(receiver, of(value));
    }

    public static Statement.Declaration declaration(TypeValue.Declarable type, java.lang.String identifier,
                                                    Object initializer) {
        return declaration(type, assignment(identifier, initializer));
    }

    public static Statement.Declaration declaration(Type type, Expression.Variable variable) {
        if (!(TypeValues.of(type) instanceof TypeValue.Declarable declarable)) {
            throw new IllegalArgumentException("Type %s is not declarable".formatted(type));
        }
        return declaration(declarable, variable);
    }

    public static Statement.Declaration declaration(Type type, Expression.Variable variable, Object initializer) {
        if (!(TypeValues.of(type) instanceof TypeValue.Declarable declarable)) {
            throw new IllegalArgumentException("Type %s is not declarable".formatted(type));
        }
        return declaration(declarable, variable, of(initializer));
    }

    public static Statement.Declaration declaration(String className, Expression.Variable variable,
                                                    Object initializer) {
        return declaration(TypeValues.of(className), assignment(variable, initializer));
    }

    public static Statement.Declaration declaration(TypeValue.Declarable type, Expression.Variable variable,
                                                    Object initializer) {
        return declaration(type, assignment(variable, initializer));
    }

    public static Block.Try rethrow(Statement statement) {
        Statement.Throw throwsException = CodeValues.throwsException(RuntimeException.class,
                                                                     CodeValues.variable("exception"));
        return CodeValues.tryBuilder()
                         .body(body -> body.statement(statement))
                         .catches(catchBuilder -> catchBuilder.exception(Exception.class)
                                                              .body(body -> body.statement(throwsException)))
                         .build();
    }

    public static Statement.Throw throwsException(Type type, Object... args) {
        if (!(TypeValues.of(type) instanceof TypeValue.Declarable declarable)) {
            throw new IllegalArgumentException("Type %s is not declarable".formatted(type));
        }

        return throwsException(newInstance(type, args));
    }

    public static TryBuilder tryBuilder() {
        return new TryBuilder();
    }

    public static Statement.Throw throwsException(Expression expression) {
        return new Statement.Throw(expression);
    }

    public static StatementExpression.NewInstance newInstance(Type type, Object... args) {
        if (!(TypeValues.of(type) instanceof TypeValue.Declarable declarable)) {
            throw new IllegalArgumentException("Type %s is not declarable".formatted(type));
        }
        return newInstance(declarable, args);
    }

    public static Block.Try rethrow(Block.Simple block) {
        String exceptionIdentifier = "exception";
        Statement.Throw throwsException = CodeValues.throwsException(RuntimeException.class,
                                                                     CodeValues.variable(exceptionIdentifier));
        return CodeValues.tryBuilder()
                         .body(body -> body.block(block))
                         .catches(catchBuilder -> catchBuilder.identifier(exceptionIdentifier)
                                                              .exception(Exception.class)
                                                              .body(body -> body.statement(throwsException)))
                         .build();
    }

    public static Statement.Return.Value returns(Expression value) {
        return new Statement.Return.Value(value);
    }

    public static Statement.Return.Void returns() {
        return VOID_RETURN;
    }
}
