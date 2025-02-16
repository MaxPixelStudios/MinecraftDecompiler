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

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.component.Documented;
import cn.maxpixel.mcdecompiler.mapping.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.trait.AccessTransformationTrait;
import cn.maxpixel.mcdecompiler.mapping.trait.InheritanceTrait;
import cn.maxpixel.mcdecompiler.mapping.util.NamingUtil;
import cn.maxpixel.mcdecompiler.mapping.util.Utils;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.Locale;
import java.util.Objects;

public enum PdmeMappingGenerator implements MappingGenerator.Classified<PairedMapping> {
    INSTANCE;

    private static final String PARA = "¶";
    private static final String NIL = "nil";

    @Override
    public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
        return MappingFormats.PDME;
    }

    @Override
    public ObjectList<String> generate(ClassifiedMapping<PairedMapping> mappings, ClassifiedMappingRemapper remapper) {
        ObjectArrayList<String> lines = new ObjectArrayList<>();
        lines.add("tipo¶original¶nuevo¶def¶pos¶desc");
        if (mappings.classes.isEmpty()) return lines;
        mappings.classes.parallelStream().forEach(classMapping -> {
            PairedMapping cm = classMapping.mapping;
            String unmapped = NamingUtil.asJavaName(cm.getUnmappedName());
            String mapped = NamingUtil.asJavaName(cm.getMappedName());
            String clsDoc = getDoc(cm);
            synchronized (lines) {
                lines.add(String.join(PARA, "Class", unmapped, mapped, NIL, NIL, clsDoc));
            }
            classMapping.getFields().parallelStream().forEach(field -> {
                String desc = field.getComponent(Descriptor.Unmapped.class).descriptor;
                String unmappedName = unmapped + '.' + field.getUnmappedName() + ':' + desc;
                String doc = getDoc(field);
                synchronized (lines) {
                    lines.add(String.join(PARA, "Var", unmappedName, field.mappedName, NIL, NIL, doc));
                }
            });
            classMapping.getMethods().parallelStream().forEach(method -> {
                String desc = method.getComponent(Descriptor.Unmapped.class).descriptor;
                String unmappedName = unmapped + '.' + method.getUnmappedName() + desc;
                String doc = getDoc(method);
                synchronized (lines) {
                    lines.add(String.join(PARA, "Def", unmappedName, method.mappedName, NIL, NIL, doc));
                }
                var lvt = method.getComponent(LocalVariableTable.Paired.class);
                if (lvt != null) {
                    IntIterator it = lvt.getLocalVariableIndexes().iterator();
                    while (it.hasNext()) {
                        int index = it.nextInt();
                        PairedMapping lv = lvt.getLocalVariable(index);
                        String line = String.join(
                                PARA,
                                "Param",
                                nilWhenBlank(lv.unmappedName),
                                nilWhenBlank(lv.mappedName),
                                unmappedName,
                                String.valueOf(index),
                                getDoc(lv)
                        );
                        synchronized (lines) {
                            lines.add(line);
                        }
                    }
                }
            });
        });
        if (mappings.hasTrait(InheritanceTrait.class)) {
            mappings.getTrait(InheritanceTrait.class).map.forEach((k, v) -> {
                if (!v.isEmpty()) lines.add(String.join(PARA, "Include", NamingUtil.asJavaName(k),
                        NamingUtil.asJavaName(String.join(",", v)), NIL, NIL, ""));
            });
        }
        if (mappings.hasTrait(AccessTransformationTrait.class)) {
            var at = mappings.getTrait(AccessTransformationTrait.class);
            at.classMap.object2IntEntrySet().fastForEach(e -> lines.add(String.join(PARA, "AccessFlag",
                    NamingUtil.asJavaName(e.getKey()), formatHex(e.getIntValue()), NIL, NIL, "")));
            at.fieldMap.object2IntEntrySet().fastForEach(e -> lines.add(String.join(PARA, "AccessFlag",
                    NamingUtil.asJavaName(e.getKey().owner()) + '.' + e.getKey().name() + ':' +
                            Objects.requireNonNull(e.getKey().descriptor()), formatHex(e.getIntValue()), NIL, NIL, "")));
            at.methodMap.object2IntEntrySet().fastForEach(e -> lines.add(String.join(PARA, "AccessFlag",
                    NamingUtil.asJavaName(e.getKey().owner()) + '.' + e.getKey().name() + e.getKey().descriptor(),
                    formatHex(e.getIntValue()), NIL, NIL, "")));
        }
        return lines;
    }

    private static String nilWhenBlank(String s) {
        return Utils.isStringNotBlank(s) ? s : NIL;
    }

    private static String getDoc(PairedMapping m) {
        var doc = m.getComponent(Documented.class);
        return doc != null ? doc.getContentString() : "";
    }

    private static String formatHex(int value) {
        String s = Integer.toHexString(value).toUpperCase(Locale.ENGLISH);
        return "0x" + "0".repeat(4 - s.length()) + s;
    }
}