package io.github.sheikah45.fx2j.processor.internal.resolve;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Set;

@Execution(ExecutionMode.CONCURRENT)
abstract class AbstractResolverTest {

    protected final ResolverContainer resolverContainer = ResolverContainer.from(Set.of(), getClass().getClassLoader());

}
