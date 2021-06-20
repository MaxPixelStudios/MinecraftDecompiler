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

package cn.maxpixel.mcdecompiler.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class IOUtil {
    private static final Class<?> ZIP_FILESYSTEM;

    static {
        try {
            ZIP_FILESYSTEM = Class.forName("jdk.nio.zipfs.ZipFileSystem");
        } catch (ClassNotFoundException e) {
            throw Utils.wrapInRuntime(e);
        }
    }

    public static byte[] readZipFileBytes(Path fileInZip) throws IOException {
        if(ZIP_FILESYSTEM.isInstance(fileInZip.getFileSystem())) // Ensure the filesystem is zipfs
            // Zipfs implementation returns a ByteArrayInputStream, so this is the fastest way
            return Files.newInputStream(fileInZip).readAllBytes();
        else throw new UnsupportedOperationException();
    }
}