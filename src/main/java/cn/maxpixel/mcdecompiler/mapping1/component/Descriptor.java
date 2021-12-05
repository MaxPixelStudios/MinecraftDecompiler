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

package cn.maxpixel.mcdecompiler.mapping1.component;

import cn.maxpixel.mcdecompiler.asm.ClassifiedMappingRemapper;

/**
 * Descriptor component for paired mappings
 */
public class Descriptor implements Component {
    public String unmappedDescriptor;

    public Descriptor() {}

    public Descriptor(String unmappedDescriptor) {
        this.unmappedDescriptor = unmappedDescriptor;
    }

    public void reverseUnmapped(ClassifiedMappingRemapper remapper) {
        unmappedDescriptor = remapper.getMappedDescByUnmappedDesc(unmappedDescriptor);
    }

    public String getUnmappedDescriptor() {
        return unmappedDescriptor;
    }

    public void setUnmappedDescriptor(String unmappedDescriptor) {
        this.unmappedDescriptor = unmappedDescriptor;
    }

    /**
     * Mapped descriptor component for paired mappings
     */
    public static class Mapped implements Component {
        public String mappedDescriptor;

        public Mapped() {}

        public Mapped(String mappedDescriptor) {
            this.mappedDescriptor = mappedDescriptor;
        }

        public void reverseMapped(ClassifiedMappingRemapper remapper) {
            mappedDescriptor = remapper.getUnmappedDescByMappedDesc(mappedDescriptor);
        }

        public String getMappedDescriptor() {
            return mappedDescriptor;
        }

        public void setMappedDescriptor(String mappedDescriptor) {
            this.mappedDescriptor = mappedDescriptor;
        }
    }

    /**
     * Namespaced descriptor component<br>
     * Extends {@link Descriptor} because the currently supported namespaced mappings only have unmapped descriptors
     */
    public static class Namespaced extends Descriptor {
        public String descriptorNamespace;

        public Namespaced() {}

        public Namespaced(String unmappedDescriptor, String descriptorNamespace) {
            super(unmappedDescriptor);
            this.descriptorNamespace = descriptorNamespace;
        }

        public String getDescriptorNamespace() {
            return descriptorNamespace;
        }

        public void setDescriptorNamespace(String descriptorNamespace) {
            this.descriptorNamespace = descriptorNamespace;
        }
    }
}