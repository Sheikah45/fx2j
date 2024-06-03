package io.github.sheikah45.fx2j.processor.internal.resolve;

import java.util.Set;

public class ResolverContainer {

    private final TypeResolver typeResolver;
    private final MethodResolver methodResolver;
    private final NameResolver nameResolver;
    private final ValueResolver valueResolver;
    private final ExpressionResolver expressionResolver;

    private ResolverContainer(TypeResolver typeResolver, MethodResolver methodResolver, NameResolver nameResolver,
                              ValueResolver valueResolver, ExpressionResolver expressionResolver) {
        this.typeResolver = typeResolver;
        this.methodResolver = methodResolver;
        this.nameResolver = nameResolver;
        this.valueResolver = valueResolver;
        this.expressionResolver = expressionResolver;
    }

    public static ResolverContainer from(Set<String> imports, ClassLoader classLoader) {
        TypeResolver typeResolver = new TypeResolver(imports, classLoader);
        MethodResolver methodResolver = new MethodResolver(typeResolver);
        NameResolver nameResolver = new NameResolver(typeResolver);
        ValueResolver valueResolver = new ValueResolver(typeResolver, methodResolver, nameResolver);
        ExpressionResolver expressionResolver = new ExpressionResolver(typeResolver, methodResolver, nameResolver);
        return new ResolverContainer(typeResolver, methodResolver, nameResolver, valueResolver, expressionResolver);
    }

    public TypeResolver getTypeResolver() {
        return typeResolver;
    }

    public MethodResolver getMethodResolver() {
        return methodResolver;
    }

    public NameResolver getNameResolver() {
        return nameResolver;
    }

    public ValueResolver getValueResolver() {
        return valueResolver;
    }

    public ExpressionResolver getExpressionResolver() {
        return expressionResolver;
    }
}
