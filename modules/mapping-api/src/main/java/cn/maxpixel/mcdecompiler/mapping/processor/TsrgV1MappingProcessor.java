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

import java.util.function.Consumer;

public enum TsrgV1MappingProcessor implements MappingProcessor.Classified<PairedMapping> {
    INSTANCE;

    @Override
    public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
        return MappingFormats.TSRG_V1;
    }

    @Override
    public ClassifiedMapping<PairedMapping> process(ContentList contents) {
        ClassifiedMapping<PairedMapping> mappings = new ClassifiedMapping<>();
        for (var content : contents) {
            try (var lines = preprocess(content.lines().map(this::stripComments))) {
                lines.forEach(new Consumer<>() {
                    private ClassMapping<PairedMapping> currentClass;

                    @Override
                    public void accept(String s) {
                        if (s.charAt(0) == '\t') {
                            String[] sa = MappingUtils.split(s, ' ', 1);
                            switch (sa.length) {
                                case 2 -> currentClass.addField(MappingUtils.Paired.o(sa[0], sa[1]));
                                case 3 -> currentClass.addMethod(MappingUtils.Paired.duo(sa[0], sa[2], sa[1]));
                                default -> error();
                            }
                            return;
                        }
                        String[] sa = MappingUtils.split(s, ' ');
                        if (sa[0].charAt(sa[0].length() - 1) == '/') {
                            mappings.packages.add(new PairedMapping(sa[0].substring(0, sa[0].length() - 1),
                                    sa[1].substring(0, sa[1].length() - 1)));
                        } else {
                            ClassMapping<PairedMapping> classMapping = new ClassMapping<>(new PairedMapping(sa[0], sa[1]));
                            mappings.classes.add(classMapping);
                            currentClass = classMapping;
                        }
                    }
                });
            }
        }
        return mappings;
    }

    private static void error() {
        throw new IllegalArgumentException("Is this TSRG v1 mapping format?");
    }
}