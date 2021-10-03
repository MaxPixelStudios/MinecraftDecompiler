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
import cn.maxpixel.mcdecompiler.mapping1.UniqueMapping;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;

public class UniqueMappingReader<M extends Mapping> extends AbstractMappingReader<M, UniqueMapping<M>, MappingProcessor.Unique<M>> {
    public UniqueMappingReader(MappingProcessor.Unique<M> processor, BufferedReader reader) {
        super(processor, reader);
    }

    public UniqueMappingReader(MappingProcessor.Unique<M> processor, Reader rd) {
        super(processor, rd);
    }

    public UniqueMappingReader(MappingProcessor.Unique<M> processor, InputStream is) {
        super(processor, is);
    }

    public UniqueMappingReader(MappingProcessor.Unique<M> processor, String path) throws FileNotFoundException {
        super(processor, path);
    }

    public UniqueMappingReader(MappingProcessor.Unique<M> processor, BufferedReader... readers) {
        super(processor, readers);
    }

    public UniqueMappingReader(MappingProcessor.Unique<M> processor, Reader... rd) {
        super(processor, rd);
    }

    public UniqueMappingReader(MappingProcessor.Unique<M> processor, InputStream... is) {
        super(processor, is);
    }

    public UniqueMappingReader(MappingProcessor.Unique<M> processor, String... path) throws FileNotFoundException {
        super(processor, path);
    }
}