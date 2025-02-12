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

package cn.maxpixel.mcdecompiler.mapping.remapper;

import cn.maxpixel.mcdecompiler.common.util.Utils;
import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.NameGetter;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.component.StaticIdentifiable;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.stream.Collectors;

public class ClassifiedMappingRemapper implements MappingRemapper {
    private final Object2ObjectOpenHashMap<String, ? extends Object2ObjectOpenHashMap<String, ? extends Mapping>> fieldByUnm;
    private final Object2ObjectOpenHashMap<String, ? extends Object2ObjectOpenHashMap<String,
            ? extends Object2ObjectOpenHashMap<String, ? extends Mapping>>> methodsByUnm;
    private final Object2ObjectOpenHashMap<String, ? extends ClassMapping<? extends Mapping>> mappingByUnm;
    private final Object2ObjectOpenHashMap<String, ? extends ClassMapping<? extends Mapping>> mappingByMap;
    private final DescriptorRemapper descriptorRemapper;
    private boolean methodStaticIdentifiable;

    public ClassifiedMappingRemapper(ClassifiedMapping<?> mappings) {
        this.fieldByUnm = genFieldsByUnmappedNameMap(mappings.classes);
        this.mappingByUnm = genMappingsByUnmappedNameMap(mappings.classes);
        this.mappingByMap = genMappingsByMappedNameMap(mappings.classes);
        this.descriptorRemapper = new DescriptorRemapper(mappingByUnm, mappingByMap);
        var namespaced = mappings.getTrait(NamespacedTrait.class);
        var remapperMap = namespaced != null ? new Object2ObjectOpenHashMap<String, UniDescriptorRemapper>() : null;
        this.methodsByUnm = mappings.classes.parallelStream().collect(Collectors.toMap(cm -> cm.mapping.getUnmappedName(), cm -> {
            Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, Mapping>> map =
                    new Object2ObjectOpenHashMap<>();
            for (Mapping mm : cm.getMethods()) {
                var m = map.computeIfAbsent(mm.getUnmappedName(), k -> new Object2ObjectOpenHashMap<>());
                if (!methodStaticIdentifiable && mm.hasComponent(StaticIdentifiable.class)) methodStaticIdentifiable = true;
                if (m.putIfAbsent(namespaced == null ? getUnmappedDesc(mm) : getUnmappedDesc(mm, namespaced.getUnmappedNamespace(),
                        remapperMap, (ClassifiedMapping<NamespacedMapping>) mappings), mm) != null) {
                    throw new IllegalArgumentException("Method duplicated... This should not happen!");
                }
            }
            return map;
        }, Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    @Override
    public boolean hasClassMapping(String name) {
        return mappingByUnm.containsKey(name);
    }

    @Override
    public boolean isMethodStaticIdentifiable() {
        return methodStaticIdentifiable;
    }

    @Override
    public @Nullable("When no corresponding mapping found") String mapClass(@NotNull String name) {
        ClassMapping<? extends Mapping> classMapping = mappingByUnm.get(name);
        if (classMapping != null) return classMapping.mapping.getMappedName();
        return null;
    }

    @Override
    public @Nullable("When no corresponding mapping found") String unmapClass(@NotNull String name) {
        ClassMapping<? extends Mapping> classMapping = mappingByMap.get(name);
        if (classMapping != null) return classMapping.mapping.getUnmappedName();
        return null;
    }

    @Override
    public @Nullable("When no corresponding mapping found") String mapField(@NotNull String owner, @NotNull String name) {
        var fields = fieldByUnm.get(owner);
        if (fields != null) {
            var mapping = fields.get(name);
            if (mapping != null) return mapping.getMappedName();
        }
        return null;
    }

    @Override
    public @Nullable("When no corresponding mapping found") String mapMethod(@NotNull String owner, @NotNull String name,
                                     @Nullable("When desc doesn't matter") String desc) {
        var methods = methodsByUnm.get(owner);
        if (methods != null) {
            var mappings = methods.get(name);
            if (mappings != null) {
                if (desc == null) {
                    if (!mappings.isEmpty()) return mappings.values().iterator().next().getMappedName();
                } else {
                    var mapping = mappings.get(desc);
                    if (mapping != null) return mapping.getMappedName();
                }
            }
        }
        return null;
    }

    @Override
    public DescriptorRemapper getDescriptorRemapper() {
        return descriptorRemapper;
    }

    public ClassMapping<? extends Mapping> getClassMappingUnmapped(@NotNull String name) {
        return mappingByUnm.get(name);
    }

    public String getUnmappedDesc(Mapping mapping) {
        if (mapping.hasComponent(Descriptor.Unmapped.class)) return mapping.getComponent(Descriptor.Unmapped.class).descriptor;
        else if (mapping.hasComponent(Descriptor.Mapped.class))
            return unmapMethodDesc(mapping.getComponent(Descriptor.Mapped.class).descriptor);
        else throw new IllegalArgumentException("Mapping for methods must support at least one of the descriptor components");
    }

    public String getUnmappedDesc(Mapping mapping, String unmappedNamespace, Object2ObjectOpenHashMap<String, UniDescriptorRemapper> map,
                                  ClassifiedMapping<NamespacedMapping> mappings) {
        var desc = mapping.getComponent(Descriptor.Namespaced.class);
        if (desc != null) return unmappedNamespace.equals(desc.descriptorNamespace) ? desc.descriptor : map
                .computeIfAbsent(desc.descriptorNamespace, (String n) -> new UniDescriptorRemapper(genMappingsByNamespaceMap(mappings.classes, n)))
                .unmapMethodDesc(desc.descriptor);
        else throw new IllegalArgumentException("Mapping for methods must support at least one of the descriptor components");
    }

    public static <T extends Mapping> Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, T>> genFieldsByUnmappedNameMap(
            ObjectList<ClassMapping<T>> mapping) {
        return mapping.parallelStream().collect(Collectors.toMap(
                cm -> cm.mapping.getUnmappedName(),
                cm -> cm.getFields().parallelStream().collect(Collectors.toMap(NameGetter::getUnmappedName, Function.identity(),
                        Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new)),
                Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    public static <T extends Mapping> Object2ObjectOpenHashMap<String, ClassMapping<T>> genMappingsByUnmappedNameMap(
            ObjectList<ClassMapping<T>> mapping) {
        return mapping.parallelStream().collect(Collectors.toMap(cm -> cm.mapping.getUnmappedName(),
                Function.identity(), Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    public static <T extends Mapping> Object2ObjectOpenHashMap<String, ClassMapping<T>> genMappingsByMappedNameMap(
            ObjectList<ClassMapping<T>> mapping) {
        return mapping.parallelStream().collect(Collectors.toMap(cm -> cm.mapping.getMappedName(),
                Function.identity(), Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }

    public static Object2ObjectOpenHashMap<String, ClassMapping<NamespacedMapping>> genMappingsByNamespaceMap(
            ObjectList<ClassMapping<NamespacedMapping>> mapping, String namespace) {
        return mapping.parallelStream().collect(Collectors.toMap(m -> m.mapping.getName(namespace),
                Function.identity(), Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new));
    }
}