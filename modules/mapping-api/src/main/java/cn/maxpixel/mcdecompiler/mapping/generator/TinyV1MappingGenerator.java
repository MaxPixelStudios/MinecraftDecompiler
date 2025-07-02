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

import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.trait.NamespacedTrait;
import cn.maxpixel.mcdecompiler.mapping.trait.PropertiesTrait;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtils;
import cn.maxpixel.mcdecompiler.mapping.util.NamingUtil;
import cn.maxpixel.mcdecompiler.mapping.util.TinyUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public enum TinyV1MappingGenerator implements MappingGenerator.Classified<NamespacedMapping> {
    INSTANCE;

    @Override
    public MappingFormat<NamespacedMapping, ClassifiedMapping<NamespacedMapping>> getFormat() {
        return MappingFormats.TINY_V1;
    }

    @Override
    public ObjectList<String> generate(ClassifiedMapping<NamespacedMapping> mappings, ClassifiedMappingRemapper remapper) {
        ObjectArrayList<String> lines = new ObjectArrayList<>();
        if (mappings.classes.isEmpty()) return lines;
        var namespaces = mappings.getTrait(NamespacedTrait.class).namespaces;
        String namespace0 = namespaces.first();
        lines.add("v1\t" + String.join("\t", namespaces));
        var props = mappings.getTrait(PropertiesTrait.class);
        if (props != null) {
            for (String property : props.properties) lines.add("# " + TinyUtil.ensureSafe(property));
            props.propertiesWithValue.forEach((k, v) ->
                    lines.add("# " + TinyUtil.ensureSafe(k) + ' ' + TinyUtil.ensureSafeAndSpaceless(v)));
        }
        mappings.classes.parallelStream().forEach(cls -> {
            NamespacedMapping classMapping = cls.mapping;
            synchronized (lines) {
                lines.add("CLASS\t" + NamingUtil.concatNamespaces(namespaces, classMapping::getName, "\t"));
            }
            cls.getFields().parallelStream().forEach(field -> {
                String desc = MappingUtils.Namespaced.checkTiny(namespace0, cls, field);
                synchronized (lines) {
                    lines.add("FIELD\t" + classMapping.getName(namespace0) + '\t' + desc + '\t' +
                            NamingUtil.concatNamespaces(namespaces, field::getName, "\t"));
                }
            });
            cls.getMethods().parallelStream().forEach(method -> {
                String desc = MappingUtils.Namespaced.checkTiny(namespace0, cls, method);
                synchronized (lines) {
                    lines.add("METHOD\t" + classMapping.getName(namespace0) + '\t' + desc + '\t' +
                            NamingUtil.concatNamespaces(namespaces, method::getName, "\t"));
                }
            });
        });
        return lines;
    }
}