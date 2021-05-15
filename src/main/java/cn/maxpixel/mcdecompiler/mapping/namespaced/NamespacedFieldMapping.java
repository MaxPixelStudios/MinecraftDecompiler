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

import java.util.Map;

public class NamespacedFieldMapping extends NamespacedMapping implements Owned<NamespacedFieldMapping, NamespacedClassMapping> {
    private NamespacedClassMapping owner;

    public NamespacedFieldMapping(Map<String, String> names) {
        super(names);
    }

    public NamespacedFieldMapping(String namespace, String name) {
        super(namespace, name);
    }

    public NamespacedFieldMapping(String[] namespaces, String[] names) {
        super(namespaces, names);
    }

    public NamespacedFieldMapping(String[] namespaces, String[] names, int nameStart) {
        super(namespaces, names, nameStart);
    }
    public NamespacedFieldMapping() {}

    public boolean isDescriptor() {
        return this instanceof Descriptor;
    }

    public Descriptor asDescriptor() {
        return (Descriptor) this;
    }

    @Override
    public NamespacedClassMapping getOwner() {
        return owner;
    }

    @Override
    public NamespacedFieldMapping setOwner(NamespacedClassMapping owner) {
        this.owner = owner;
        return this;
    }
}