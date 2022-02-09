/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2022  MaxPixelStudios
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

import cn.maxpixel.mcdecompiler.mapping.Mapping;
import cn.maxpixel.mcdecompiler.mapping.collection.UniqueMapping;
import cn.maxpixel.mcdecompiler.mapping.type.MappingType;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;

public final class UniqueMappingReader<M extends Mapping> extends AbstractMappingReader<M, UniqueMapping<M>, MappingType.Unique<M>> {
    public UniqueMappingReader(MappingType.Unique<M> type, BufferedReader reader) {
        super(type, reader);
    }

    public UniqueMappingReader(MappingType.Unique<M> type, Reader rd) {
        super(type, rd);
    }

    public UniqueMappingReader(MappingType.Unique<M> type, InputStream is) {
        super(type, is);
    }

    public UniqueMappingReader(MappingType.Unique<M> type, String path) throws FileNotFoundException {
        super(type, path);
    }

    public UniqueMappingReader(MappingType.Unique<M> type, BufferedReader... readers) {
        super(type, readers);
    }

    public UniqueMappingReader(MappingType.Unique<M> type, Reader... rd) {
        super(type, rd);
    }

    public UniqueMappingReader(MappingType.Unique<M> type, InputStream... is) {
        super(type, is);
    }

    public UniqueMappingReader(MappingType.Unique<M> type, String... path) throws FileNotFoundException {
        super(type, path);
    }
}