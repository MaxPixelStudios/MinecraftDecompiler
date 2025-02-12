/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2025 MaxPixelStudios(XiaoPangxie732)
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

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

/**
 * Lightweight remapper for descriptors in place of the general heavyweight {@link MappingRemapper}s.
 */
public class DescriptorRemapper extends UniDescriptorRemapper {
    private final Object2ObjectOpenHashMap<String, ? extends ClassMapping<? extends Mapping>> mappingByMap;

    public DescriptorRemapper(Object2ObjectOpenHashMap<String, ? extends ClassMapping<? extends Mapping>> mappingByUnm,
                              Object2ObjectOpenHashMap<String, ? extends ClassMapping<? extends Mapping>> mappingByMap) {
        super(mappingByUnm);
        this.mappingByMap = mappingByMap;
    }

    public DescriptorRemapper(ClassifiedMapping<?> collection) {
        this(ClassifiedMappingRemapper.genMappingsByUnmappedNameMap(collection.classes),
                ClassifiedMappingRemapper.genMappingsByMappedNameMap(collection.classes));
    }

    public DescriptorRemapper(ClassifiedMapping<NamespacedMapping> collection, String targetNamespace) {
        this(setup(collection, targetNamespace));
    }

    private static ClassifiedMapping<NamespacedMapping> setup(ClassifiedMapping<NamespacedMapping> collection, String targetNamespace) {
        var trait = collection.getTrait(NamespacedTrait.class);
        trait.setMappedNamespace(targetNamespace);
        trait.setFallbackNamespace(trait.getUnmappedNamespace());
        collection.updateCollection();
        return collection;
    }

    @Override
    protected String unmapClass(@NotNull String name) {
        ClassMapping<? extends Mapping> classMapping = mappingByMap.get(name);
        if (classMapping != null) return classMapping.mapping.getUnmappedName();
        return name;
    }
}