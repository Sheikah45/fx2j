package io.github.sheikah45.fx2j.parser.utils;

import io.github.sheikah45.fx2j.parser.internal.utils.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
class StringUtilsTest {

    @Test
    void isNullOrBlankTest() {
        assertTrue(StringUtils.isNullOrBlank(null));
        assertTrue(StringUtils.isNullOrBlank(""));
        assertTrue(StringUtils.isNullOrBlank(" "));
        assertFalse(StringUtils.isNullOrBlank("a"));
    }
}