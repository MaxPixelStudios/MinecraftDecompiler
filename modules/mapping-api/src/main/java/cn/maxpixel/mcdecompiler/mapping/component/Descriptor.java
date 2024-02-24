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

package cn.maxpixel.mcdecompiler.mapping.component;

import cn.maxpixel.mcdecompiler.common.annotation.MethodOrFieldDesc;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Descriptor component for paired mappings
 */
public class Descriptor implements Component {
    public @NotNull @MethodOrFieldDesc String unmappedDescriptor;

    public Descriptor(@NotNull @MethodOrFieldDesc String unmappedDescriptor) {
        this.unmappedDescriptor = Objects.requireNonNull(unmappedDescriptor);
    }

    public @NotNull String getUnmappedDescriptor() {
        return unmappedDescriptor;
    }

    public void setUnmappedDescriptor(@NotNull @MethodOrFieldDesc String unmappedDescriptor) {
        this.unmappedDescriptor = Objects.requireNonNull(unmappedDescriptor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Descriptor that)) return false;
        return unmappedDescriptor.equals(that.unmappedDescriptor);
    }

    @Override
    public int hashCode() {
        return unmappedDescriptor.hashCode();
    }

    /**
     * Mapped descriptor component for paired mappings
     */
    public static class Mapped implements Component {
        public @NotNull @MethodOrFieldDesc String mappedDescriptor;

        public Mapped(@NotNull @MethodOrFieldDesc String mappedDescriptor) {
            this.mappedDescriptor = Objects.requireNonNull(mappedDescriptor);
        }

        public @NotNull @MethodOrFieldDesc String getMappedDescriptor() {
            return mappedDescriptor;
        }

        public void setMappedDescriptor(@NotNull @MethodOrFieldDesc String mappedDescriptor) {
            this.mappedDescriptor = Objects.requireNonNull(mappedDescriptor);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Mapped mapped)) return false;
            return mappedDescriptor.equals(mapped.mappedDescriptor);
        }

        @Override
        public int hashCode() {
            return mappedDescriptor.hashCode();
        }
    }

    /**
     * Namespaced descriptor component<br>
     * Extends {@link Descriptor} because the currently supported namespaced mappings only have unmapped descriptors
     */
    public static class Namespaced extends Descriptor {
        public @NotNull String descriptorNamespace;

        public Namespaced(@NotNull @MethodOrFieldDesc String unmappedDescriptor, @NotNull String descriptorNamespace) {
            super(unmappedDescriptor);
            this.descriptorNamespace = Objects.requireNonNull(descriptorNamespace);
        }

        public @NotNull String getDescriptorNamespace() {
            return descriptorNamespace;
        }

        public void setDescriptorNamespace(@NotNull String descriptorNamespace) {
            this.descriptorNamespace = Objects.requireNonNull(descriptorNamespace);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Namespaced that)) return false;
            if (!super.equals(o)) return false;
            return descriptorNamespace.equals(that.descriptorNamespace);
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + descriptorNamespace.hashCode();
        }
    }
}