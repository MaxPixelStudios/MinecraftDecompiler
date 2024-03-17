/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2024 MaxPixelStudios(XiaoPangxie732)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.mapping;

import cn.maxpixel.mcdecompiler.mapping.component.Component;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.mapping.component.Owned;

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
     *
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
     *
     * @param unmappedName The unmapped name
     * @param mappedName The mapped name
     */
    public PairedMapping(String unmappedName, String mappedName) {
        this.unmappedName = unmappedName;
        this.mappedName = mappedName;
    }

    /**
     * Constructor
     *
     * @param components Components add to this mapping
     */
    public PairedMapping(Component... components) {
        super(components);
    }

    /**
     * No-arg constructor
     */
    public PairedMapping() {}

    public Owned<PairedMapping> getOwned() {
        return getComponent(Owned.class);
    }

    /**
     * Reverse this mapping
     *
     * @return this mapping
     */
    public PairedMapping reverse() {
        String temp = unmappedName;
        unmappedName = mappedName;
        mappedName = temp;
        boolean supportDesc = hasComponent(Descriptor.class);
        boolean supportDescMapped = hasComponent(Descriptor.Mapped.class);
        if (supportDesc) {
            Descriptor unmapped = getComponent(Descriptor.class);
            if (supportDescMapped) {
                Descriptor.Mapped mapped = getComponent(Descriptor.Mapped.class);
                String desc = unmapped.unmappedDescriptor;
                unmapped.unmappedDescriptor = mapped.mappedDescriptor;
                mapped.mappedDescriptor = desc;
            } else {
                addComponent(new Descriptor.Mapped(unmapped.unmappedDescriptor));
                removeComponent(Descriptor.class);
            }
        } else if (supportDescMapped) {
            addComponent(new Descriptor(getComponent(Descriptor.Mapped.class).mappedDescriptor));
            removeComponent(Descriptor.Mapped.class);
        }
        getComponentOptional(LocalVariableTable.Paired.class).ifPresent(LocalVariableTable.Paired::reverse);
        return this;
    }

    @Override
    public String getUnmappedName() {
        return unmappedName;
    }

    public void setUnmappedName(String unmappedName) {
        this.unmappedName = unmappedName;
    }

    @Override
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
        if (!(o instanceof PairedMapping that)) return false;
        if (!super.equals(o)) return false;
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