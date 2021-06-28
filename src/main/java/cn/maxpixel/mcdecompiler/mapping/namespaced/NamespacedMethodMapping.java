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
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.Objects;

public class NamespacedMethodMapping extends NamespacedMapping implements Descriptor, Owned<NamespacedMethodMapping, NamespacedClassMapping> {
    private final Int2ObjectOpenHashMap<Object2ObjectMap<String, String>> lvt = new Int2ObjectOpenHashMap<>();
    private String unmappedDescriptor;
    private NamespacedClassMapping owner;

    public NamespacedMethodMapping(Map<String, String> names, String unmappedDescriptor) {
        super(names);
        this.unmappedDescriptor = unmappedDescriptor;
    }

    public NamespacedMethodMapping(String namespace, String name, String unmappedDescriptor) {
        super(namespace, name);
        this.unmappedDescriptor = unmappedDescriptor;
    }

    public NamespacedMethodMapping(String[] namespaces, String[] names, String unmappedDescriptor) {
        super(namespaces, names);
        this.unmappedDescriptor = unmappedDescriptor;
    }

    public NamespacedMethodMapping(String[] namespaces, String[] names, int nameStart, String unmappedDescriptor) {
        super(namespaces, names, nameStart);
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
        lvt.computeIfAbsent(index, Object2ObjectOpenHashMap::new).putAll(Objects.requireNonNull(names));
    }

    public void setLocalVariableName(int index, String namespace, String name) {
        lvt.computeIfAbsent(index, Object2ObjectOpenHashMap::new).put(namespace, name);
    }

    public void setLocalVariableName(int index, String[] namespaces, String[] names) {
        if(namespaces.length != names.length) throw new IllegalArgumentException();
        Object2ObjectMap<String, String> map = lvt.computeIfAbsent(index, Object2ObjectOpenHashMap::new);
        for(int i = 0; i < namespaces.length; i++) {
            map.put(namespaces[i], names[i]);
        }
    }

    public void setLocalVariableName(int index, String[] namespaces, String[] names, int nameStart) {
        if(nameStart < 0 || nameStart > names.length || namespaces.length != names.length - nameStart) throw new IllegalArgumentException();
        Object2ObjectMap<String, String> map = lvt.computeIfAbsent(index, Object2ObjectOpenHashMap::new);
        for(int i = 0; i < namespaces.length; i++) {
            map.put(namespaces[i], names[i + nameStart]);
        }
    }

    @Override
    public String getUnmappedDescriptor() {
        return unmappedDescriptor;
    }

    @Override
    public void setUnmappedDescriptor(String unmappedDescriptor) {
        this.unmappedDescriptor = unmappedDescriptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NamespacedMethodMapping that)) return false;
        if (!super.equals(o)) return false;
        return lvt.equals(that.lvt) && Objects.equals(unmappedDescriptor, that.unmappedDescriptor) && Objects.equals(owner, that.owner);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(lvt, unmappedDescriptor, owner);
    }
}