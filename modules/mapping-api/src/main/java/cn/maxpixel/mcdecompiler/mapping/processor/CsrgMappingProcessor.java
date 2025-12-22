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

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.util.InputCollection;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public enum CsrgMappingProcessor implements MappingProcessor.Classified<PairedMapping> {
    INSTANCE;

    @Override
    public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
        return MappingFormats.CSRG;
    }

    @Override
    public ClassifiedMapping<PairedMapping> process(InputCollection contents) {
        ClassifiedMapping<PairedMapping> mappings = new ClassifiedMapping<>();
        Object2ObjectOpenHashMap<String, ClassMapping<PairedMapping>> classes = new Object2ObjectOpenHashMap<>(); // k: unmapped name
        for (var content : contents) {
            try (var lines = preprocess(content.lines().map(this::stripComments))) {
                lines.parallel().forEach(s -> {
                    String[] sa = MappingUtils.split(s, ' ');
                    switch (sa.length) {
                        case 2 -> { // Class / Package
                            if (sa[0].charAt(sa[0].length() - 1) == '/') synchronized (mappings.packages) {
                                mappings.packages.add(new PairedMapping(sa[0].substring(0, sa[0].length() - 1),
                                        sa[1].substring(0, sa[1].length() - 1)));
                            } else {
                                ClassMapping<PairedMapping> classMapping = new ClassMapping<>(new PairedMapping(sa[0], sa[1]));
                                synchronized (classes) {
                                    classes.merge(classMapping.mapping.unmappedName, classMapping, (o, n) -> {
                                        n.addFields(o.getFields());
                                        n.addMethods(o.getMethods());
                                        return n;
                                    });
                                }
                            }
                        }
                        case 3 -> { // Field
                            PairedMapping fieldMapping = MappingUtils.Paired.o(sa[1], sa[2]);
                            synchronized (classes) {
                                classes.computeIfAbsent(sa[0], MappingUtils.Paired.COMPUTE_DEFAULT_CLASS).addField(fieldMapping);
                            }
                        }
                        case 4 -> { // Method
                            PairedMapping methodMapping = MappingUtils.Paired.duo(sa[1], sa[3], sa[2]);
                            synchronized (classes) {
                                classes.computeIfAbsent(sa[0], MappingUtils.Paired.COMPUTE_DEFAULT_CLASS).addMethod(methodMapping);
                            }
                        }
                        default -> throw new IllegalArgumentException("Is this CSRG mapping format?");
                    }
                });
            }
        }
        mappings.classes.addAll(classes.values());
        return mappings;
    }
}