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

package cn.maxpixel.mcdecompiler.decompiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface IDecompiler {
    /**
     * @return The type of the input.
     */
    SourceType getSourceType();

    /**
     * Decompile.
     * @param source Path for input. The path is absolute and normalized.
     * @param targetDir Path for output. The path is absolute and normalized.
     */
    void decompile(Path source, Path targetDir) throws IOException;

    default void checkArgs(Path source, Path target) {
        if(!Files.isDirectory(target)) throw new IllegalArgumentException("target must be directory");
        if(getSourceType() == SourceType.FILE && Files.isDirectory(source)) throw new IllegalArgumentException("source must be file");
        if(getSourceType() == SourceType.DIRECTORY && !Files.isDirectory(source)) throw new IllegalArgumentException("source must be directory!");
    }

    enum SourceType {
        FILE,
        DIRECTORY
    }
}