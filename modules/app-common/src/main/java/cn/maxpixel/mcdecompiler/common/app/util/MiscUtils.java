/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2025 MaxPixelStudios(XiaoPangxie732)
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

package cn.maxpixel.mcdecompiler.common.app.util;

import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;

public class MiscUtils {
    public static RuntimeException wrapInRuntime(Throwable e) {
        return new RuntimeException(e);
    }

    public static <T> T onKeyDuplicate(T t, T u) {
        throw new IllegalArgumentException("Key duplicated for \"" + t + "\" and \"" + u + "\"");
    }

    public static StringBuilder createHashString(MessageDigest md) {
        StringBuilder out = new StringBuilder();
        for (byte b : md.digest()) {
            String hex = Integer.toHexString(Byte.toUnsignedInt(b));
            if (hex.length() < 2) out.append('0');
            out.append(hex);
        }
        return out;
    }

    public static String file2Native(@NotNull String fileName) {
        return fileName.replace('\\', '/').replace(".class", "");
    }
}
