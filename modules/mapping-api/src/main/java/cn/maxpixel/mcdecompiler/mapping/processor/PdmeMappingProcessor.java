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

import cn.maxpixel.mcdecompiler.common.util.Utils;
import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Documented;
import cn.maxpixel.mcdecompiler.mapping.component.LocalVariableTable;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.trait.AccessTransformationTrait;
import cn.maxpixel.mcdecompiler.mapping.trait.InheritanceTrait;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;

public enum PdmeMappingProcessor implements MappingProcessor.Classified<PairedMapping> {
    INSTANCE;

    private static final char PARA = '¶';

    @Override
    public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
        return MappingFormats.PDME;
    }

    @Override
    public ClassifiedMapping<PairedMapping> process(ObjectList<String> content) {
        InheritanceTrait inheritanceMap = new InheritanceTrait();
        AccessTransformationTrait at = new AccessTransformationTrait();
        ClassifiedMapping<PairedMapping> mappings = new ClassifiedMapping<>(inheritanceMap, at);
        Object2ObjectOpenHashMap<String, ClassMapping<PairedMapping>> classes = new Object2ObjectOpenHashMap<>(); // k: unmapped name
        Object2ObjectOpenHashMap<String, PairedMapping> methodMap = new Object2ObjectOpenHashMap<>();
        for (String line : content) {
            String[] parts = MappingUtil.split(line, PARA);
            switch (parts[0]) {
                case "Class" -> {
                    String unmapped = parts[1].replace('.', '/');
                    String mapped = parts[2].replace('.', '/');
                    classes.merge(unmapped, new ClassMapping<>(new PairedMapping(unmapped, mapped, new Documented(parts[5]))), (o, n) -> {
                        n.addFields(o.getFields());
                        n.addMethods(o.getMethods());
                        return n;
                    });
                }
                case "Def" -> getMethod(parts[1], parts[2], parts[5], classes, methodMap);
                case "Var" -> {
                    int lastDot = parts[1].lastIndexOf('.');
                    String nameAndDesc = parts[1].substring(lastDot + 1);
                    int colon = nameAndDesc.indexOf(':');
                    PairedMapping field = MappingUtil.Paired.duo(nameAndDesc.substring(0, colon), parts[2],
                            nameAndDesc.substring(colon + 1));
                    field.addComponent(new Documented(parts[5]));
                    ClassMapping<PairedMapping> cm = classes.computeIfAbsent(parts[1].substring(0, lastDot)
                            .replace('.', '/'), MappingUtil.Paired.COMPUTE_DEFAULT_CLASS);
                    cm.addField(field);
                }
                case "Param" -> {
                    PairedMapping local = new PairedMapping(parts[1], parts[2]);
                    local.addComponent(new Documented(parts[5]));
                    PairedMapping method = getMethod(parts[3], null, null, classes, methodMap);
                    LocalVariableTable.Paired lvt = method.getOrCreateComponent(LocalVariableTable.Paired.class,
                            LocalVariableTable.Paired::new);
                    lvt.setLocalVariable(Integer.parseInt(parts[4]), local);
                }
                case "Include", "Incluir" -> inheritanceMap.put(parts[1].replace('.', '/'),
                        Utils.mapArray(MappingUtil.split(parts[2], ','), String[]::new,
                                s -> s.replace('.', '/')));
                case "AccessFlag", "BanderaDeAcceso" -> {
                    if (parts[1].contains(":")) { // field
                    } else if (parts[1].contains("(")) { // method
                    } else { // class
                        at.add(parts[1].replace('.', '/'), parts[2].startsWith("0x") ?
                                Integer.parseInt(parts[2].substring(2), 16) : Integer.parseInt(parts[2]));
                    }
                }
            }
        }
        mappings.classes.addAll(classes.values());
        for (var cm : mappings.classes) parseOuterClass(cm.mapping.unmappedName, classes);
        return mappings;
    }

    private static String parseOuterClass(String unmapped, Object2ObjectOpenHashMap<String, ClassMapping<PairedMapping>> classes) {
        ClassMapping<PairedMapping> cm = classes.get(unmapped);
        String mapped = cm == null ? unmapped : cm.mapping.mappedName;
        int lastDollar = unmapped.lastIndexOf('$');
        if (lastDollar < 0) return mapped;
        String outer = unmapped.substring(0, lastDollar);
        if (cm != null) {
            if (mapped.contains("$")) return mapped;
            String ret = parseOuterClass(outer, classes) + '$' + mapped;
            cm.mapping.mappedName = ret;
            return ret;
        }
        String ret = parseOuterClass(outer, classes) + '$' + unmapped.substring(lastDollar + 1);
        classes.put(unmapped, new ClassMapping<>(new PairedMapping(unmapped, ret)));
        return ret;
    }

    private static PairedMapping getMethod(String original, String mapped, String docs,
                                           Object2ObjectOpenHashMap<String, ClassMapping<PairedMapping>> classes,
                                           Object2ObjectOpenHashMap<String, PairedMapping> methodMap) {
        return methodMap.compute(original, (s, old) -> {
            if (old != null) {
                if (docs != null) old.addComponent(new Documented(docs));
                if (mapped != null) old.mappedName = mapped;
                return old;
            }
            int lastDot = s.lastIndexOf('.');
            String nameAndDesc = s.substring(lastDot + 1);
            int bracket = nameAndDesc.indexOf('(');
            String name = nameAndDesc.substring(0, bracket);
            PairedMapping method = MappingUtil.Paired.duo(name, mapped == null ? name : mapped,
                    nameAndDesc.substring(bracket));
            if (docs != null) method.addComponent(new Documented(docs));
            ClassMapping<PairedMapping> cm = classes.computeIfAbsent(s.substring(0, lastDot)
                    .replace('.', '/'), MappingUtil.Paired.COMPUTE_DEFAULT_CLASS);
            cm.addMethod(method);
            return method;
        });
    }
}