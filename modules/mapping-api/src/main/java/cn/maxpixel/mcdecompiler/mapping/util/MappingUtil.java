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

package cn.maxpixel.mcdecompiler.mapping.util;

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.component.*;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class MappingUtil {
    private MappingUtil() {
        throw new AssertionError("No instances");
    }

    public static <T extends Mapping> void checkOwner(Owned<T> owned, ClassMapping<T> owner) {
        if (owned.owner != owner) throw new IllegalArgumentException("Owner mismatch");
    }

    public static final class Paired {
        public static final Function<String, ClassMapping<PairedMapping>> COMPUTE_DEFAULT_CLASS = name ->
                new ClassMapping<>(new PairedMapping(name));

        public static String checkSlimSrgMethod(ClassMapping<PairedMapping> cls, PairedMapping method, @Nullable ClassifiedMappingRemapper remapper) {
            checkOwner(method.getOwned(), cls);
            if (method.hasComponent(Descriptor.class)) {
                return method.getComponent(Descriptor.class).unmappedDescriptor;
            } else if (remapper != null && method.hasComponent(Descriptor.Mapped.class)) {
                return remapper.unmapMethodDesc(method.getComponent(Descriptor.Mapped.class).mappedDescriptor);
            } else throw new UnsupportedOperationException();
        }

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
        public static String checkTiny(String namespace0, ClassMapping<NamespacedMapping> cls, NamespacedMapping mapping) {
            if (!mapping.hasComponent(Owned.class) || !mapping.hasComponent(Descriptor.Namespaced.class))
                throw new UnsupportedOperationException();
            checkOwner(mapping.getOwned(), cls);
            Descriptor.Namespaced desc = mapping.getComponent(Descriptor.Namespaced.class);
            if (!namespace0.equals(desc.descriptorNamespace)) throw new IllegalArgumentException("Descriptor namespace mismatch");
            return desc.unmappedDescriptor;
        }

        public static NamespacedMapping o(String[] namespaces, String[] names) {
            return new NamespacedMapping(namespaces, names, new Owned<>()).setUnmappedNamespace(namespaces[0]);
        }

        public static NamespacedMapping d(String[] namespaces, String[] names, int start) {
            return new NamespacedMapping(namespaces, names, start, new Documented()).setUnmappedNamespace(namespaces[0]);
        }

        public static NamespacedMapping duo(String[] namespaces, String[] names, int start, String descNamespace, String desc) {
            return new NamespacedMapping(namespaces, names, start, new Owned<>(), new Descriptor.Namespaced(desc, descNamespace))
                    .setUnmappedNamespace(namespaces[0]);
        }

        public static NamespacedMapping dduo(String[] namespaces, String[] names, int start, String descNamespace, String desc) {
            return new NamespacedMapping(namespaces, names, start, new Owned<>(), new Descriptor.Namespaced(desc, descNamespace), new Documented())
                    .setUnmappedNamespace(namespaces[0]);
        }

        public static NamespacedMapping slduo(String[] namespaces, String[] names, int start, String descNamespace, String desc) {
            return new NamespacedMapping(namespaces, names, start, new Owned<>(), new Descriptor.Namespaced(desc, descNamespace),
                    new StaticIdentifiable(), new LocalVariableTable.Namespaced().setUnmappedNamespace(namespaces[0]))
                    .setUnmappedNamespace(namespaces[0]);
        }

        public static NamespacedMapping dlduo(String[] namespaces, String[] names, int start, String descNamespace, String desc) {
            return new NamespacedMapping(namespaces, names, start, new Owned<>(), new Descriptor.Namespaced(desc, descNamespace),
                    new Documented(), new LocalVariableTable.Namespaced().setUnmappedNamespace(namespaces[0]))
                    .setUnmappedNamespace(namespaces[0]);
        }
    }

    public static String[] split(String s, char c) {
        int n = 2;
        int i = s.indexOf(c);
        if (i == -1) return new String[] {s};
        while ((i = s.indexOf(c, i + 1)) != -1) n++;

        String[] ret = new String[n];
        int start = 0;
        for (int j = s.indexOf(c), p = 0; j != -1; j = s.indexOf(c, start)) {
            ret[p++] = s.substring(start, j);
            start = j + 1;
        }
        ret[n - 1] = s.substring(start);

        return ret;
    }
}