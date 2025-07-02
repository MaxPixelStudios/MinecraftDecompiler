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
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormats;
import cn.maxpixel.mcdecompiler.mapping.remapper.ClassifiedMappingRemapper;
import cn.maxpixel.mcdecompiler.mapping.util.MappingUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public enum SrgMappingGenerator implements MappingGenerator.Classified<PairedMapping> {
    INSTANCE;

    @Override
    public MappingFormat<PairedMapping, ClassifiedMapping<PairedMapping>> getFormat() {
        return MappingFormats.SRG;
    }

    @Override
    public ObjectList<String> generate(ClassifiedMapping<PairedMapping> mappings, ClassifiedMappingRemapper remapper) {
        ObjectArrayList<String> lines = new ObjectArrayList<>();
        if (mappings.classes.isEmpty() && mappings.packages.isEmpty()) return lines;
        mappings.classes.parallelStream().forEach(cls -> {
            PairedMapping classMapping = cls.mapping;
            synchronized (lines) {
                lines.add("CL: " + classMapping.unmappedName + " " + classMapping.mappedName);
            }
            cls.getFields().parallelStream().forEach(field -> {
                MappingUtils.checkOwner(field.getOwned(), cls);
                synchronized (lines) {
                    lines.add("FD: " + classMapping.unmappedName + '/' + field.unmappedName + ' ' +
                            classMapping.mappedName + '/' + field.mappedName);
                }
            });
            cls.getMethods().parallelStream().forEach(method -> {
                MappingUtils.checkOwner(method.getOwned(), cls);
                String unmappedDesc, mappedDesc;
                if (method.hasComponent(Descriptor.Unmapped.class)) {
                    unmappedDesc = method.getComponent(Descriptor.Unmapped.class).descriptor;
                    if (method.hasComponent(Descriptor.Mapped.class))
                        mappedDesc = method.getComponent(Descriptor.Mapped.class).descriptor;
                    else if (remapper != null) mappedDesc = remapper.mapMethodDesc(unmappedDesc);
                    else throw new UnsupportedOperationException();
                } else if (method.hasComponent(Descriptor.Mapped.class)) {
                    mappedDesc = method.getComponent(Descriptor.Mapped.class).descriptor;
                    if (remapper != null) unmappedDesc = remapper.unmapMethodDesc(mappedDesc);
                    else throw new UnsupportedOperationException();
                } else throw new UnsupportedOperationException();
                synchronized (lines) {
                    lines.add("MD: " + classMapping.unmappedName + '/' + method.unmappedName + ' ' + unmappedDesc + ' ' +
                            classMapping.mappedName + '/' + method.mappedName + ' ' + mappedDesc);
                }
            });
        });
        mappings.packages.parallelStream().forEach(pkg -> {
            synchronized(lines) {
                lines.add("PK: " + pkg.unmappedName + ' ' + pkg.mappedName);
            }
        });
        return lines;
    }
}