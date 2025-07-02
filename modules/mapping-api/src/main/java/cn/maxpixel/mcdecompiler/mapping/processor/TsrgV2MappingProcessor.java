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
import cn.maxpixel.mcdecompiler.mapping.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.mapping.component.StaticIdentifiable;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import cn.maxpixel.mcdecompiler.mapping.util.ContentList;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtils;

import java.io.IOException;
import java.util.function.Consumer;

public enum TsrgV2MappingProcessor implements MappingProcessor.Classified<NamespacedMapping> {
    INSTANCE;

    @Override
    public MappingFormat<NamespacedMapping, ClassifiedMapping<NamespacedMapping>> getFormat() {
        return MappingFormats.TSRG_V2;
    }

    @Override
    public ClassifiedMapping<NamespacedMapping> process(ContentList contents) throws IOException {
        try (var reader = contents.getAsSingle().asBufferedReader()) {
            String firstLine = getFirstLine(reader);
            if (!firstLine.startsWith("tsrg2")) error();
            String[] namespaces = MappingUtils.split(firstLine, ' ', 6);
            var trait = new NamespacedTrait(namespaces);
            trait.setUnmappedNamespace(namespaces[0]);
            ClassifiedMapping<NamespacedMapping> mappings = new ClassifiedMapping<>(trait);
            preprocess(reader.lines().map(this::stripComments)).forEach(new Consumer<>() {
                private ClassMapping<NamespacedMapping> currentClass;
                private NamespacedMapping currentMethod;

                @Override
                public void accept(String s) {
                    if (s.charAt(0) == '\t') {
                        if (s.charAt(1) == '\t') {
                            if (s.equals("\t\tstatic")) {
                                currentMethod.getComponent(StaticIdentifiable.class).setStatic(true);
                                return;
                            }
                            String[] sa = MappingUtils.split(s, ' ', 2);
                            currentMethod.getComponent(LocalVariableTable.Namespaced.class)
                                    .setLocalVariable(Integer.parseInt(sa[0]), new NamespacedMapping(namespaces, sa, 1));
                            return;
                        }
                        currentMethod = processTree(namespaces, s, currentClass);
                        return;
                    }
                    String[] sa = MappingUtils.split(s, ' ');
                    if (sa[0].charAt(sa[0].length() - 1) == '/') {
                        for (int j = 0; j < sa.length; j++) sa[j] = sa[j].substring(0, sa[j].length() - 1);
                        mappings.packages.add(new NamespacedMapping(namespaces, sa));
                    } else {
                        ClassMapping<NamespacedMapping> classMapping = new ClassMapping<>(new NamespacedMapping(namespaces, sa));
                        mappings.classes.add(classMapping);
                        currentClass = classMapping;
                    }
                }
            });
            mappings.updateCollection();
            return mappings;
        }
    }

    private static NamespacedMapping processTree(String[] namespaces, String s, ClassMapping<NamespacedMapping> classMapping) {
        String[] sa = MappingUtils.split(s, ' ', 1);
        switch (sa.length - namespaces.length) {
            case 0 -> classMapping.addField(MappingUtils.Namespaced.o(namespaces, sa));
            case 1 -> {
                String desc = sa[1];
                sa[1] = sa[0];
                if (desc.charAt(0) == '(') {
                    NamespacedMapping methodMapping = MappingUtils.Namespaced.slduo(
                            namespaces, sa, 1, namespaces[0], desc);
                    classMapping.addMethod(methodMapping);
                    return methodMapping;
                } else {
                    classMapping.addField(MappingUtils.Namespaced.duo(namespaces,
                            sa, 1, namespaces[0], desc));
                }
            }
            default -> error();
        }
        return null;
    }

    private static void error() {
        throw new IllegalArgumentException("Is this TSRG v2 mapping format?");
    }
}