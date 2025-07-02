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

package cn.maxpixel.mcdecompiler.remapper.util;

import cn.maxpixel.mcdecompiler.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.InflaterInputStream;

public class IOUtil {
    private static final Class<?> ZIP_PATH;
    private static final Class<?> ENTRY_INPUT_STREAM;

    static {
        try {
            ZIP_PATH = Class.forName("jdk.nio.zipfs.ZipPath");
            ENTRY_INPUT_STREAM = Class.forName("jdk.nio.zipfs.ZipFileSystem$EntryInputStream");
        } catch (ClassNotFoundException e) {
            throw Utils.wrapInRuntime(e);
        }
    }

    public static byte[] readAllBytes(@NotNull Path file) throws IOException {
        if (ZIP_PATH != file.getClass()) throw new IllegalArgumentException(); // Ensure this is zipfs path
        try (InputStream is = Files.newInputStream(file)) {
            byte[] bytes = new byte[is.available()];
            if (is instanceof InflaterInputStream) {
                if (bytes.length > 65536) for (int len = 0; len != bytes.length; len += is.read(bytes, len, bytes.length - len));
                else is.read(bytes);
                return bytes;
            }
            if (is.getClass() == ENTRY_INPUT_STREAM) {
                is.read(bytes);
                return bytes;
            }
            throw new UnsupportedOperationException();
        }
    }
}