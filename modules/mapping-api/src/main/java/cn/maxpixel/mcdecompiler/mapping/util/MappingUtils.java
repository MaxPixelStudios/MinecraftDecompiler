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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.Objects;
import java.util.function.Function;

public final class MappingUtils {
    private MappingUtils() {
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
            if (method.hasComponent(Descriptor.Unmapped.class)) {
                return method.getComponent(Descriptor.Unmapped.class).descriptor;
            } else if (remapper != null && method.hasComponent(Descriptor.Mapped.class)) {
                return remapper.unmapMethodDesc(method.getComponent(Descriptor.Mapped.class).descriptor);
            } else throw new UnsupportedOperationException();
        }

        public static PairedMapping o(String unmapped, String mapped) {
            return new PairedMapping(unmapped, mapped, new Owned<>());
        }

        public static PairedMapping duo(String unmapped, String mapped, String unmappedDesc) {
            return new PairedMapping(unmapped, mapped, new Descriptor.Unmapped(unmappedDesc), new Owned<>());
        }

        public static PairedMapping lvduo(String unmapped, String mapped, String unmappedDesc) {
            return new PairedMapping(unmapped, mapped, new LocalVariableTable.Paired(), new Descriptor.Unmapped(unmappedDesc), new Owned<>());
        }

        public static PairedMapping dmo(String unmapped, String mapped, String mappedDesc) {
            return new PairedMapping(unmapped, mapped, new Descriptor.Mapped(mappedDesc), new Owned<>());
        }

        public static PairedMapping ldmo(String unmapped, String mapped, String mappedDesc, int start, int end) {
            return new PairedMapping(unmapped, mapped, new LineNumber(start, end), new Descriptor.Mapped(mappedDesc), new Owned<>());
        }

        public static PairedMapping d2o(String unmapped, String mapped, String unmappedDesc, String mappedDesc) {
            return new PairedMapping(unmapped, mapped, new Descriptor.Unmapped(unmappedDesc), new Descriptor.Mapped(mappedDesc), new Owned<>());
        }
    }

    public static final class Namespaced {
        public static String checkTiny(String namespace0, ClassMapping<NamespacedMapping> cls, NamespacedMapping mapping) {
            if (!mapping.hasComponent(Owned.class) || !mapping.hasComponent(Descriptor.Namespaced.class))
                throw new UnsupportedOperationException();
            checkOwner(mapping.getOwned(), cls);
            Descriptor.Namespaced desc = mapping.getComponent(Descriptor.Namespaced.class);
            if (!namespace0.equals(desc.descriptorNamespace)) throw new IllegalArgumentException("Descriptor namespace mismatch");
            return desc.descriptor;
        }

        public static NamespacedMapping o(String[] namespaces, String[] names) {
            return new NamespacedMapping(namespaces, names, new Owned<>());
        }

        public static NamespacedMapping d(String[] namespaces, String[] names) {
            return new NamespacedMapping(namespaces, names, new Documented());
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

        public static NamespacedMapping dlduo(String[] namespaces, String[] names, int start, String descNamespace, String desc) {
            return new NamespacedMapping(namespaces, names, start, new Owned<>(), new Descriptor.Namespaced(desc, descNamespace),
                    new Documented(), new LocalVariableTable.Namespaced());
        }
    }

    public static String[] splitExact(String s, char c, int len) {
        int i = s.indexOf(c);
        if (i == -1) return new String[] { s };

        String[] ret = new String[len];
        int start = 0, p = 0;
        for (int j = i; p < len - 1; j = s.indexOf(c, start)) {
            ret[p++] = s.substring(start, j);
            start = j + 1;
        }
        ret[p] = s.substring(start);

        return ret;
    }

    public static String[] split(String s, char c) {
        return split(s, c, 0);
    }

    public static String[] split(String s, char c, int beginIndex) {
        int i = s.indexOf(c, beginIndex);
        if (i == -1) return new String[] { s.substring(beginIndex) };
        int n = 2;
        while ((i = s.indexOf(c, i + 1)) != -1) n++;

        String[] ret = new String[n];
        int start = beginIndex;
        for (int j = s.indexOf(c, beginIndex), p = 0; j != -1; j = s.indexOf(c, start)) {
            ret[p++] = s.substring(start, j);
            start = j + 1;
        }
        ret[n - 1] = s.substring(start);

        return ret;
    }

//    public static <I, O> O[] mapArray(I[] input, IntFunction<O[]> outputGenerator, Function<I, O> func) {
//        Objects.requireNonNull(func);
//        O[] output = Objects.requireNonNull(outputGenerator.apply(input.length));
//        for (int i = 0; i < input.length; i++) {
//            output[i] = Objects.requireNonNull(func.apply(Objects.requireNonNull(input[i])));
//        }
//        return output;
//    }

    public static boolean isStringNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    public static <T> T throwInvalidDescriptor(boolean method) {
        throw new IllegalArgumentException(method ? "Invalid method descriptor" : "Invalid descriptor");
    }

    public static BufferedReader asBufferedReader(@NotNull Reader reader) {
        return Objects.requireNonNull(reader, "reader cannot be null") instanceof BufferedReader br ?
                br : new BufferedReader(reader);
    }
}