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

import cn.maxpixel.mcdecompiler.mapping.NamespacedMapping;
import cn.maxpixel.mcdecompiler.mapping.generator.TinyV2MappingGenerator;
import cn.maxpixel.mcdecompiler.mapping.processor.TinyV2MappingProcessor;
import org.jetbrains.annotations.NotNull;

public enum TinyV2MappingFormat implements MappingFormat.Classified<NamespacedMapping> {
    INSTANCE;

    @Override
    public @NotNull String getName() {
        return "tiny-v2";
    }

    @Override
    public @NotNull TinyV2MappingProcessor getProcessor() {
        return TinyV2MappingProcessor.INSTANCE;
    }

    @Override
    public @NotNull TinyV2MappingGenerator getGenerator() {
        return TinyV2MappingGenerator.INSTANCE;
    }
}