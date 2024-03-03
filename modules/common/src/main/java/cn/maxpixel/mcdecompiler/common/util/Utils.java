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

import java.security.MessageDigest;
import java.util.Objects;
import java.util.function.IntFunction;

public class Utils {
    public static RuntimeException wrapInRuntime(Throwable e) {
        return new RuntimeException(e);
    }

    public static <I, O, E extends Throwable> O[] mapArray(I[] input, IntFunction<O[]> outputGenerator,
                                                           LambdaUtil.Function_WithThrowable<I, O, E> func) throws E {
        Objects.requireNonNull(input);
        Objects.requireNonNull(outputGenerator);
        Objects.requireNonNull(func);
        O[] output = outputGenerator.apply(input.length);
        for(int i = 0; i < input.length; i++) {
            output[i] = Objects.requireNonNull(func.apply(Objects.requireNonNull(input[i])));
        }
        return output;
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

    public static boolean isStringNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}