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

import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.*;

public class JarUtil {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final FileSystemProvider JAR_FSP;
    public static FileSystemProvider getJarFileSystemProvider() {
        return JAR_FSP;
    }
    public static FileSystem createZipFs(Path zipPath) throws IOException {
        return JAR_FSP.newFileSystem(zipPath, Object2ObjectMaps.singleton("create", "true"));
    }
    public static void unzipJar(Path jar, Path dest) {
        LOGGER.info("Unzipping jar file \"{}\" into \"{}\"...", jar, dest);
        FileUtil.requireExist(jar);
        FileUtil.ensureDirectoryExist(dest);
        // Don't use zipfs to unzip jar file because its performance is lower than parallel JarFile
        try(JarFile jarFile = new JarFile(jar.toFile())) {
            jarFile.stream().parallel().forEach(entry -> {
                LOGGER.trace("Unzipping \"{}\"...", entry);
                try {
                    if(entry.isDirectory()) {
                        Files.createDirectories(dest.resolve(entry.getName()));
                    } else {
                        Path p = dest.resolve(entry.getName());
                        Files.createDirectories(p.getParent());
                        try(ReadableByteChannel rc = Channels.newChannel(jarFile.getInputStream(entry));
                            FileChannel fileChannel = FileChannel.open(p, WRITE, CREATE, TRUNCATE_EXISTING)) {
                            fileChannel.transferFrom(rc, 0L, Long.MAX_VALUE);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("IO error occurred when unzipping jar file", e);
                }
            });
        } catch (IOException e) {
            LOGGER.error("IO error occurred when unzipping jar file", e);
        }
    }
    public static void zipJar(String mainClass, Path jar, Path source) {
        LOGGER.info("Zipping jar file \"{}\" from \"{}\"", jar, source);
        FileUtil.requireExist(source);
        FileUtil.delete(jar);
        try(FileSystem jarFs = createZipFs(jar);
            Stream<Path> s = Files.walk(source).skip(1L).parallel()) {
            s.forEach(path -> {
                try {
                    Path relativePath = source.relativize(path);
                    String[] paths = new String[relativePath.getNameCount()];
                    for(int i = 0; i < relativePath.getNameCount(); i++) paths[i] = relativePath.getName(i).toString();
                    Path p = jarFs.getPath("/", paths);
                    if(Files.isDirectory(path)) FileUtil.ensureDirectoryExist(p);
                    else {
                        FileUtil.ensureFileExist(p);
                        Set<OpenOption> options = new HashSet<>();
                        Collections.addAll(options, WRITE, CREATE, TRUNCATE_EXISTING);
                        try(WritableByteChannel to = JAR_FSP.newByteChannel(p, options);
                            FileChannel from = FileChannel.open(path, READ)) {
                            from.transferTo(0L, Long.MAX_VALUE, to);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("IO error occurred when zipping jar file", e);
                }
            });
            try(OutputStream out = JAR_FSP.newOutputStream(jarFs.getPath(JarFile.MANIFEST_NAME), CREATE, TRUNCATE_EXISTING)) {
                Manifest manifest = new Manifest();
                manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
                manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);
                manifest.write(out);
            }
        } catch (IOException e) {
            LOGGER.error("IO error occurred when zipping jar file", e);
        }
    }
    static {
        FileSystemProvider provider = null;
        for (FileSystemProvider p: FileSystemProvider.installedProviders()) {
            if(p.getScheme().equalsIgnoreCase("jar")) {
                provider = p;
            }
        }
        if(provider == null) throw new IllegalStateException("jar/zip file system provider does not exist");
        JAR_FSP = provider;
    }
}