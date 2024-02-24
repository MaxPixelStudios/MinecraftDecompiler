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

package cn.maxpixel.mcdecompiler.common.util;

import cn.maxpixel.mcdecompiler.common.Constants;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class NamingUtil {
    public static int getDimension(@NotNull String javaName) {
        int arrDimension = 0;
        for (int i = javaName.indexOf("[]"); i != -1; i = javaName.indexOf("[]", i + 2)) {
            arrDimension++;
        }
        return arrDimension;
    }

    public static String concatNamespaces(@NotNull Set<String> namespaces, @NotNull Function<String, String> namespaceMapper, @NotNull String delimiter) {
        return namespaces.stream().map(namespaceMapper).peek(name -> {
            if (name == null) throw new IllegalArgumentException("Namespace mismatch");
        }).collect(Collectors.joining(delimiter));
    }

    public static String asJavaName(@NotNull String nativeName) {
        return nativeName.replace('/', '.');
    }

    public static String asNativeName(@NotNull String javaName) {
        return javaName.replace('.', '/');
    }

    public static String file2Native(@NotNull String fileName) {
        return fileName.replace('\\', '/').replace(".class", "");
    }

    public static String java2Descriptor(@NotNull String javaName) {
        if (javaName.isBlank()) return "";
        if (!javaName.contains("[]")) {
            return switch (javaName) {
                case "boolean" -> "Z";
                case "byte" -> "B";
                case "char" -> "C";
                case "double" -> "D";
                case "float" -> "F";
                case "int" -> "I";
                case "long" -> "J";
                case "short" -> "S";
                case "void" -> "V";
                default -> 'L' + asNativeName(javaName) + ';';
            };
        } else {
            String dim = "[".repeat(getDimension(javaName));
            javaName = javaName.replace("[]", "");
            return switch (javaName) {
                case "boolean" -> dim + 'Z';
                case "byte" -> dim + 'B';
                case "char" -> dim + 'C';
                case "double" -> dim + 'D';
                case "float" -> dim + 'F';
                case "int" -> dim + 'I';
                case "long" -> dim + 'J';
                case "short" -> dim + 'S';
                case "void" -> throw new IllegalArgumentException("Invalid java name");
                default -> dim + 'L' + asNativeName(javaName) + ';';
            };
        }
    }

    public static String descriptor2Java(@NotNull @Pattern(Constants.FIELD_DESC_PATTERN) String descriptor) {
        if (descriptor.isBlank()) return "";
        char c0 = descriptor.charAt(0);
        if (c0 != '[') {
            if (c0 == 'L') return asJavaName(descriptor.substring(1, descriptor.length() - 1));
            else return primitive2Java(c0);
        } else {
            int dim = 1;
            while (descriptor.charAt(dim++) == '[');
            String s = "[]".repeat(--dim);
            return switch (descriptor.charAt(dim)) {
                case 'L' -> asJavaName(descriptor.substring(dim + 1, descriptor.length() - 1)) + s;
                case 'V' -> DescriptorUtil.throwInvalid(false);
                default -> primitive2Java(descriptor.charAt(dim));
            } + s;
        }
    }

    public static String primitive2Java(@NotNull char desc) {
        return switch (desc) {
            case 'Z' -> "boolean";
            case 'B' -> "byte";
            case 'C' -> "char";
            case 'D' -> "double";
            case 'F' -> "float";
            case 'I' -> "int";
            case 'J' -> "long";
            case 'S' -> "short";
            case 'V' -> "void";
            default -> DescriptorUtil.throwInvalid(false);
        };
    }
}