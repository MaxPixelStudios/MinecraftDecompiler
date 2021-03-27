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

import cn.maxpixel.mcdecompiler.mapping.components.Descriptor;

public class MappedDescriptoredBaseMethodMapping extends BaseMethodMapping implements Descriptor.Mapped {
    private String mappedDescriptor;

    public MappedDescriptoredBaseMethodMapping(String unmappedName, String mappedName, String mappedDescriptor) {
        super(unmappedName, mappedName);
        this.mappedDescriptor = mappedDescriptor;
    }
    public MappedDescriptoredBaseMethodMapping() {}

    @Override
    public String getMappedDescriptor() {
        return mappedDescriptor;
    }

    @Override
    public void setMappedDescriptor(String mappedDescriptor) {
        this.mappedDescriptor = mappedDescriptor;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj instanceof MappedDescriptoredBaseMethodMapping) {
            MappedDescriptoredBaseMethodMapping obj1 = (MappedDescriptoredBaseMethodMapping) obj;
            return super.equals(obj) && getMappedDescriptor().equals(obj1.getMappedDescriptor());
        }
        return false;
    }

    @Override
    public String toString() {
        return "MappedDescriptoredBaseMethodMapping{" +
                "UnmappedName=" + getUnmappedName() +
                ", MappedName=" + getMappedName() +
                ", MappedDescriptor" + getMappedDescriptor() +
                '}';
    }
}