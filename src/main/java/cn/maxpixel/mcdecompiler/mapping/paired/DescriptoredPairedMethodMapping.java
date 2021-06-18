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

package cn.maxpixel.mcdecompiler.mapping.paired;

import cn.maxpixel.mcdecompiler.mapping.components.Descriptor;

import java.util.Objects;

public class DescriptoredPairedMethodMapping extends PairedMethodMapping implements Descriptor, Descriptor.Mapped {
    private String unmappedDescriptor;
    private String mappedDescriptor;

    public DescriptoredPairedMethodMapping(String unmappedName, String mappedName, String unmappedDescriptor, String mappedDescriptor) {
        super(unmappedName, mappedName);
        this.unmappedDescriptor = unmappedDescriptor;
        this.mappedDescriptor = mappedDescriptor;
    }
    public DescriptoredPairedMethodMapping() {}

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
    public DescriptoredPairedMethodMapping setOwner(PairedClassMapping owner) {
        super.setOwner(owner);
        return this;
    }

    @Override
    public void reverse() {
        super.reverse();

        String temp = unmappedDescriptor;
        unmappedDescriptor = mappedDescriptor;
        mappedDescriptor = temp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DescriptoredPairedMethodMapping that)) return false;
        if (!super.equals(o)) return false;
        return unmappedDescriptor.equals(that.unmappedDescriptor) && mappedDescriptor.equals(that.mappedDescriptor);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(unmappedDescriptor, mappedDescriptor);
    }

    @Override
    public String toString() {
        return "DescriptoredPairedMethodMapping{" +
                "unmappedDescriptor='" + unmappedDescriptor + '\'' +
                ", mappedDescriptor='" + mappedDescriptor + '\'' +
                "} " + super.toString();
    }
}