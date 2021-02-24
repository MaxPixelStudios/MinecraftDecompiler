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
import cn.maxpixel.mcdecompiler.mapping.base.DescriptoredBaseMethodMapping;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class TinyMethodMapping extends DescriptoredBaseMethodMapping implements cn.maxpixel.mcdecompiler.mapping.components.Namespaced {
    private final Object2ObjectOpenHashMap<String, String> names = new Object2ObjectOpenHashMap<>();
    public TinyMethodMapping(String unmappedDescriptor, Namespaced... names) {
        for(Namespaced namespaced : names) this.names.put(namespaced.getNamespace(), namespaced.getName());
        setUnmappedDescriptor(unmappedDescriptor);
    }

    @Override
    public String getName(String namespace) {
        return names.get(namespace);
    }

    @Override
    public void setName(Namespaced name) {
        names.put(name.getNamespace(), name.getName());
    }

    @Override
    public TinyClassMapping getOwner() {
        return (TinyClassMapping) super.getOwner();
    }
    @Override
    public TinyMethodMapping setOwner(ClassMapping owner) {
        if(!(owner instanceof TinyClassMapping)) throw new IllegalArgumentException("TinyMethodMapping's owner must be TinyClassMapping");
        return this.setOwner((TinyClassMapping) owner);
    }
    public TinyMethodMapping setOwner(TinyClassMapping owner) {
        super.setOwner(owner);
        return this;
    }

    @Override
    public String getUnmappedName() {
        return getName(Namespaced.OFFICIAL);
    }

    @Override
    public String getMappedName() {
        String s = getName(Namespaced.YARN);
        return s == null ? getName(Namespaced.INTERMEDIARY) : s;
    }

    @Override
    public void setUnmappedName(String unmappedName) {
        setName(new Namespaced(Namespaced.OFFICIAL, unmappedName));
    }

    @Override
    public void setMappedName(String mappedName) {
        String s = getName(Namespaced.YARN);
        if(s == null) setName(new Namespaced(Namespaced.INTERMEDIARY, mappedName));
        else setName(new Namespaced(Namespaced.YARN, mappedName));
    }
}