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

package cn.maxpixel.mcdecompiler.mapping.namespaced;

import cn.maxpixel.mcdecompiler.mapping.AbstractMapping;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

public class NamespacedMapping implements AbstractMapping {
    // Forge
    public static final String OBF = "obf";
    public static final String SRG = "srg";

    // Fabric
    public static final String OFFICIAL = "official";
    public static final String INTERMEDIARY = "intermediary";
    public static final String YARN = "named";

    private final Object2ObjectOpenHashMap<String, String> names = new Object2ObjectOpenHashMap<>();

    public NamespacedMapping(Map<String, String> names) {
        this.names.putAll(names);
    }

    public NamespacedMapping(String namespace, String name) {
        this.names.put(namespace, name);
    }

    public NamespacedMapping(String[] namespaces, String[] names) {
        if(namespaces.length != names.length) throw new IllegalArgumentException();
        for(int i = 0; i < namespaces.length; i++) {
            this.names.put(namespaces[i], names[i]);
        }
    }

    public NamespacedMapping(String[] namespaces, String[] names, int nameStart) {
        if(nameStart < 0 || nameStart >= names.length || namespaces.length != (names.length - nameStart)) throw new IllegalArgumentException();
        for(int i = 0; i < namespaces.length; i++) {
            this.names.put(namespaces[i], names[i + nameStart]);
        }
    }
    public NamespacedMapping() {}

    public void setName(String namespace, String name) {
        names.put(namespace, name);
    }

    public String getName(String namespace) {
        return names.get(namespace);
    }

    public void swap(String namespace, String namespace1) {
        String temp = names.get(namespace);
        names.put(namespace, names.get(namespace1));
        names.put(namespace1, temp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NamespacedMapping that)) return false;
        return names.equals(that.names);
    }

    @Override
    public int hashCode() {
        return names.hashCode();
    }
}