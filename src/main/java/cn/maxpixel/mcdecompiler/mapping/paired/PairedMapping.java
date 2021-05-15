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
import cn.maxpixel.mcdecompiler.mapping.AbstractMapping;

import java.util.Objects;

public class PairedMapping implements AbstractMapping {
    private String unmappedName;
    private String mappedName;

    public PairedMapping(String unmappedName, String mappedName) {
        this.unmappedName = unmappedName;
        this.mappedName = mappedName;
    }
    public PairedMapping() {}

    public String getUnmappedName() {
        return unmappedName;
    }

    public void setUnmappedName(String unmappedName) {
        this.unmappedName = unmappedName;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String mappedName) {
        this.mappedName = mappedName;
    }

    public void reverse() {
        String temp = unmappedName;
        unmappedName = mappedName;
        mappedName = temp;
    }

    public void reverse(MappingRemapper remapper) {
        reverse();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PairedMapping)) return false;
        PairedMapping that = (PairedMapping) o;
        return unmappedName.equals(that.unmappedName) && mappedName.equals(that.mappedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unmappedName, mappedName);
    }

    @Override
    public String toString() {
        return "PairedMapping{" +
                "unmappedName='" + unmappedName + '\'' +
                ", mappedName='" + mappedName + '\'' +
                '}';
    }
}