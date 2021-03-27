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

package cn.maxpixel.mcdecompiler.mapping.base;

import cn.maxpixel.mcdecompiler.asm.MappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.components.LineNumber;

public abstract class BaseMethodMapping extends BaseFieldMethodShared {
    public BaseMethodMapping(String unmappedName, String mappedName) {
        super(unmappedName, mappedName);
    }
    public BaseMethodMapping() {}

    public boolean isLineNumber() {
        return this instanceof LineNumber;
    }

    public LineNumber asLineNumber() {
        return (LineNumber) this;
    }

    @Override
    public BaseMethodMapping setOwner(ClassMapping owner) {
        super.setOwner(owner);
        return this;
    }

    @Override
    @Deprecated
    public void reverse() {
        throw new UnsupportedOperationException();
    }

    public void reverse(MappingRemapper remapper) {
        super.reverse();
        if(isDescriptor()) asDescriptor().reverse0(remapper);
        else if(isMappedDescriptor()) asMappedDescriptor().reverse0(remapper);
        else throw new IllegalArgumentException("Impls of MethodMapping must implement at least one of Descriptor or Descriptor.Mapped");
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(this == obj) return true;
        if(obj instanceof BaseMethodMapping) {
            BaseMethodMapping obj1 = (BaseMethodMapping) obj;
            return getUnmappedName().equals(obj1.getUnmappedName()) && getMappedName().equals(obj1.getMappedName());
        }
        return false;
    }

    @Override
    public String toString() {
        return "BaseMethodMapping{" +
                "UnmappedName=" + getUnmappedName() +
                ", MappedName=" + getMappedName() +
                '}';
    }
}