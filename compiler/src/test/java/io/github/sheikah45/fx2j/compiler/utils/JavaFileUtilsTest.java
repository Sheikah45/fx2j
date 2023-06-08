package io.github.sheikah45.fx2j.compiler.utils;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.CONCURRENT)
public class JavaFileUtilsTest {

    @Test
    public void testGetCanonicalClassName() {
        assertEquals("test.test.Test", JavaFileUtils.getCanonicalClassName(JavaFile.builder("test.test", TypeSpec.classBuilder("Test")
                                                                                                                 .build())
                                                                                   .build()));
    }
}
