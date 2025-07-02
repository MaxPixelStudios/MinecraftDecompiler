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
import cn.maxpixel.mcdecompiler.mapping.component.Documented;
import cn.maxpixel.mcdecompiler.mapping.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import cn.maxpixel.mcdecompiler.mapping.trait.PropertiesTrait;
import cn.maxpixel.mcdecompiler.mapping.util.ContentList;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtils;
import cn.maxpixel.mcdecompiler.mapping.util.TinyUtil;

import java.io.IOException;
import java.util.function.Consumer;

public enum TinyV2MappingProcessor implements MappingProcessor.Classified<NamespacedMapping> {
    INSTANCE;

    @Override
    public MappingFormat<NamespacedMapping, ClassifiedMapping<NamespacedMapping>> getFormat() {
        return MappingFormats.TINY_V2;
    }

    @Override
    public ClassifiedMapping<NamespacedMapping> process(ContentList contents) throws IOException {
        try (var reader = contents.getAsSingle().asBufferedReader()) {
            String firstLine = reader.readLine();
            if (!firstLine.startsWith("tiny\t2\t0")) error();
            String[] namespaces = MappingUtils.split(firstLine, '\t', 9);
            var trait = new NamespacedTrait(namespaces);
            trait.setUnmappedNamespace(namespaces[0]);
            ClassifiedMapping<NamespacedMapping> mappings = new ClassifiedMapping<>(trait);
            preprocess(reader.lines()).forEach(new Consumer<>() {
                private ClassMapping<NamespacedMapping> currentClass;
                private NamespacedMapping currentMember;
                private NamespacedMapping currentLocalVariable;

                @Override
                public void accept(String s) {
                    if (s.charAt(0) == '\t') {
                        if (currentClass == null) {
                            String[] sa = MappingUtils.split(s, '\t', 1);
                            var props = mappings.getOrCreateTrait(PropertiesTrait.class, PropertiesTrait::new);
                            if (sa.length == 2) props.setProperty(sa[0], TinyUtil.unescape(sa[1]));
                            else props.addProperty(sa[0]);
                            return;
                        }
                        if (s.charAt(1) == '\t') {
                            if (s.charAt(2) == '\t') {
                                if (s.charAt(3) == 'c') {
                                    currentLocalVariable.getComponent(Documented.class)
                                            .setContentString(TinyUtil.unescape(s, 5));
                                } else error();
                                return;
                            }
                            currentLocalVariable = processTree1(namespaces, s, currentMember);
                            return;
                        }
                        currentMember = processTree(namespaces, s, currentClass);
                        return;
                    }
                    if (s.charAt(0) == 'c') {
                        String[] sa = MappingUtils.split(s, '\t', 2);
                        ClassMapping<NamespacedMapping> classMapping = new ClassMapping<>(MappingUtils.Namespaced.d(namespaces, sa));
                        mappings.classes.add(classMapping);
                        currentClass = classMapping;
                    } else error();
                }
            });
            mappings.updateCollection();
            return mappings;
        }
    }

    private static NamespacedMapping processTree(String[] namespaces, String s, ClassMapping<NamespacedMapping> classMapping) {
        String[] sa = MappingUtils.split(s, '\t', 3);
        switch (s.charAt(1)) {
            case 'c' -> classMapping.mapping.getComponent(Documented.class).setContentString(TinyUtil.unescape(sa[0]));
            case 'f' -> {
                NamespacedMapping fieldMapping = MappingUtils.Namespaced.dduo(namespaces, sa, 1, namespaces[0], sa[0]);
                classMapping.addField(fieldMapping);
                return fieldMapping;
            }
            case 'm' -> {
                NamespacedMapping methodMapping = MappingUtils.Namespaced.dlduo(namespaces, sa, 1, namespaces[0], sa[0]);
                classMapping.addMethod(methodMapping);
                return methodMapping;
            }
            default -> error();
        }
        return null;
    }

    private static NamespacedMapping processTree1(String[] namespaces, String s, NamespacedMapping mapping) {
        switch (s.charAt(2)) {
            case 'c' -> mapping.getComponent(Documented.class).setContentString(TinyUtil.unescape(s, 4));
            case 'p' -> {
                String[] sa = MappingUtils.split(s, '\t', 4);
                NamespacedMapping localVariable = MappingUtils.Namespaced.d(namespaces, sa, 1);
                mapping.getComponent(LocalVariableTable.Namespaced.class)
                        .setLocalVariable(Integer.parseInt(sa[0]), localVariable);
                return localVariable;
            }
            default -> error();
        }
        return null;
    }

    private static void error() {
        throw new IllegalArgumentException("Is this Tiny v2 mapping format?");
    }
}