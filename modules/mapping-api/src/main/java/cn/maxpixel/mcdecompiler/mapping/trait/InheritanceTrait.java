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

package cn.maxpixel.mcdecompiler.mapping.trait;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Arrays;
import java.util.Set;

/**
 * A trait that stores inheritance information
 */
public class InheritanceTrait implements MappingTrait {
    public final Object2ObjectOpenHashMap<String, Set<String>> map = new Object2ObjectOpenHashMap<>();

    public Object2ObjectOpenHashMap<String, Set<String>> getMap() {
        return map;
    }

    public void put(String parent, String... children) {
        if (children.length == 0) {
            map.remove(parent);
            return;
        }
        map.put(parent, new ObjectOpenHashSet<>(children));
    }

    public void add(String parent, String... children) {
        if (children.length == 0) return;
        map.computeIfAbsent(parent, k -> new ObjectOpenHashSet<>()).addAll(Arrays.asList(children));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InheritanceTrait that)) return false;
        return map.equals(that.map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}