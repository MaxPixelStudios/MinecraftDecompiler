/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2020  MaxPixelStudios
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static java.nio.file.StandardOpenOption.*;

public class JarUtil {
    private static final Logger LOGGER = LogManager.getLogger();
    public static void decompressJar(Path jar, Path target) {
        LOGGER.info("Decompressing jar file \"{}\" into \"{}\"...", jar, target);
        try(JarFile jarFile = new JarFile(jar.toFile())) {
            jarFile.stream().forEach(entry -> {
                LOGGER.trace("Decompressing \"{}\"...", entry);
                try {
                    if(entry.isDirectory()) Files.createDirectories(target.resolve(entry.getName()));
                    else {
                        Path p = target.resolve(entry.getName());
                        Files.createDirectories(p.getParent());
                        try(ReadableByteChannel rc = Channels.newChannel(jarFile.getInputStream(entry));
                            FileChannel fileChannel = FileChannel.open(p, WRITE, CREATE, TRUNCATE_EXISTING)) {
                            fileChannel.transferFrom(rc, 0, Long.MAX_VALUE);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("IO error occurred when decompressing jar file", e);
                }
            });
        } catch (IOException e) {
            LOGGER.error("IO error occurred when opening or closing jar file", e);
        }
    }
    public static void compressJar(String mainClass, Path jar, Path from) {
        LOGGER.info("Compressing jar file \"{}\" from \"{}\"", jar, from);
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);
        try(OutputStream os = Channels.newOutputStream(FileChannel.open(jar, WRITE, CREATE, TRUNCATE_EXISTING));
            JarOutputStream outputStream = new JarOutputStream(os, manifest)) {
            Files.walkFileTree(from, new FileVisitor<Path>() {
                final FileUtil.RelativePathWalkerHelper helper = new FileUtil.RelativePathWalkerHelper();
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    helper.doPreVisitDir(dir);
                    outputStream.putNextEntry(new ZipEntry(helper.getRelativePath() + "/"));
                    outputStream.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    LOGGER.error("Error while zipping file: " + file, exc);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    outputStream.putNextEntry(new ZipEntry(helper.getRelativePath() == null ? file.getFileName().toString() :
                            helper.getRelativePath() + "/" + file.getFileName()));
                    try(FileChannel channel = FileChannel.open(file, READ)) {
                        channel.transferTo(0, Long.MAX_VALUE, Channels.newChannel(outputStream));
                    }
                    outputStream.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    helper.doPostVisitDir(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}