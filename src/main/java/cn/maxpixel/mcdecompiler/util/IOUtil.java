/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2022  MaxPixelStudios
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.InflaterInputStream;

public class IOUtil {
    private static final Class<?> ZIP_FILESYSTEM;
    private static final Class<?> ENTRY_INPUT_STREAM;

    static {
        try {
            ZIP_FILESYSTEM = Class.forName("jdk.nio.zipfs.ZipFileSystem");
            ENTRY_INPUT_STREAM = Class.forName("jdk.nio.zipfs.ZipFileSystem$EntryInputStream");
        } catch (ClassNotFoundException e) {
            throw Utils.wrapInRuntime(e);
        }
    }

    public static byte[] readAllBytes(Path file) throws IOException {
        if(ZIP_FILESYSTEM != file.getFileSystem().getClass()) throw new IllegalArgumentException(); // Ensure the filesystem is zipfs
        InputStream is = Files.newInputStream(file); // Caller will close this stream
        byte[] bytes = new byte[is.available()];
        if(is instanceof InflaterInputStream) {
            if(bytes.length > 65536) for(int len = 0; len != bytes.length; len += is.read(bytes, len, bytes.length - len));
            else is.read(bytes);
            return bytes;
        }
        if(is.getClass() == ENTRY_INPUT_STREAM) {
            is.read(bytes);
            return bytes;
        }
        throw new UnsupportedOperationException();
    }

    public static BufferedReader asBufferedReader(Reader reader) {
        return asBufferedReader(reader, "reader");
    }

    public static BufferedReader asBufferedReader(Reader reader, String readerName) {
        return Objects.requireNonNull(reader, () -> readerName + " cannot be null") instanceof BufferedReader br ?
                br : new BufferedReader(reader);
    }
}