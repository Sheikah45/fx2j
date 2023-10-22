package io.github.sheikah45.fx2j.processor.internal.utils;

import com.squareup.javapoet.JavaFile;

public class JavaFileUtils {

    public static String getCanonicalClassName(JavaFile javaFile) {
        return javaFile.packageName + "." + javaFile.typeSpec.name;
    }

}
