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

package cn.maxpixel.mcdecompiler.common.app.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Objects;

public final class DataMap {// TODO
    private final Object2ObjectOpenHashMap<Key<?>, Object> data = new Object2ObjectOpenHashMap<>();

    public <T> void put(Key<T> key, T value) {
        data.put(key, value);
    }

    public <T> T get(Key<T> key) {
        return (T) data.get(key);
    }

    public static final class Key<T> {
        private final String name;

        public Key(String name) {
            this.name = Objects.requireNonNull(name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return name.equals(((Key<?>) o).name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}