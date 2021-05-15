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

import cn.maxpixel.mcdecompiler.mapping.components.Documented;
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedClassMapping;
import cn.maxpixel.mcdecompiler.mapping.namespaced.UnmappedDescriptoredNamespacedFieldMapping;

import java.util.Map;

public class TinyFieldMapping extends UnmappedDescriptoredNamespacedFieldMapping implements Documented {
    private String doc;

    public TinyFieldMapping(Map<String, String> names, String unmappedDescriptor) {
        super(names, unmappedDescriptor);
    }

    public TinyFieldMapping(String namespace, String name, String unmappedDescriptor) {
        super(namespace, name, unmappedDescriptor);
    }

    public TinyFieldMapping(String[] namespaces, String[] names, String unmappedDescriptor) {
        super(namespaces, names, unmappedDescriptor);
    }

    public TinyFieldMapping(String[] namespaces, String[] names, int nameStart, String unmappedDescriptor) {
        super(namespaces, names, nameStart, unmappedDescriptor);
    }

    public TinyFieldMapping(String unmappedDescriptor) {
        super(unmappedDescriptor);
    }

    @Override
    public TinyFieldMapping setOwner(NamespacedClassMapping owner) {
        super.setOwner(owner);
        return this;
    }

    @Override
    public void setDoc(String doc) {
        this.doc = doc;
    }

    @Override
    public String getDoc() {
        return doc;
    }
}