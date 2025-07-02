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
import cn.maxpixel.mcdecompiler.mapping.util.ContentList;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.function.Function;

public enum SrgMappingProcessor implements MappingProcessor.Classified<PairedMapping> {
    INSTANCE;

    private static final Function<String, Function<String, ClassMapping<PairedMapping>>> MAPPING_FUNC = s ->
            k -> new ClassMapping<>(new PairedMapping(k, getClassName(s)));

    @Override
    public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
        return MappingFormats.SRG;
    }

    @Override
    public ClassifiedMapping<PairedMapping> process(ContentList contents) {
        ClassifiedMapping<PairedMapping> mappings = new ClassifiedMapping<>();
        Object2ObjectOpenHashMap<String, ClassMapping<PairedMapping>> classes = new Object2ObjectOpenHashMap<>(); // k: unmapped name
        for (var content : contents) {
            try (var lines = preprocess(content.lines().map(this::stripComments))) {
                lines.parallel().forEach(s -> {
                    String[] strings = MappingUtils.split(s, ' ');
                    switch (strings[0]) {
                        case "CL:" -> {
                            ClassMapping<PairedMapping> classMapping = new ClassMapping<>(new PairedMapping(strings[1], strings[2]));
                            synchronized (classes) {
                                classes.putIfAbsent(strings[1], classMapping);
                            }
                        }
                        case "FD:" -> {
                            PairedMapping fieldMapping = MappingUtils.Paired.o(getName(strings[1]), getName(strings[2]));
                            String unmClassName = getClassName(strings[1]);
                            synchronized (classes) {
                                classes.computeIfAbsent(unmClassName, MAPPING_FUNC.apply(strings[2]))
                                        .addField(fieldMapping);
                            }
                        }
                        case "MD:" -> {
                            PairedMapping methodMapping = MappingUtils.Paired.d2o(getName(strings[1]), getName(strings[3]), strings[2], strings[4]);
                            String unmClassName = getClassName(strings[1]);
                            synchronized (classes) {
                                classes.computeIfAbsent(unmClassName, MAPPING_FUNC.apply(strings[3]))
                                        .addMethod(methodMapping);
                            }
                        }
                        case "PK:" -> {
                            synchronized (mappings.packages) {
                                mappings.packages.add(new PairedMapping(strings[1], strings[2]));
                            }
                        }
                        default -> throw new IllegalArgumentException("Is this SRG mapping format?");
                    }
                });
            }
        }
        mappings.classes.addAll(classes.values());
        return mappings;
    }

    private static String getClassName(String s) {
        return s.substring(0, s.lastIndexOf('/'));
    }

    private static String getName(String s) {
        return s.substring(s.lastIndexOf('/') + 1);
    }
}