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

package cn.maxpixel.mcdecompiler.remapper.variable;

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.mapping.remapper.MappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtils;
import cn.maxpixel.mcdecompiler.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.stream.Collectors;

public class MappingVariableNameProvider implements VariableNameProvider {
    private static final String PLACEHOLDER_CHARS = "o";
    private final Object2ObjectOpenHashMap<String, Mapping> methodByMappedName;
    private final boolean omitThis;

    public MappingVariableNameProvider(@NotNull ClassMapping<? extends Mapping> mapping, @NotNull MappingRemapper remapper) {
        this.omitThis = remapper.isMethodStaticIdentifiable();
        this.methodByMappedName = mapping.getMethods().stream().collect(Collectors.toMap(m -> {
            String descriptor;
            if (m.hasComponent(Descriptor.Unmapped.class)) descriptor = remapper.mapMethodDesc(m.getComponent(Descriptor.Unmapped.class).descriptor);
            else if (m.hasComponent(Descriptor.Mapped.class)) descriptor = m.getComponent(Descriptor.Mapped.class).descriptor;
            else if (m.hasComponent(Descriptor.Namespaced.class)) descriptor = remapper.mapMethodDesc(m.getComponent(Descriptor.Namespaced.class).descriptor);
            else throw new IllegalArgumentException("Method mapping requires at least one of the descriptor components");
            return m.getMappedName().concat(descriptor);
        }, Function.identity(), Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    @Override
    public @NotNull RenameFunction forMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        Mapping mapping = methodByMappedName.get(name.concat(descriptor));
        if (mapping != null) {
            LocalVariableTable<? extends Mapping> lvt;
            if (mapping.hasComponent(LocalVariableTable.Paired.class)) {
                lvt = mapping.getComponent(LocalVariableTable.Paired.class);
            } else if (mapping.hasComponent(LocalVariableTable.Namespaced.class)) {
                lvt = mapping.getComponent(LocalVariableTable.Namespaced.class);
            } else return RenameFunction.NOP;
            return (originalName, descriptor1, signature1, start, end, index) -> {
                Mapping m = lvt.getLocalVariable(index);
                if (m != null) {
                    String mapped = m.getMappedName();
                    return MappingUtils.isStringNotBlank(mapped) && !PLACEHOLDER_CHARS.contains(mapped) ? mapped : null;
                }
                return null;
            };
        }
        return RenameFunction.NOP;
    }

    @Override
    public @NotNull RenameAbstractFunction forAbstractMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        RenameFunction rf = forMethod(access, name, descriptor, signature, exceptions);
        return (i, t) -> rf.getName(null, null, null, null, null, i);
    }

    @Override
    public boolean omitThis() {
        return omitThis;
    }
}