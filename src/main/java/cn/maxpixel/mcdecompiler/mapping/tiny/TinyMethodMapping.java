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
import cn.maxpixel.mcdecompiler.mapping.namespaced.NamespacedMethodMapping;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Map;

public class TinyMethodMapping extends NamespacedMethodMapping implements Documented, Documented.LocalVariable {
    private final Int2ObjectOpenHashMap<String> lvtDoc = new Int2ObjectOpenHashMap<>();
    private String doc;

    public TinyMethodMapping(Map<String, String> names, String unmappedDescriptor) {
        super(names, unmappedDescriptor);
    }

    public TinyMethodMapping(String unmappedDescriptor) {
        super(unmappedDescriptor);
    }

    @Override
    public TinyMethodMapping setOwner(NamespacedClassMapping owner) {
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

    @Override
    public void setLocalVariableDoc(int index, String doc) {
        lvtDoc.put(index, doc);
    }

    @Override
    public String getLocalVariableDoc(int index) {
        return lvtDoc.get(index);
    }
}