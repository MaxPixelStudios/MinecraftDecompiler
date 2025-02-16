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

package cn.maxpixel.mcdecompiler.mapping.util;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class Utils {
    public static RuntimeException wrapInRuntime(Throwable e) {
        return new RuntimeException(e);
    }

    public static <I, O> O[] mapArray(I[] input, IntFunction<O[]> outputGenerator, Function<I, O> func) {
        Objects.requireNonNull(func);
        O[] output = Objects.requireNonNull(outputGenerator.apply(input.length));
        for (int i = 0; i < input.length; i++) {
            output[i] = Objects.requireNonNull(func.apply(Objects.requireNonNull(input[i])));
        }
        return output;
    }

    public static <T> T onKeyDuplicate(T t, T u) {
        throw new IllegalArgumentException("Key duplicated for \"" + t + "\" and \"" + u + "\"");
    }

    public static boolean isStringNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    public static <T> T throwInvalidDescriptor(boolean method) {
        throw new IllegalArgumentException(method ? "Invalid method descriptor" : "Invalid descriptor");
    }

    public static BufferedReader asBufferedReader(@NotNull Reader reader) {
        return Objects.requireNonNull(reader, "reader cannot be null") instanceof BufferedReader br ?
                br : new BufferedReader(reader);
    }
}