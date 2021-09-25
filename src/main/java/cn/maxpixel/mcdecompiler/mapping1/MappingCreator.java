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

import cn.maxpixel.mcdecompiler.mapping1.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping1.component.LineNumber;
import cn.maxpixel.mcdecompiler.mapping1.component.Owned;

/**
 * Create supported field & method mappings easily
 */
public final class MappingCreator {
    public static final class Paired {
        public static PairedMapping newOwned() {
            return new PairedImpl(Owned.class);
        }

        public static PairedMapping newOwned(String unmappedName, String mappedName) {
            return new PairedImpl(unmappedName, mappedName, Owned.class);
        }

        public static PairedMapping newDescriptorOwned() {
            return new PairedImpl(Owned.class, Descriptor.class);
        }

        public static PairedMapping newDescriptorOwned(String unmappedName, String mappedName) {
            return new PairedImpl(unmappedName, mappedName, Owned.class, Descriptor.class);
        }

        public static PairedMapping newDescriptorOwned(String unmappedName, String mappedName, String unmappedDescriptor) {
            PairedMapping m = new PairedImpl(unmappedName, mappedName, Owned.class, Descriptor.class);
            ((Descriptor) m).setUnmappedDescriptor(unmappedDescriptor);
            return m;
        }

        public static PairedMapping newMappedDescriptorOwned() {
            return new PairedImpl(Owned.class, Descriptor.Mapped.class);
        }

        public static PairedMapping newMappedDescriptorOwned(String unmappedName, String mappedName) {
            return new PairedImpl(unmappedName, mappedName, Owned.class, Descriptor.Mapped.class);
        }

        public static PairedMapping newMappedDescriptorOwned(String unmappedName, String mappedName, String mappedDescriptor) {
            PairedMapping m = new PairedImpl(unmappedName, mappedName, Owned.class, Descriptor.Mapped.class);
            ((Descriptor.Mapped) m).setMappedDescriptor(mappedDescriptor);
            return m;
        }

        public static PairedMapping newLineNumberMappedDescriptorOwned(String unmappedName, String mappedName, String mappedDescriptor) {
            PairedMapping m = new PairedImpl(unmappedName, mappedName, Owned.class, Descriptor.Mapped.class, LineNumber.class);
            ((Descriptor.Mapped) m).setMappedDescriptor(mappedDescriptor);
            return m;
        }

        public static PairedMapping newLineNumberMappedDescriptorOwned(String unmappedName, String mappedName, String mappedDescriptor, int startLineNumber, int endLineNumber) {
            PairedMapping m = new PairedImpl(unmappedName, mappedName, Owned.class, Descriptor.Mapped.class, LineNumber.class);
            ((Descriptor.Mapped) m).setMappedDescriptor(mappedDescriptor);
            ((LineNumber) m).setStartLineNumber(startLineNumber);
            ((LineNumber) m).setEndLineNumber(endLineNumber);
            return m;
        }

        public static PairedMapping newDescriptorsOwned() {
            return new PairedImpl(Owned.class, Descriptor.class, Descriptor.Mapped.class);
        }

        public static PairedMapping newDescriptorsOwned(String unmappedName, String mappedName) {
            return new PairedImpl(unmappedName, mappedName, Owned.class, Descriptor.class, Descriptor.Mapped.class);
        }

        public static PairedMapping newDescriptorsOwned(String unmappedName, String mappedName, String unmappedDescriptor, String mappedDescriptor) {
            PairedMapping m = new PairedImpl(unmappedName, mappedName, Owned.class, Descriptor.class, Descriptor.Mapped.class);
            ((Descriptor) m).setUnmappedDescriptor(unmappedDescriptor);
            ((Descriptor.Mapped) m).setMappedDescriptor(mappedDescriptor);
            return m;
        }
    }
    public static final class Namespaced {
    }
}