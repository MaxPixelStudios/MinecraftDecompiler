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

package cn.maxpixel.mcdecompiler.asm.variable;

import cn.maxpixel.mcdecompiler.asm.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.mapping.component.StaticIdentifiable;
import cn.maxpixel.mcdecompiler.util.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.stream.Collectors;

public class MappingVariableNameProvider<T extends Mapping> implements VariableNameProvider {
    private static final String PLACEHOLDER_CHARS = "o";
    private final Object2ObjectOpenHashMap<String, T> methodByMappedName;
    private boolean omitThis;

    public MappingVariableNameProvider(@NotNull ClassMapping<T> mapping, @NotNull ClassifiedMappingRemapper remapper) {
        this.methodByMappedName = mapping.getMethods().stream().collect(Collectors.toMap(m -> {
            String descriptor;
            if (m.hasComponent(Descriptor.class)) descriptor = remapper.getMappedDescByUnmappedDesc(m.getComponent(Descriptor.class).unmappedDescriptor);
            else if (m.hasComponent(Descriptor.Mapped.class)) descriptor = m.getComponent(Descriptor.Mapped.class).mappedDescriptor;
            else if (m.hasComponent(Descriptor.Namespaced.class)) descriptor = remapper.getMappedDescByUnmappedDesc(m.getComponent(Descriptor.Namespaced.class).unmappedDescriptor);
            else throw new IllegalArgumentException("Method mapping requires at least one of the descriptor components");
            if (!omitThis && m.hasComponent(StaticIdentifiable.class)) omitThis = true;
            return m.getMappedName().concat(descriptor);
        }, Function.identity(), Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    @Override
    public @NotNull RenameFunction forMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        T mapping = methodByMappedName.get(name.concat(descriptor));
        if (mapping != null) {
            if (mapping.hasComponent(LocalVariableTable.class)) {
                LocalVariableTable lvt = mapping.getComponent(LocalVariableTable.class);
                return (originalName, descriptor1, signature1, start, end, index) -> {
                    PairedMapping m = lvt.getLocalVariable(index);
                    return m != null && !m.mappedName.isBlank() && !PLACEHOLDER_CHARS.contains(m.mappedName)
                            ? m.mappedName : null;
                };
            } else if (mapping.hasComponent(LocalVariableTable.Namespaced.class)) {
                LocalVariableTable.Namespaced lvt = mapping.getComponent(LocalVariableTable.Namespaced.class);
                return (originalName, descriptor1, signature1, start, end, index) -> {
                    NamespacedMapping m = lvt.getLocalVariable(index);
                    return m != null && !m.getMappedName().isBlank() && !PLACEHOLDER_CHARS.contains(m.getMappedName())
                            ? m.getMappedName() : null;
                };
            }
        }
        return RenameFunction.NOP;
    }

    @Override
    public @NotNull RenameAbstractFunction forAbstractMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return (i, t) -> forMethod(access, name, descriptor, signature, exceptions).getName(null, null, null, null, null, i);
    }

    @Override
    public boolean omitThis() {
        return omitThis;
    }
}