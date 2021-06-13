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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class FileUtil {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void copy(Path source, Path target) {
        if(Files.isDirectory(source)) copyDirectory(source, target);
        else copyFile(source, target);
    }

    public static void copyDirectory(Path source, Path target) {
        if(Files.notExists(source)) {
            LOGGER.debug("Source \"{}\" does not exist, skipping this operation...", source);
            return;
        }
        if(!Files.isDirectory(source)) throw new IllegalArgumentException("Source isn't a directory");
        if(Files.exists(target) && !Files.isDirectory(target)) throw new IllegalArgumentException("Target isn't a directory");
        try(Stream<Path> sourceStream = Files.walk(source).parallel().filter(Files::isRegularFile)) {
            LOGGER.debug("Coping directory \"{}\" to \"{}\"...", source, target);
            FileUtil.ensureDirectoryExist(target);
            sourceStream.forEach(path -> {
                Path p = target.resolve(path.toString().substring(1));
                FileUtil.ensureDirectoryExist(p.getParent());
                try(InputStream in = Files.newInputStream(path);
                    OutputStream out = Files.newOutputStream(p, CREATE, TRUNCATE_EXISTING)) {
                    byte[] buf = new byte[8192];
                    for(int len = in.read(buf); len != -1; len = in.read(buf)) out.write(buf, 0, len);
                } catch (IOException e) {
                    LOGGER.error("Error when coping file \"{}\"", path, e);
                }
            });
        } catch (IOException e) {
            LOGGER.error("Error when coping directory", e);
        }
    }

    public static void copyFile(Path source, Path target) {
        if(Files.notExists(source)) {
            LOGGER.debug("Source \"{}\" does not exist, skipping this operation...", source);
            return;
        }
        if(Files.isDirectory(source)) throw new IllegalArgumentException("Source isn't a file");
        if(Files.exists(target) && Files.isDirectory(target)) target = target.resolve(source.getFileName().toString());
        LOGGER.debug("Coping file {} to {} ...", source, target);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(source, target, REPLACE_EXISTING);
        } catch(FileAlreadyExistsException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteIfExists(Path path) {
        if(Files.notExists(path)) {
            LOGGER.debug("\"{}\" does not exist, skipping this operation...", path);
            return;
        }
        if(Files.isDirectory(path)) deleteDirectoryIfExists(path);
        else {
            try {
                Files.delete(path);
            } catch (IOException e) {
                LOGGER.error("Error when deleting file \"{}\"", path, e);
            }
        }
    }

    public static void deleteDirectoryIfExists(Path directory) {
        if(Files.notExists(directory)) {
            LOGGER.debug("\"{}\" does not exist, skipping this operation...", directory);
            return;
        }
        if(!Files.isDirectory(directory)) throw new IllegalArgumentException("Not a directory!");
        try {
            LOGGER.debug("Deleting directory \"{}\"...", directory);
            Files.walkFileTree(directory, new FileVisitor<>() {
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    LOGGER.error("Error when deleting file \"{}\" in directory \"{}\"", file, directory, exc);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOGGER.error("Error when deleting directory \"{}\"", directory, e);
        }
    }

    public static void requireExist(Path p) {
        if(Files.notExists(p)) throw new IllegalArgumentException("Path \"" + p + "\"does not exist");
    }

    public static Path ensureFileExist(Path p) {
        if(p != null && Files.notExists(p)) {
            try {
                ensureDirectoryExist(p.getParent());
                Files.createFile(p);
            } catch (IOException e) {
                throw Utils.wrapInRuntime(e);
            }
        }
        return p;
    }

    public static Path ensureDirectoryExist(Path p) {
        if(p != null && Files.notExists(p)) {
            try {
                Files.createDirectories(p);
            } catch (IOException e) {
                throw Utils.wrapInRuntime(e);
            }
        }
        return p;
    }
}