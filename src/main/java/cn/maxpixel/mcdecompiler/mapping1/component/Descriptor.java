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
public interface Descriptor extends Component {
    String getUnmappedDescriptor();

    void setUnmappedDescriptor(String unmappedDescriptor);

    default void reverseUnmapped(ClassifiedMappingRemapper remapper) {
        setUnmappedDescriptor(remapper.getMappedDescByUnmappedDesc(getUnmappedDescriptor()));
    }

    /**
     * Mapped descriptor component for paired mappings
     */
    interface Mapped extends Component {
        String getMappedDescriptor();

        void setMappedDescriptor(String mappedDescriptor);

        default void reverseMapped(ClassifiedMappingRemapper remapper) {
            setMappedDescriptor(remapper.getUnmappedDescByMappedDesc(getMappedDescriptor()));
        }
    }

    /**
     * Namespaced descriptor component<br>
     * Extends {@link Descriptor} because the currently supported namespaced mappings only have unmapped descriptors
     */
    interface Namespaced extends Descriptor {
        String getDescriptorNamespace();

        void setDescriptorNamespace(String namespace);
    }
}