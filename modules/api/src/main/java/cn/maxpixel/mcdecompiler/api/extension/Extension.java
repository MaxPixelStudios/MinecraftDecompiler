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

package cn.maxpixel.mcdecompiler.api.extension;

import cn.maxpixel.mcdecompiler.api.util.DataMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.function.Supplier;

@ApiStatus.OverrideOnly
public interface Extension {
    @NotNull String getName();

    /**
     * Register options, mapping formats, format detectors, decompilers, etc.
     * @param optionRegistrar option registrar
     * @see cn.maxpixel.mcdecompiler.decompiler.Decompilers#registerDecompiler(cn.maxpixel.mcdecompiler.decompiler.IDecompiler)
     * @see cn.maxpixel.mcdecompiler.mapping.format.MappingFormats#registerMappingFormat(cn.maxpixel.mcdecompiler.mapping.format.MappingFormat)
     * @see cn.maxpixel.mcdecompiler.mapping.detector.FormatDetector#registerDetectionUnit(cn.maxpixel.mcdecompiler.mapping.detector.DetectionUnit)
     * @see cn.maxpixel.mcdecompiler.remapper.processing.ClassProcessor#addProcess(cn.maxpixel.mcdecompiler.remapper.processing.Process.Run, Supplier)
     */
    default void onRegistering(OptionRegistry.Registrar optionRegistrar) {
    }

    default void onReceivingOptions(OptionRegistry.ValueGetter getter) {
    }

    default void onPreprocess(FileSystem fs, Path tempDir, DataMap dataMap) throws IOException {
    }
}