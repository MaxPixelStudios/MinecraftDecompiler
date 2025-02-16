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

package cn.maxpixel.mcdecompiler.mapping.generator;

import cn.maxpixel.mcdecompiler.common.util.NamingUtil;
import cn.maxpixel.mcdecompiler.common.util.Utils;
import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Documented;
import cn.maxpixel.mcdecompiler.mapping.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.mapping.component.StaticIdentifiable;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import cn.maxpixel.mcdecompiler.mapping.trait.PropertiesTrait;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtil;
import cn.maxpixel.mcdecompiler.mapping.util.TinyUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public enum TinyV2MappingGenerator implements MappingGenerator.Classified<NamespacedMapping> {
    INSTANCE;

    @Override
    public MappingFormat<NamespacedMapping, ClassifiedMapping<NamespacedMapping>> getFormat() {
        return MappingFormats.TINY_V2;
    }

    @Override
    public ObjectList<String> generate(ClassifiedMapping<NamespacedMapping> mappings, ClassifiedMappingRemapper remapper) {
        ObjectArrayList<String> lines = new ObjectArrayList<>();
        if (mappings.classes.isEmpty()) return lines;
        var namespaces = mappings.getTrait(NamespacedTrait.class).namespaces;
        String namespace0 = namespaces.first();
        lines.add("tiny\t2\t0\t" + String.join("\t", namespaces));
        var props = mappings.getTrait(PropertiesTrait.class);
        if (props != null) {
            for (String property : props.properties) lines.add('\t' + TinyUtil.ensureSafe(property));
            props.propertiesWithValue.forEach((k, v) -> lines.add('\t' + TinyUtil.ensureSafe(k) + '\t' + TinyUtil.escape(v)));
        }
        for (ClassMapping<NamespacedMapping> cls : mappings.classes) {
            lines.add("c\t" + NamingUtil.concatNamespaces(namespaces, cls.mapping::getName, "\t"));
            var classDoc = cls.mapping.getComponent(Documented.class);
            if (classDoc != null) {
                String content = classDoc.getContentString();
                if (!content.isBlank()) lines.add("\tc\t" + TinyUtil.escape(content));
            }
            cls.getFields().parallelStream().forEach(field -> {
                String desc = MappingUtil.Namespaced.checkTiny(namespace0, cls, field);
                synchronized (lines) {
                    lines.add("\tf\t" + desc + '\t' + NamingUtil.concatNamespaces(namespaces, field::getName, "\t"));
                    var fieldDoc = field.getComponent(Documented.class);
                    if (fieldDoc != null) {
                        String doc = fieldDoc.getContentString();
                        if (!doc.isBlank()) lines.add("\t\tc\t" + TinyUtil.escape(doc));
                    }
                }
            });
            cls.getMethods().parallelStream().forEach(method -> {
                String desc = MappingUtil.Namespaced.checkTiny(namespace0, cls, method);
                synchronized (lines) {
                    lines.add("\tm\t" + desc + '\t' + NamingUtil.concatNamespaces(namespaces, method::getName, "\t"));
                    var methodDoc = method.getComponent(Documented.class);
                    if (methodDoc != null) {
                        String doc = methodDoc.getContentString();
                        if (!doc.isBlank()) lines.add("\t\tc\t" + TinyUtil.escape(doc));
                    }
                    if (method.hasComponent(LocalVariableTable.Namespaced.class)) {
                        LocalVariableTable.Namespaced lvt = method.getComponent(LocalVariableTable.Namespaced.class);
                        boolean omittedThis = method.hasComponent(StaticIdentifiable.class) &&
                                !method.getComponent(StaticIdentifiable.class).isStatic;
                        lvt.getLocalVariableIndexes().forEach(index -> {
                            NamespacedMapping localVariable = lvt.getLocalVariable(omittedThis ? index + 1 : index);
                            String names = NamingUtil.concatNamespaces(namespaces, namespace -> {
                                String name = localVariable.getName(namespace);
                                return Utils.isStringNotBlank(name) ? name : "";
                            }, "\t");
                            lines.add("\t\tp\t" + index + '\t' + names);
                            var paramDoc = localVariable.getComponent(Documented.class);
                            if (paramDoc != null) {
                                String doc = paramDoc.getContentString();
                                if (!doc.isBlank()) lines.add("\t\t\tc\t" + TinyUtil.escape(doc));
                            }
                        });
                    }
                }
            });
        }
        return lines;
    }
}