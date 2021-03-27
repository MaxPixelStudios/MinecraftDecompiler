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

package cn.maxpixel.mcdecompiler.mapping.srg;

import cn.maxpixel.mcdecompiler.asm.MappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.base.BaseMethodMapping;
import cn.maxpixel.mcdecompiler.mapping.components.Descriptor;

public class SrgMethodMapping extends BaseMethodMapping implements Descriptor, Descriptor.Mapped {
    private String unmappedDescriptor;
    private String mappedDescriptor;

    public SrgMethodMapping(String unmappedName, String mappedName, String unmappedDescriptor, String mappedDescriptor) {
        super(unmappedName, mappedName);
        this.unmappedDescriptor = unmappedDescriptor;
        this.mappedDescriptor = mappedDescriptor;
    }
    public SrgMethodMapping() {}

    @Override
    public String getUnmappedDescriptor() {
        return unmappedDescriptor;
    }

    @Override
    public void setUnmappedDescriptor(String unmappedDescriptor) {
        this.unmappedDescriptor = unmappedDescriptor;
    }

    @Override
    public String getMappedDescriptor() {
        return mappedDescriptor;
    }

    @Override
    public void setMappedDescriptor(String mappedDescriptor) {
        this.mappedDescriptor = mappedDescriptor;
    }

    @Override
    public SrgMethodMapping setOwner(ClassMapping owner) {
        super.setOwner(owner);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj instanceof SrgMethodMapping) {
            SrgMethodMapping obj1 = (SrgMethodMapping) obj;
            return super.equals(obj) && getUnmappedDescriptor().equals(obj1.getUnmappedDescriptor()) &&
                    getMappedDescriptor().equals(obj1.getMappedDescriptor());
        }
        return false;
    }

    @Override
    public void reverse() {
        String temp = getUnmappedName();
        setUnmappedName(getMappedName());
        setMappedName(temp);

        temp = unmappedDescriptor;
        unmappedDescriptor = mappedDescriptor;
        mappedDescriptor = temp;
    }

    @Override
    @Deprecated
    public void reverse(MappingRemapper remapper) {
        reverse();
    }

    @Override
    @Deprecated
    public void reverse0(MappingRemapper remapper) {
        reverse();
    }

    @Override
    public String toString() {
        return "SrgMethodMapping{" +
                "UnmappedName=" + getUnmappedName() +
                ", MappedName=" + getMappedName() +
                ", UnmappedDescriptor" + getUnmappedDescriptor() +
                ", MappedDescriptor" + getMappedDescriptor() +
                '}';
    }
}