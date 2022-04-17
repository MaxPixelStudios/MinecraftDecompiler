/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2022  MaxPixelStudios
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.util;

import cn.maxpixel.mcdecompiler.Info;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NamingUtil {
    public static int getDimension(@NotNull String javaName) {
        int arrDimension = 0;
        for(int i = javaName.indexOf("[]"); i != -1; i = javaName.indexOf("[]", i + 2)) {
            arrDimension++;
        }
        return arrDimension;
    }

    public static String findSourceNamespace(@NotNull ObjectList<ClassMapping<NamespacedMapping>> mappings) {
        return mappings.parallelStream()
                .map(mapping -> mapping.mapping.getUnmappedNamespace())
                .filter(Objects::nonNull).findAny()
                .or(() -> mappings.parallelStream()
                        .flatMap(cm -> cm.getMethods().parallelStream())
                        .filter(mapping -> mapping.hasComponent(Descriptor.Namespaced.class))
                        .map(mapping -> mapping.getComponent(Descriptor.Namespaced.class).descriptorNamespace)
                        .findAny()
                ).orElseThrow(NullPointerException::new);
    }

    public static int getArgumentLength(@NotNull @Pattern(Info.METHOD_DESC_PATTERN) String descriptor) {
        int length = 0;
        for(int i = 1, max = descriptor.lastIndexOf(')'); i < max; i++) {
            switch(descriptor.charAt(i)) {
                case 'Z', 'B', 'C', 'D', 'F', 'I', 'J', 'S' -> length++;
                case 'L' -> {
                    length++;
                    if((i = descriptor.indexOf(';', i)) == -1) throw new IllegalArgumentException("Invalid method descriptor");
                }
            }
        }
        return length;
    }

    public static String concatNamespaces(@NotNull ObjectSet<String> namespaces, @NotNull Function<String, String> namespaceMapper, @NotNull String delimiter) {
        return namespaces.stream().map(namespaceMapper).peek(name -> {
            if(name == null) throw new IllegalArgumentException("Namespace mismatch");
        }).collect(Collectors.joining(delimiter));
    }

    public static String asJavaName(@NotNull String pureNativeName) {
        return pureNativeName.replace('/', '.');
    }

    public static String asNativeName(@NotNull String javaName) {
        return javaName.replace('.', '/');
    }

    public static String asNativeName0(@NotNull String fileName) {
        return fileName.replace('\\', '/').replace(".class", "");
    }

    public static String asDescriptor(@NotNull String javaName) {
        if(javaName.isBlank()) return "";
        if(!javaName.contains("[]")) {
            return switch(javaName) {
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
            return switch(javaName) {
                case "boolean" -> dim + 'Z';
                case "byte" -> dim + 'B';
                case "char" -> dim + 'C';
                case "double" -> dim + 'D';
                case "float" -> dim + 'F';
                case "int" -> dim + 'I';
                case "long" -> dim + 'J';
                case "short" -> dim + 'S';
                case "void" -> dim + 'V';
                default -> dim + 'L' + asNativeName(javaName) + ';';
            };
        }
    }
}