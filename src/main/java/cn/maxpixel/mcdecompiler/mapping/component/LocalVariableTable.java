/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2022  MaxPixelStudios
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

package cn.maxpixel.mcdecompiler.mapping.component;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.Objects;

public interface LocalVariableTable {
    class Namespaced implements Component {
        private final Int2ObjectOpenHashMap<Object2ObjectMap<String, String>> lvt = new Int2ObjectOpenHashMap<>();

        public String getLocalVariableName(int index, String namespace) {
            return lvt.getOrDefault(index, Object2ObjectMaps.emptyMap()).get(namespace);
        }

        public Object2ObjectMap<String, String> getLocalVariableNames(int index) {
            return Object2ObjectMaps.unmodifiable(lvt.get(index));
        }

        public void setLocalVariableName(int index, Map<String, String> names) {
            if(Objects.requireNonNull(names).containsKey(null)) throw new IllegalArgumentException();
            lvt.computeIfAbsent(index, Object2ObjectOpenHashMap::new).putAll(names);
        }

        public void setLocalVariableName(int index, String namespace, String name) {
            lvt.computeIfAbsent(index, Object2ObjectOpenHashMap::new).put(Objects.requireNonNull(namespace), name);
        }

        public void setLocalVariableName(int index, String[] namespaces, String[] names) {
            if(namespaces.length != names.length) throw new IllegalArgumentException();
            Object2ObjectMap<String, String> map = lvt.computeIfAbsent(index, Object2ObjectOpenHashMap::new);
            for(int i = 0; i < namespaces.length; i++) {
                map.put(Objects.requireNonNull(namespaces[i]), names[i]);
            }
        }

        public void setLocalVariableName(int index, String[] namespaces, String[] names, int nameStart) {
            if(nameStart < 0 || nameStart > names.length || namespaces.length != names.length - nameStart) throw new IllegalArgumentException();
            Object2ObjectMap<String, String> map = lvt.computeIfAbsent(index, Object2ObjectOpenHashMap::new);
            for(int i = 0; i < namespaces.length; i++) {
                map.put(Objects.requireNonNull(namespaces[i]), names[i + nameStart]);
            }
        }

        public IntSet getLocalVariableIndexes() {
            return lvt.keySet();
        }

        public void swapAll(String fromNamespace, String toNamespace) {
            Objects.requireNonNull(fromNamespace);
            Objects.requireNonNull(toNamespace);
            lvt.keySet().forEach(index -> {
                Object2ObjectMap<String, String> map = lvt.get(index);
                map.put(toNamespace, map.put(fromNamespace, map.get(toNamespace)));
            });
        }

        public void swap(int index, String fromNamespace, String toNamespace) {
            if(index < 0) throw new IndexOutOfBoundsException();
            Objects.requireNonNull(fromNamespace);
            Objects.requireNonNull(toNamespace);
            Object2ObjectMap<String, String> map = lvt.get(index);
            map.put(toNamespace, map.put(fromNamespace, map.get(toNamespace)));
        }
    }
}