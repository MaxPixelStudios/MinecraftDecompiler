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

package cn.maxpixel.mcdecompiler.mapping1.component.impl;

import cn.maxpixel.mcdecompiler.mapping1.component.Descriptor;

public class DescriptorImpl implements Descriptor {
    private String unmappedDescriptor;

    @Override
    public String getUnmappedDescriptor() {
        return unmappedDescriptor;
    }

    @Override
    public void setUnmappedDescriptor(String unmappedDescriptor) {
        this.unmappedDescriptor = unmappedDescriptor;
    }

    public static class MappedImpl implements Mapped {
        private String mappedDescriptor;

        @Override
        public String getMappedDescriptor() {
            return mappedDescriptor;
        }

        @Override
        public void setMappedDescriptor(String mappedDescriptor) {
            this.mappedDescriptor = mappedDescriptor;
        }
    }

    public static class NamespacedImpl extends DescriptorImpl implements Namespaced {
        private String descriptorNamespace;

        @Override
        public String getDescriptorNamespace() {
            return descriptorNamespace;
        }

        @Override
        public void setDescriptorNamespace(String namespace) {
            this.descriptorNamespace = namespace;
        }
    }
}