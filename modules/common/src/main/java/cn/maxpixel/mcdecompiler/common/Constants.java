/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2024 MaxPixelStudios(XiaoPangxie732)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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