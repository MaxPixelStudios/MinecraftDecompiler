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

package cn.maxpixel.mcdecompiler.mapping.format;

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.generator.TsrgV1MappingGenerator;
import cn.maxpixel.mcdecompiler.mapping.processor.TsrgV1MappingProcessor;
import org.jetbrains.annotations.NotNull;

public enum TsrgV1MappingFormat implements MappingFormat.Classified<PairedMapping> {
    INSTANCE;

    @Override
    public @NotNull String getName() {
        return "tsrg-v1";
    }

    @Override
    public @NotNull TsrgV1MappingProcessor getProcessor() {
        return TsrgV1MappingProcessor.INSTANCE;
    }

    @Override
    public @NotNull TsrgV1MappingGenerator getGenerator() {
        return TsrgV1MappingGenerator.INSTANCE;
    }
}