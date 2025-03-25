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

package cn.maxpixel.mcdecompiler.mapping.processor;

import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.List;
import java.util.function.Function;

public enum TinyV1MappingProcessor implements MappingProcessor.Classified<NamespacedMapping> {
    INSTANCE;

    private static final Function<String[], Function<String, ClassMapping<NamespacedMapping>>> MAPPING_FUNC = (namespaces) ->
            key -> new ClassMapping<>(new NamespacedMapping(namespaces, key));

    @Override
    public MappingFormat<NamespacedMapping, ClassifiedMapping<NamespacedMapping>> getFormat() {
        return MappingFormats.TINY_V1;
    }

    @Override
    public ClassifiedMapping<NamespacedMapping> process(List<String> content) {
        if (!content.get(0).startsWith("v1")) error();
        String[] namespaces = MappingUtil.split(content.get(0), '\t', 3);
        var trait = new NamespacedTrait(namespaces);
        trait.setUnmappedNamespace(namespaces[0]);
        ClassifiedMapping<NamespacedMapping> mappings = new ClassifiedMapping<>(trait);
        Object2ObjectOpenHashMap<String, ClassMapping<NamespacedMapping>> classes = new Object2ObjectOpenHashMap<>(); // k: the first namespace, usually unmapped name
        String k = namespaces[0];
        content.parallelStream().skip(1).forEach(s -> {
            String[] sa = MappingUtil.split(s, '\t');
            if (s.startsWith("CLASS")) {
                ClassMapping<NamespacedMapping> classMapping = new ClassMapping<>(new NamespacedMapping(namespaces, sa, 1));
                synchronized (classes) {
                    classes.merge(sa[1], classMapping, (o, n) -> n.addFields(o.getFields()).addMethods(o.getMethods()));
                }
            } else if (s.startsWith("FIELD")) {
                NamespacedMapping fieldMapping = MappingUtil.Namespaced.duo(namespaces, sa, 3, k, sa[2]);
                synchronized (classes) {
                    classes.computeIfAbsent(sa[1], MAPPING_FUNC.apply(namespaces))
                            .addField(fieldMapping);
                }
            } else if (s.startsWith("METHOD")) {
                NamespacedMapping methodMapping = MappingUtil.Namespaced.duo(namespaces, sa, 3, k, sa[2]);
                synchronized (classes) {
                    classes.computeIfAbsent(sa[1], MAPPING_FUNC.apply(namespaces))
                            .addMethod(methodMapping);
                }
            } else error();
        });
        mappings.classes.addAll(classes.values());
        mappings.updateCollection();
        return mappings;
    }

    private static void error() {
        throw new IllegalArgumentException("Is this a Tiny v1 mapping file?");
    }
}