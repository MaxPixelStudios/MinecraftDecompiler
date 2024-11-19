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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

/**
 * A trait that stores inheritance information
 */
public class InheritanceTrait implements MappingTrait {
    private final Object2ObjectOpenHashMap<String, List<String>> map = new Object2ObjectOpenHashMap<>();

    @Override
    public String getName() {
        return "inheritance";
    }

    public Object2ObjectOpenHashMap<String, List<String>> getMap() {
        return map;
    }

    public void put(String parent, String[] children) {
        if (children.length == 0) return;
        map.put(parent, ObjectArrayList.wrap(children));
    }
}