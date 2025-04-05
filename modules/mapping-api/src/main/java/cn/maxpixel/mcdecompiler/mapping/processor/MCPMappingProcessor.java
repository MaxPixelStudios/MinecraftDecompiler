/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2025 MaxPixelStudios(XiaoPangxie732)
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

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.collection.UniqueMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MCPMappingFormat;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;

import java.util.List;

public enum MCPMappingProcessor implements MappingProcessor.Unique<PairedMapping> {
    INSTANCE;

    @Override
    public MappingFormat<PairedMapping, UniqueMapping<PairedMapping>> getFormat() {
        return MCPMappingFormat.INSTANCE;
    }

    @Override
    public UniqueMapping<PairedMapping> process(List<String> content) {
        return null;
    }

    @Override
    public UniqueMapping<PairedMapping> process(List<String>... contents) {
        return Unique.super.process(contents);
    }
}