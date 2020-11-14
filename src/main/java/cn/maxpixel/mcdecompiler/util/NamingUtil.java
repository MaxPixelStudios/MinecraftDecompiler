/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2020  MaxPixelStudios
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
    public static String asJavaName(String nativeName) {
        return nativeName.replace('/', '.').replace('\\', '.').replace(".class", "");
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
    public static String asDescriptor(String javaName) {
        if(javaName == null || javaName.isEmpty()) return "";
        if(!javaName.contains("[]"))
            switch (javaName) {
                case "boolean": return "Z";
                case "byte": return "B";
                case "char": return "C";
                case "double": return "D";
                case "float": return "F";
                case "int": return "I";
                case "long": return "J";
                case "short": return "S";
                case "void": return "V";
                default: return "L" + asNativeName(javaName) + ";";
            }
        else {
            StringBuilder buf = new StringBuilder();
            for(int arrDimension = getDimension(javaName);arrDimension>0;arrDimension--) buf.append('[');
            javaName = javaName.replace("[]", "");
            switch (javaName) {
                case "boolean":
                    buf.append('Z');
                    break;
                case "byte":
                    buf.append('B');
                    break;
                case "char":
                    buf.append('C');
                    break;
                case "double":
                    buf.append('D');
                    break;
                case "float":
                    buf.append('F');
                    break;
                case "int":
                    buf.append('I');
                    break;
                case "long":
                    buf.append('J');
                    break;
                case "short":
                    buf.append('S');
                    break;
                case "void":
                    buf.append('V');
                    break;
                default:
                    buf.append('L').append(asNativeName(javaName)).append(';');
                    break;
            }
            return buf.toString();
        }
    }
}