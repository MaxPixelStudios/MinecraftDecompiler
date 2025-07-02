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

package cn.maxpixel.mcdecompiler.mapping.parchment;

import cn.maxpixel.mcdecompiler.mapping.PairedMapping;
import cn.maxpixel.mcdecompiler.mapping.format.MappingFormat;
import org.jetbrains.annotations.NotNull;

public enum ParchmentMappingFormat implements MappingFormat.Classified<PairedMapping> {
    INSTANCE;
    public static final String NAME = "parchment";
    public static final String KEY_NAME = "name";
    public static final String KEY_JAVADOC = "javadoc";
    public static final String KEY_DESCRIPTOR = "descriptor";
    public static final String KEY_VERSION = "version";
    public static final String KEY_PACKAGES = "packages";
    public static final String KEY_CLASSES = "classes";
    public static final String KEY_FIELDS = "fields";
    public static final String KEY_METHODS = "methods";
    public static final String KEY_INDEX = "index";
    public static final String KEY_PARAMETERS = "parameters";

    @Override
    public @NotNull String getName() {
        return NAME;
    }

    @Override
    public @NotNull ParchmentMappingProcessor getProcessor() {
        return ParchmentMappingProcessor.INSTANCE;
    }

    @Override
    public @NotNull ParchmentMappingGenerator getGenerator() {
        return ParchmentMappingGenerator.INSTANCE;
    }
}