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

import cn.maxpixel.mcdecompiler.asm.MappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.components.Descriptor;

import java.util.Objects;

public class MappedDescriptoredPairedMethodMapping extends PairedMethodMapping implements Descriptor.Mapped {
    private String mappedDescriptor;

    public MappedDescriptoredPairedMethodMapping(String unmappedName, String mappedName, String mappedDescriptor) {
        super(unmappedName, mappedName);
        this.mappedDescriptor = mappedDescriptor;
    }
    public MappedDescriptoredPairedMethodMapping() {}

    @Override
    public String getMappedDescriptor() {
        return mappedDescriptor;
    }

    @Override
    public void setMappedDescriptor(String mappedDescriptor) {
        this.mappedDescriptor = mappedDescriptor;
    }

    @Override
    @Deprecated
    public void reverse() {
        throw new UnsupportedOperationException("Use reverse(MappingRemapper) instead");
    }

    @Override
    public void reverse(MappingRemapper remapper) {
        super.reverse();
        mappedDescriptor = remapper.getUnmappedDescByMappedDesc(mappedDescriptor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MappedDescriptoredPairedMethodMapping)) return false;
        if (!super.equals(o)) return false;
        MappedDescriptoredPairedMethodMapping that = (MappedDescriptoredPairedMethodMapping) o;
        return mappedDescriptor.equals(that.mappedDescriptor);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(mappedDescriptor);
    }

    @Override
    public String toString() {
        return "MappedDescriptoredPairedMethodMapping{" +
                "mappedDescriptor='" + mappedDescriptor + '\'' +
                "} " + super.toString();
    }
}