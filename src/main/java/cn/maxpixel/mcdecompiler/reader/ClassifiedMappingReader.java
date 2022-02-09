/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2021  MaxPixelStudios
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.reader;

import cn.maxpixel.mcdecompiler.mapping1.Mapping;
import cn.maxpixel.mcdecompiler.mapping1.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping1.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping1.collection.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping1.type.MappingType;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;

public final class ClassifiedMappingReader<M extends Mapping> extends AbstractMappingReader<M, ObjectList<ClassMapping<M>>, MappingType.Classified<M>> {
    public ClassifiedMappingReader(MappingType.Classified<M> type, BufferedReader reader) {
        super(type, reader);
    }

    public ClassifiedMappingReader(MappingType.Classified<M> type, Reader rd) {
        super(type, rd);
    }

    public ClassifiedMappingReader(MappingType.Classified<M> type, InputStream is) {
        super(type, is);
    }

    public ClassifiedMappingReader(MappingType.Classified<M> type, String path) throws FileNotFoundException {
        super(type, path);
    }

    public ClassifiedMappingReader(MappingType.Classified<M> type, BufferedReader... readers) {
        super(type, readers);
    }

    public ClassifiedMappingReader(MappingType.Classified<M> type, Reader... rd) {
        super(type, rd);
    }

    public ClassifiedMappingReader(MappingType.Classified<M> type, InputStream... is) {
        super(type, is);
    }

    public ClassifiedMappingReader(MappingType.Classified<M> type, String... path) throws FileNotFoundException {
        super(type, path);
    }

    public static ClassifiedMappingReader<PairedMapping> reverse(ClassifiedMappingReader<PairedMapping> reader) {
        ClassMapping.reverse(reader.mappings, reader.packages);
        return reader;
    }

    public static ClassifiedMappingReader<NamespacedMapping> swap(ClassifiedMappingReader<NamespacedMapping> reader, String targetNamespace) {
        return swap(reader, NamingUtil.findSourceNamespace(reader.mappings), targetNamespace);
    }

    public static ClassifiedMappingReader<NamespacedMapping> swap(ClassifiedMappingReader<NamespacedMapping> reader, String sourceNamespace, String targetNamespace) {
        ClassMapping.swap(reader.mappings, reader.packages, sourceNamespace, targetNamespace);
        return reader;
    }
}