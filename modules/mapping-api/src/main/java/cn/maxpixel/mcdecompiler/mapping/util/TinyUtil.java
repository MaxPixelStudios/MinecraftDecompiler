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

package cn.maxpixel.mcdecompiler.mapping.util;

public final class TinyUtil {
    private TinyUtil() {
        throw new AssertionError("No instances");
    }

    public static String escape(String unescaped) {
        StringBuilder sb = new StringBuilder(unescaped.length() + 16);
        int mark = 0;
        for (int i = 0; i < unescaped.length(); i++) {
            char escaped = switch (unescaped.charAt(i)) {
                case '\\' -> '\\';
                case '\n' -> 'n';
                case '\r' -> 'r';
                case '\t' -> 't';
                case '\0' -> '0';
                default -> '\0';
            };
            if (escaped != 0) {
                if (mark < i) sb.append(unescaped, mark, i);
                mark = i + 1;
                sb.append('\\').append(escaped);
            }
        }
        return sb.append(unescaped, mark, unescaped.length()).toString();
    }

    public static String unescape(String escaped) {
        return unescape(escaped, 0);
    }

    public static String unescape(String escaped, int beginIndex) {
        StringBuilder sb = new StringBuilder(escaped.length());
        int mark = beginIndex;
        for (int i = escaped.indexOf('\\', beginIndex); i >= 0; i = escaped.indexOf('\\', mark)) {
            char unescaped = switch (escaped.charAt(++i)) {
                case '\\' -> '\\';
                case 'n' -> '\n';
                case 'r' -> '\r';
                case 't' -> '\t';
                case '0' -> '\0';
                default -> throw new IllegalArgumentException("Unknown escape character: \\" + escaped.charAt(i));
            };
            if (mark < i - 1) sb.append(escaped, mark, i - 1);
            mark = i + 1;
            sb.append(unescaped);
        }
        return sb.append(escaped, mark, escaped.length()).toString();
    }
}