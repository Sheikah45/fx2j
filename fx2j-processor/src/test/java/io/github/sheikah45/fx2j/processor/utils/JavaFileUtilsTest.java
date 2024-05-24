package io.github.sheikah45.fx2j.processor.utils;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.github.sheikah45.fx2j.processor.internal.utils.JavaFileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.CONCURRENT)
class JavaFileUtilsTest {

    @Test
    void testGetCanonicalClassName() {
        assertEquals("test.test.Test", JavaFileUtils.getCanonicalClassName(JavaFile.builder("test.test", TypeSpec.classBuilder("Test")
                                                                                                                 .build())
                                                                                   .build()));
    }
}
