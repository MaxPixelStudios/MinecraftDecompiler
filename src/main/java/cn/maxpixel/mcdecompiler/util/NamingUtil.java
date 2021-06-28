/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2021  MaxPixelStudios
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

public class NamingUtil {
    public static String getClassName(String name) {
        String fullClassName = name.replace("/", ".");
        return fullClassName.substring(fullClassName.lastIndexOf("."));
    }

    public static String getPackageName(String name) {
        String fullClassName = name.replace("/", ".");
        return fullClassName.substring(0, fullClassName.lastIndexOf("."));
    }

    public static String asJavaName(String pureNativeName) {
        return pureNativeName.replace('/', '.');
    }

    public static String asJavaName0(String fileName) {
        return fileName.replace('/', '.').replace('\\', '.').replace(".class", "");
    }

    public static int getDimension(String javaName) {
        int arrDimension = 0;
        for(int index = 0;index < javaName.length();index+=2) {
            if((index = javaName.indexOf("[]", index)) != -1) arrDimension++;
        }
        return arrDimension;
    }

    public static String asNativeName(String javaName) {
        return javaName.replace('.', '/');
    }

    public static String asNativeName0(String fileName) {
        return fileName.replace('\\', '/').replace(".class", "");
    }

    public static String asDescriptor(String javaName) {
        if(javaName == null || javaName.isEmpty()) return "";
        if(!javaName.contains("[]")) {
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
                default -> "L" + asNativeName(javaName) + ";";
            };
        } else {
            StringBuilder buf = new StringBuilder();
            buf.append("[".repeat(Math.max(0, getDimension(javaName))));
            javaName = javaName.replace("[]", "");
            switch (javaName) {
                case "boolean" -> buf.append('Z');
                case "byte" -> buf.append('B');
                case "char" -> buf.append('C');
                case "double" -> buf.append('D');
                case "float" -> buf.append('F');
                case "int" -> buf.append('I');
                case "long" -> buf.append('J');
                case "short" -> buf.append('S');
                case "void" -> buf.append('V');
                default -> buf.append('L').append(asNativeName(javaName)).append(';');
            }
            return buf.toString();
        }
    }
}