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

import cn.maxpixel.mcdecompiler.api.util.FileUtil;
import cn.maxpixel.mcdecompiler.api.util.JarUtil;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Action extends AutoCloseable {
    String getName();

    default String getDefaultOutputName() {
        return getName() + ".jar";
    }

    /**
     * Executes the action without processing the arguments
     * @param input The input, which is either a zip file or a directory.
     * @param others The others zip file. When files shouldn't be passed as input to
     *               the next action but still matter in the output, put them here.
     *               They will be merged into the intermediate output. This file is not
     *               automatically created and should be created by the action itself
     * @param output The output, which is either a zip file or a directory. Files here will
     *               become the intermediate output and the input for the next action.
     *               It should be created by the action itself
     * @apiNote All paths passed to this method should be absolute and normalized
     * @throws IOException When IO exception occurs
     */
    default void executeRaw(Path input, Path others, Path output) throws Exception {
        FileUtil.deleteIfExists(output);
        try (FileSystem othersFs = JarUtil.createZipFs(FileUtil.makeParentDirs(others), true);
             FileSystem outputFs = JarUtil.createZipFs(FileUtil.makeParentDirs(output), true)) {
            if (Files.isDirectory(input)) {
                execute(input, othersFs.getPath(""), outputFs.getPath(""));
            } else try (FileSystem inputFs = JarUtil.createZipFs(input)) {
                execute(inputFs.getPath(""), othersFs.getPath(""), outputFs.getPath(""));
            }
        }
    }

    /**
     * Executes the action
     * @param input The input directory
     * @param others The others directory. When files shouldn't be passed as input to
     *               the next action but still matter in the output, put them here.
     *               They will be merged into the intermediate output
     * @param output The output directory. Files here will become the intermediate output
     *               and the input for the next action
     * @apiNote All paths passed to this method should be absolute and normalized
     * @throws IOException When IO exception occurs
     */
    void execute(Path input, Path others, Path output) throws Exception;

    /**
     * Clean the things up to save memory if your action holds big objects
     */
    @Override
    default void close() throws Exception {// TODO: Should throwing exceptions be allowed?
    }
}