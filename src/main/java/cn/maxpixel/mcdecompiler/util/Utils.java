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

import java.lang.reflect.Array;

public class Utils {
    public interface MapFunction<T, U> {
        U map(int index, T t);
    }
    public static <T, U> U[] mapArray(T[] t, MapFunction<T, U> func, Class<U> cls) {
        U[] u = (U[]) Array.newInstance(cls, t.length);
        for(int i = 0; i < t.length; i++) u[i] = func.map(i, t[i]);
        return u;
    }
    public static RuntimeException wrapInRuntime(Throwable e) {
        return new RuntimeException(e);
    }
}