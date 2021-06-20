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

import java.util.Map;
import java.util.Objects;

public class UnmappedDescriptoredNamespacedFieldMapping extends NamespacedFieldMapping implements Descriptor {
    private String unmappedDescriptor;

    public UnmappedDescriptoredNamespacedFieldMapping(Map<String, String> names, String unmappedDescriptor) {
        super(names);
        this.unmappedDescriptor = unmappedDescriptor;
    }

    public UnmappedDescriptoredNamespacedFieldMapping(String namespace, String name, String unmappedDescriptor) {
        super(namespace, name);
        this.unmappedDescriptor = unmappedDescriptor;
    }

    public UnmappedDescriptoredNamespacedFieldMapping(String[] namespaces, String[] names, String unmappedDescriptor) {
        super(namespaces, names);
        this.unmappedDescriptor = unmappedDescriptor;
    }

    public UnmappedDescriptoredNamespacedFieldMapping(String[] namespaces, String[] names, int nameStart, String unmappedDescriptor) {
        super(namespaces, names, nameStart);
        this.unmappedDescriptor = unmappedDescriptor;
    }

    public UnmappedDescriptoredNamespacedFieldMapping(String unmappedDescriptor) {
        this.unmappedDescriptor = unmappedDescriptor;
    }

    @Override
    public UnmappedDescriptoredNamespacedFieldMapping setOwner(NamespacedClassMapping owner) {
        super.setOwner(owner);
        return this;
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
    public void swap(String namespace, String namespace1) {
        super.swap(namespace, namespace1);//TODO
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnmappedDescriptoredNamespacedFieldMapping that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(unmappedDescriptor, that.unmappedDescriptor);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + unmappedDescriptor.hashCode();
    }
}