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
import cn.maxpixel.mcdecompiler.mapping.components.LineNumber;
import cn.maxpixel.mcdecompiler.mapping.components.Owned;

import java.util.Objects;

public abstract class PairedMethodMapping extends PairedMapping implements Owned<PairedMethodMapping, PairedClassMapping> {
    private PairedClassMapping owner;

    PairedMethodMapping(String unmappedName, String mappedName) {
        super(unmappedName, mappedName);
    }
    PairedMethodMapping() {}

    public boolean isLineNumber() {
        return this instanceof LineNumber;
    }

    public LineNumber asLineNumber() {
        return (LineNumber) this;
    }

    public boolean isDescriptor() {
        return this instanceof Descriptor;
    }
    public boolean isMappedDescriptor() {
        return this instanceof Descriptor.Mapped;
    }

    public Descriptor asDescriptor() {
        return (Descriptor) this;
    }
    public Descriptor.Mapped asMappedDescriptor() {
        return (Descriptor.Mapped) this;
    }

    @Override
    public PairedMethodMapping setOwner(PairedClassMapping owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public PairedClassMapping getOwner() {
        return owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PairedMethodMapping that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(owner, that.owner);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + owner.hashCode();
    }

    @Override
    public String toString() {
        return "PairedMethodMapping{" +
                "owner={unmappedName='" + owner.getUnmappedName() +
                "', mappedName='" + owner.getMappedName() + '\'' +
                "} " + super.toString();
    }
}