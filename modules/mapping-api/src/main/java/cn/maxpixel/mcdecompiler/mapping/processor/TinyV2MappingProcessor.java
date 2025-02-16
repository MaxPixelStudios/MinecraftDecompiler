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
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtil;
import cn.maxpixel.mcdecompiler.mapping.util.TinyUtil;
import it.unimi.dsi.fastutil.objects.ObjectList;

public enum TinyV2MappingProcessor implements MappingProcessor.Classified<NamespacedMapping> {
    INSTANCE;

    @Override
    public MappingFormat<NamespacedMapping, ClassifiedMapping<NamespacedMapping>> getFormat() {
        return MappingFormats.TINY_V2;
    }

    @Override
    public ClassifiedMapping<NamespacedMapping> process(ObjectList<String> content) {
        if (!content.get(0).startsWith("tiny\t2\t0")) error();
        String[] namespaces = MappingUtil.split(content.get(0), '\t', 9);
        var trait = new NamespacedTrait(namespaces);
        trait.setUnmappedNamespace(namespaces[0]);
        ClassifiedMapping<NamespacedMapping> mappings = new ClassifiedMapping<>(trait);
        for (int i = 1, len = content.size(); i < len; ) {
            String[] sa = MappingUtil.split(content.get(i), '\t');
            if (sa[0].length() == 1 && sa[0].charAt(0) == 'c') {
                ClassMapping<NamespacedMapping> classMapping = new ClassMapping<>(MappingUtil.Namespaced.d(namespaces, sa, 1));
                i = processTree(i, len, namespaces, content, classMapping);
                mappings.classes.add(classMapping);
            } else if (sa[0].isEmpty()) {
                var props = mappings.getOrCreateTrait(PropertiesTrait.class, PropertiesTrait::new);
                if (sa.length == 3) props.setProperty(sa[1], TinyUtil.unescape(sa[2]));
                else props.addProperty(sa[1]);
                i++;
            } else error();
        }
        mappings.updateCollection();
        return mappings;
    }

    private static int processTree(int index, int size, String[] namespaces, ObjectList<String> content,
                                   ClassMapping<NamespacedMapping> classMapping) {
        for (index = index + 1; index < size; index++) {
            String s = content.get(index);
            if (s.charAt(0) == '\t') {
                String[] sa = MappingUtil.split(s, '\t', 3);
                switch (s.charAt(1)) {
                    case 'c' -> classMapping.mapping.getComponent(Documented.class).setContentString(TinyUtil.unescape(sa[0]));
                    case 'f' -> {
                        NamespacedMapping fieldMapping = MappingUtil.Namespaced.dduo(namespaces, sa, 1, namespaces[0], sa[0]);
                        index = processTree1(index, size, namespaces, content, fieldMapping);
                        classMapping.addField(fieldMapping);
                    }
                    case 'm' -> {
                        NamespacedMapping methodMapping = MappingUtil.Namespaced.dlduo(namespaces, sa, 1, namespaces[0], sa[0]);
                        index = processTree1(index, size, namespaces, content, methodMapping);
                        classMapping.addMethod(methodMapping);
                    }
                    default -> error();
                }
            } else break;
        }
        return index;
    }

    private static int processTree1(int index, int size, String[] namespaces, ObjectList<String> content,
                                    NamespacedMapping mapping) {
        for (index = index + 1; index < size; index++) {
            String s = content.get(index);
            if (s.startsWith("\t\t")) {
                switch (s.charAt(2)) {
                    case 'c' -> mapping.getComponent(Documented.class).setContentString(TinyUtil.unescape(s, 4));
                    case 'p' -> {
                        String[] sa = MappingUtil.split(s, '\t', 4);
                        NamespacedMapping localVariable = MappingUtil.Namespaced.d(namespaces, sa, 1);
                        mapping.getComponent(LocalVariableTable.Namespaced.class)
                                .setLocalVariable(Integer.parseInt(sa[0]), localVariable);
                        index = processTree2(index, size, content, localVariable);
                    }
                    default -> error();
                }
            } else break;
        }
        return index - 1;
    }

    private static int processTree2(int index, int size, ObjectList<String> content, NamespacedMapping localVariable) {
        if (++index < size) {
            String s = content.get(index);
            if (s.startsWith("\t\t\t")) {
                if (s.charAt(3) == 'c') localVariable.getComponent(Documented.class).setContentString(TinyUtil.unescape(s, 5));
                else error();
                return index;
            }
        }
        return index - 1;
    }

    private static void error() {
        throw new IllegalArgumentException("Is this a Tiny v2 mapping file?");
    }
}