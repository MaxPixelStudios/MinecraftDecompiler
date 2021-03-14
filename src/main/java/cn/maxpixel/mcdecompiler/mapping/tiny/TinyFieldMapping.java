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

package cn.maxpixel.mcdecompiler.mapping.tiny;

import cn.maxpixel.mcdecompiler.mapping.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.TinyClassMapping;
import cn.maxpixel.mcdecompiler.mapping.base.BaseFieldMapping;
import cn.maxpixel.mcdecompiler.mapping.components.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.components.Documented;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class TinyFieldMapping extends BaseFieldMapping implements Descriptor, cn.maxpixel.mcdecompiler.mapping.components.Namespaced, Documented {
    private final Object2ObjectOpenHashMap<String, String> names = new Object2ObjectOpenHashMap<>();
    private String unmappedDescriptor;
    private String document;
    public TinyFieldMapping(String unmappedDescriptor, Namespaced... names) {
        for(Namespaced namespaced : names) this.names.put(namespaced.getNamespace(), namespaced.getName());
        this.unmappedDescriptor = unmappedDescriptor;
    }

    @Override
    public TinyClassMapping getOwner() {
        return (TinyClassMapping) super.getOwner();
    }
    @Override
    public TinyFieldMapping setOwner(ClassMapping owner) {
        if(!(owner instanceof TinyClassMapping)) throw new IllegalArgumentException("TinyFieldMapping's owner must be TinyClassMapping");
        return this.setOwner((TinyClassMapping) owner);
    }
    public TinyFieldMapping setOwner(TinyClassMapping owner) {
        super.setOwner(owner);
        return this;
    }

    @Override
    public String getName(String namespace) {
        return names.get(namespace);
    }

    @Override
    public void setName(Namespaced name) {
        names.put(name.getNamespace(), name.getName());
    }

    /** Recommend to use {@link TinyFieldMapping#getName(String)} */
    @Override
    public String getUnmappedName() {
        String s = getName(Namespaced.OFFICIAL);
        return s == null ? getName(Namespaced.INTERMEDIARY) : s;
    }

    /** Recommend to use {@link TinyFieldMapping#getName(String)} */
    @Override
    public String getMappedName() {
        String s = getName(Namespaced.YARN);
        return s == null ? getName(Namespaced.INTERMEDIARY) : s;
    }

    /**
     * @deprecated Use {@link TinyFieldMapping#setName(Namespaced)} instead.
     * @throws UnsupportedOperationException When calling this method
     */
    @Override
    @Deprecated
    public void setUnmappedName(String unmappedName) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated Use {@link TinyFieldMapping#setName(Namespaced)} instead.
     * @throws UnsupportedOperationException When calling this method
     */
    @Override
    @Deprecated
    public void setMappedName(String mappedName) {
        throw new UnsupportedOperationException();
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
    public void setDocument(String document) {
        this.document = document;
    }

    @Override
    public String getDocument() {
        return document;
    }
}