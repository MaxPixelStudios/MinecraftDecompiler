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
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtil {
    private static final Logger LOGGER = LogManager.getLogger();
    public static void copyDirectory(Path source, Path target, CopyOption... copyOptions) throws IOException {
        if(Files.notExists(source)) throw new IOException("Source not exist");
        if(!Files.isDirectory(source)) throw new IOException("Source isn't a directory");
        if(Files.exists(target) && !Files.isDirectory(target)) throw new IOException("Target isn't a directory");
        try {
            LOGGER.debug("Coping directory \"{}\" to \"{}\"...", source, target);
            Files.copy(source, target, copyOptions);
            Files.walkFileTree(source, new FileVisitor<Path>() {
                private final RelativePathWalkerHelper helper = new RelativePathWalkerHelper();
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    helper.doPreVisitDir(dir);
                    try {
                        Files.copy(dir, target.resolve(helper.getRelativePath()), copyOptions);
                    } catch(FileAlreadyExistsException ignored) {}
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    LOGGER.error("Error when coping file \"{}\"", file, exc);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        Files.copy(file, helper.getRelativePath() == null ?
                                target.resolve(file.getFileName()) :
                                target.resolve(helper.getRelativePath()).resolve(file.getFileName()), copyOptions);
                    } catch(FileAlreadyExistsException ignored) {}
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    helper.doPostVisitDir(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch(FileAlreadyExistsException ignored) {
        } catch (IOException e) {
            LOGGER.error("Error when coping directory", e);
        }
    }
    public static void copyFile(Path source, Path target, CopyOption... copyOptions) {
        LOGGER.debug("Coping file {} to {} ...", source, target);
        try {
            Files.createDirectories(target.getParent());
            try {
                Files.copy(source, target, copyOptions);
            } catch(FileAlreadyExistsException ignored) {}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void deleteDirectory(Path directory) {
        try {
            LOGGER.debug("Deleting directory \"{}\"...", directory);
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    LOGGER.error("Error when deleting file \"{}\"", file, exc);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOGGER.error("Error when deleting directory", e);
        }
    }
    public static class RelativePathWalkerHelper {
        private String relativePath = null;
        public String getRelativePath() {
            return relativePath;
        }
        public void doPreVisitDir(Path dir) {
            if(relativePath == null) relativePath = dir.getFileName().toString();
            else relativePath += "/" + dir.getFileName();
        }
        public void doPostVisitDir(Path dir) {
            int index = relativePath.lastIndexOf('/');
            if(index == -1) relativePath = null;
            else relativePath = relativePath.substring(0, index);
        }
    }
}