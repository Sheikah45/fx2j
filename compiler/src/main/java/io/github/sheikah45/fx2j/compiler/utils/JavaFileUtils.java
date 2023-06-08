package io.github.sheikah45.fx2j.compiler.utils;

import com.squareup.javapoet.JavaFile;

public class JavaFileUtils {

    public static String getCanonicalClassName(JavaFile javaFile) {
        return javaFile.packageName + "." + javaFile.typeSpec.name;
    }

}
