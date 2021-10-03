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

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.zip.InflaterInputStream;

public class IOUtil {
    private static final Class<?> ZIP_FILESYSTEM = LambdaUtil.trySupply(() -> Class.forName("jdk.nio.zipfs.ZipFileSystem"));
    private static final Class<?> ENTRY_INPUT_STREAM = LambdaUtil.trySupply(() -> Class.forName("jdk.nio.zipfs.ZipFileSystem$EntryInputStream"));

    public static byte[] readAllBytes(Path file) throws IOException {
        if(ZIP_FILESYSTEM == file.getFileSystem().getClass()) { // Ensure the filesystem is zipfs
            try(InputStream is = Files.newInputStream(file)) {
                if(is instanceof InflaterInputStream || ENTRY_INPUT_STREAM.isInstance(is)) {
                    byte[] bytes = new byte[is.available()];
                    if(bytes.length > 65536)
                        for(int len = 0; len != bytes.length; len += is.read(bytes, len, bytes.length - len));
                    else is.read(bytes);
                    return bytes;
                }
                if(is instanceof ByteArrayInputStream) return is.readAllBytes();
            }
            throw new UnsupportedOperationException();
        } else try(FileChannel ch = FileChannel.open(file, StandardOpenOption.READ)) {
            MappedByteBuffer mbb = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
            byte[] bytes = new byte[mbb.remaining()];
            mbb.get(bytes);
            return bytes;
        }
    }

    public static BufferedReader asBufferedReader(Reader reader) {
        return asBufferedReader(reader, "reader");
    }

    public static BufferedReader asBufferedReader(Reader reader, String readerName) {
        return Objects.requireNonNull(reader, () -> readerName + " cannot be null") instanceof BufferedReader br ?
                br : new BufferedReader(reader);
    }
}