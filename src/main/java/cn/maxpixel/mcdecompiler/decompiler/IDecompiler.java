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

package cn.maxpixel.mcdecompiler.decompiler;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents a decompiler.<br>
 *
 * To make the program can find your custom decompiler,
 * you need to create a file named cn.maxpixel.mcdecompiler.decompiler.IDecompiler in META-INF/services.
 * Then write the full name(dot-separated) of your custom decompiler class that implements this interface in it.
 */
public interface IDecompiler {
    /**
     * Gets the unique name of your decompiler. The value is needed to identify which decompiler this is.<br>
     * @return the unique name of your decompiler.
     */
    String name();

    /**
     * @return The type of the input.
     */
    SourceType getSourceType();

    /**
     * Decompile.
     * @param source Path for input. The path is absolute and normalized.
     * @param targetDir Path for output. The path is absolute and normalized.
     */
    void decompile(@NotNull Path source, @NotNull Path targetDir) throws IOException;

    default void checkArgs(@NotNull Path source, @NotNull Path target) {
        if (!Files.isDirectory(target)) throw new IllegalArgumentException("target must be directory");
        if (getSourceType() == SourceType.FILE && Files.isDirectory(source)) throw new IllegalArgumentException("source must be file");
        if (getSourceType() == SourceType.DIRECTORY && !Files.isDirectory(source)) throw new IllegalArgumentException("source must be directory!");
    }

    enum SourceType {
        FILE,
        DIRECTORY
    }
}