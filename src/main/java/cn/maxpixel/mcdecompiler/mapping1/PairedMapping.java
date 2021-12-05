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

package cn.maxpixel.mcdecompiler.mapping1;

import cn.maxpixel.mcdecompiler.mapping1.component.Component;
import cn.maxpixel.mcdecompiler.mapping1.component.Owned;

import java.util.Objects;

/**
 * A mapping with an unmapped name and a mapped name
 */
public class PairedMapping extends Mapping {
    /**
     * Unmapped name of this mapping
     */
    public String unmappedName;

    /**
     * Mapped name of this mapping
     */
    public String mappedName;

    /**
     * Constructor
     * @param unmappedName The unmapped name
     * @param mappedName The mapped name
     * @param components Components add to this mapping
     */
    public PairedMapping(String unmappedName, String mappedName, Component... components) {
        super(components);
        this.unmappedName = unmappedName;
        this.mappedName = mappedName;
    }

    /**
     * Constructor
     * @param unmappedName The unmapped name
     * @param mappedName The mapped name
     */
    public PairedMapping(String unmappedName, String mappedName) {
        this.unmappedName = unmappedName;
        this.mappedName = mappedName;
    }

    /**
     * Constructor
     * @param components Components add to this mapping
     */
    public PairedMapping(Component... components) {
        super(components);
    }

    /**
     * No-arg constructor
     */
    public PairedMapping() {}

    public Owned<? extends PairedMapping> getOwned() {
        return (Owned<? extends PairedMapping>) super.getOwned();
    }

    /**
     * Reverse this mapping
     * @return this mapping
     */
    public PairedMapping reverse() {
        String temp = unmappedName;
        unmappedName = mappedName;
        mappedName = temp;
        return this;
    }

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

    /* Auto-generated equals, hashCode and toString methods */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PairedMapping)) return false;
        if (!super.equals(o)) return false;
        PairedMapping that = (PairedMapping) o;
        return unmappedName.equals(that.unmappedName) && mappedName.equals(that.mappedName);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hash(unmappedName, mappedName);
    }

    @Override
    public String toString() {
        return "PairedMapping{" +
                "unmappedName='" + unmappedName + '\'' +
                ", mappedName='" + mappedName + '\'' +
                "} " + super.toString();
    }
}