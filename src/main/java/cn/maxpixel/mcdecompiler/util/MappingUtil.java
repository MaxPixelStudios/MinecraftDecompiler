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

package cn.maxpixel.mcdecompiler.util;

import cn.maxpixel.mcdecompiler.mapping1.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping1.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping1.component.*;

public final class MappingUtil {
    public static final class Paired {
        public static PairedMapping o(String unmapped, String mapped) {
            return new PairedMapping(unmapped, mapped, new Owned<>());
        }

        public static PairedMapping duo(String unmapped, String mapped, String unmappedDesc) {
            return new PairedMapping(unmapped, mapped, new Descriptor(unmappedDesc), new Owned<>());
        }

        public static PairedMapping dmo(String unmapped, String mapped, String mappedDesc) {
            return new PairedMapping(unmapped, mapped, new Descriptor.Mapped(mappedDesc), new Owned<>());
        }

        public static PairedMapping ldmo(String unmapped, String mapped, String mappedDesc, int start, int end) {
            return new PairedMapping(unmapped, mapped, new LineNumber(start, end), new Descriptor.Mapped(mappedDesc), new Owned<>());
        }

        public static PairedMapping d2o(String unmapped, String mapped, String unmappedDesc, String mappedDesc) {
            return new PairedMapping(unmapped, mapped, new Descriptor(unmappedDesc), new Descriptor.Mapped(mappedDesc), new Owned<>());
        }
    }

    public static final class Namespaced {
        public static NamespacedMapping o(String[] namespaces, String[] names) {
            return new NamespacedMapping(namespaces, names, new Owned<>());
        }

        public static NamespacedMapping d(String[] namespaces, String[] names, int start) {
            return new NamespacedMapping(namespaces, names, start, new Documented());
        }

        public static NamespacedMapping duo(String[] namespaces, String[] names, int start, String descNamespace, String desc) {
            return new NamespacedMapping(namespaces, names, start, new Owned<>(), new Descriptor.Namespaced(desc, descNamespace));
        }

        public static NamespacedMapping dduo(String[] namespaces, String[] names, int start, String descNamespace, String desc) {
            return new NamespacedMapping(namespaces, names, start, new Owned<>(), new Descriptor.Namespaced(desc, descNamespace), new Documented());
        }

        public static NamespacedMapping slduo(String[] namespaces, String[] names, int start, String descNamespace, String desc) {
            return new NamespacedMapping(namespaces, names, start, new Owned<>(), new Descriptor.Namespaced(desc, descNamespace),
                    new StaticIdentifiable(), new LocalVariableTable.Namespaced());
        }

        public static NamespacedMapping dllduo(String[] namespaces, String[] names, int start, String descNamespace, String desc) {
            return new NamespacedMapping(namespaces, names, start, new Owned<>(), new Descriptor.Namespaced(desc, descNamespace), new Documented(),
                    new Documented.LocalVariable(), new LocalVariableTable.Namespaced());
        }
    }
}