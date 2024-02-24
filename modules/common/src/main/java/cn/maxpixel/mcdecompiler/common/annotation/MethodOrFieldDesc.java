package cn.maxpixel.mcdecompiler.common.annotation;

import cn.maxpixel.mcdecompiler.common.Constants;
import org.intellij.lang.annotations.Pattern;

@Pattern('(' + Constants.FIELD_DESC_PATTERN + ")|(" + Constants.METHOD_DESC_PATTERN + ')')
public @interface MethodOrFieldDesc {}