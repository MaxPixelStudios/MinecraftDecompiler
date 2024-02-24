package cn.maxpixel.mcdecompiler.common;

import org.intellij.lang.annotations.Language;

public interface Constants {
    @Language("RegExp")
    String DESC_PATTERN = "(\\[*([ZBCDFIJS]|L([A-Za-z_]+\\w*[/$]?)+;))";

    @Language("RegExp")
    String FIELD_DESC_PATTERN = '^' + DESC_PATTERN + '$';

    @Language("RegExp")
    String METHOD_DESC_PATTERN = "^\\(" + DESC_PATTERN + "*\\)(" + DESC_PATTERN + "|V)$";

    boolean IS_DEV = System.console() == null && Boolean.getBoolean("mcd.isDevEnv");

    String FERNFLOWER_ABSTRACT_PARAMETER_NAMES = "fernflower_abstract_parameter_names.txt";
}