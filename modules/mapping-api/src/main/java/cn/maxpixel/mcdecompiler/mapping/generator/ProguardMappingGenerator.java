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
import cn.maxpixel.mcdecompiler.mapping.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.ClassifiedMapping;
import cn.maxpixel.mcdecompiler.mapping.component.Descriptor;
import cn.maxpixel.mcdecompiler.mapping.component.LineNumber;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtil;
import cn.maxpixel.mcdecompiler.mapping.util.NamingUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public enum ProguardMappingGenerator implements MappingGenerator.Classified<PairedMapping> {
    INSTANCE;

    @Override
    public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
        return MappingFormats.PROGUARD;
    }

    @Override
    public ObjectList<String> generate(ClassifiedMapping<PairedMapping> mappings, ClassifiedMappingRemapper remapper) {
        ObjectArrayList<String> lines = new ObjectArrayList<>();
        if (mappings.classes.isEmpty()) return lines;
        for (ClassMapping<PairedMapping> cls : mappings.classes) {
            PairedMapping mapping = cls.mapping;
            lines.add(NamingUtil.asJavaName(mapping.mappedName) + " -> " +
                    NamingUtil.asJavaName(mapping.unmappedName) + ':');
            for (PairedMapping field : cls.getFields()) {
                MappingUtil.checkOwner(field.getOwned(), cls);
                String mappedDesc;
                if (field.hasComponent(Descriptor.Mapped.class)) {
                    mappedDesc = field.getComponent(Descriptor.Mapped.class).descriptor;
                } else if (remapper != null && field.hasComponent(Descriptor.Unmapped.class)) {
                    mappedDesc = remapper.mapDesc(field.getComponent(Descriptor.Unmapped.class).descriptor);
                } else throw new UnsupportedOperationException();
                lines.add("    " + NamingUtil.descriptor2Java(mappedDesc) + ' ' + field.mappedName +
                        " -> " + field.unmappedName);
            }
            for (PairedMapping method : cls.getMethods()) {
                MappingUtil.checkOwner(method.getOwned(), cls);
                String mappedDesc;
                if (method.hasComponent(Descriptor.Mapped.class)) {
                    mappedDesc = method.getComponent(Descriptor.Mapped.class).descriptor;
                } else if (remapper != null && method.hasComponent(Descriptor.Unmapped.class)) {
                    mappedDesc = remapper.mapMethodDesc(method.getComponent(Descriptor.Unmapped.class).descriptor);
                } else throw new UnsupportedOperationException();
                StringBuilder args = new StringBuilder(mappedDesc.length());
                int end = mappedDesc.lastIndexOf(')'), last = 1;
                for (int i = 1; i < end; i++) {
                    char c = mappedDesc.charAt(i);
                    if (c != '[') {
                        if (c == 'L') i = mappedDesc.indexOf(';', i);
                        args.append(NamingUtil.descriptor2Java(mappedDesc.substring(last, last = i + 1))).append(',');
                    }
                }
                if (end > 1) args.deleteCharAt(args.length() - 1);
                String ret = NamingUtil.descriptor2Java(mappedDesc.substring(end + 1));
                if (method.hasComponent(LineNumber.class)) {
                    LineNumber lineNumber = method.getComponent(LineNumber.class);
                    lines.add("    " + lineNumber.startLineNumber + ':' + lineNumber.endLineNumber + ':' +
                            ret + ' ' + method.mappedName + '(' + args + ") -> " + method.unmappedName);
                } else {
                    lines.add("    " + ret + ' ' + method.mappedName + '(' + args + ") -> " + method.unmappedName);
                }
            }
        }
        return lines;
    }
}