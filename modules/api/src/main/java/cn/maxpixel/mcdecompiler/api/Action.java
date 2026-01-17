/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2026 MaxPixelStudios(XiaoPangxie732)
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

package cn.maxpixel.mcdecompiler.api;

import cn.maxpixel.mcdecompiler.common.app.util.FileUtil;
import cn.maxpixel.mcdecompiler.common.app.util.JarUtil;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Action {
    default boolean dirInput() {
        return false;
    }

    default boolean dirOutput() {
        return false;
    }

    default void executeRaw(Path input, Path others, Path output) throws IOException {
        try (FileSystem othersFs = JarUtil.createZipFs(FileUtil.makeParentDirs(others), true);
             FileSystem outputFs = JarUtil.createZipFs(FileUtil.makeParentDirs(output), true)) {
            if (Files.isDirectory(input)) {
                execute(input, othersFs.getPath(""), outputFs.getPath(""));
            } else {
                try (FileSystem inputFs = JarUtil.createZipFs(input)) {
                    execute(inputFs.getPath(""), othersFs.getPath(""), outputFs.getPath(""));
                }
            }
        }
    }

    /**
     * Executes the action
     * @param input The input, which behaves as a directory
     * @param others The input, which behaves as a directory
     * @param output The output, which behaves as a directory
     * @throws IOException When IO exception occurs
     */
    void execute(Path input, Path others, Path output) throws IOException;
}