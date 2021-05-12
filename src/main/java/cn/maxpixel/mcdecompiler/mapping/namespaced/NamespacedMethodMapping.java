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

import cn.maxpixel.mcdecompiler.mapping.components.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.components.Owned;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;

import java.util.Map;
import java.util.Objects;

public class NamespacedMethodMapping extends NamespacedMapping implements Descriptor, Owned<NamespacedMethodMapping, NamespacedClassMapping> {
    private final Int2ObjectOpenHashMap<Map<String, String>> lvt = new Int2ObjectOpenHashMap<>();
    private String unmappedDescriptor;
    private NamespacedClassMapping owner;

    public NamespacedMethodMapping(Map<String, String> names, String unmappedDescriptor) {
        super(names);
        this.unmappedDescriptor = unmappedDescriptor;
    }

    public NamespacedMethodMapping(String unmappedDescriptor) {
        this.unmappedDescriptor = unmappedDescriptor;
    }

    @Override
    public NamespacedClassMapping getOwner() {
        return owner;
    }

    public NamespacedMethodMapping setOwner(NamespacedClassMapping owner) {
        this.owner = owner;
        return this;
    }

    public String getLocalVariableName(int index, String namespace) {
        return lvt.getOrDefault(index, Object2ObjectMaps.emptyMap()).getOrDefault(namespace, "");
    }

    public void setLocalVariableName(int index, Map<String, String> names) {
        lvt.put(index, Objects.requireNonNull(names));
    }

    @Override
    public String getUnmappedDescriptor() {
        return unmappedDescriptor;
    }

    @Override
    public void setUnmappedDescriptor(String unmappedDescriptor) {
        this.unmappedDescriptor = unmappedDescriptor;
    }
}