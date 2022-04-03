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

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class FileUtil {
    private static final Logger LOGGER = Logging.getLogger();

    public static void copyDirectory(@NotNull Path source, @NotNull Path target) {
        if(Files.notExists(source)) {
            LOGGER.log(Level.FINER, "Source \"{0}\" does not exist, skipping this operation...", source);
            return;
        }
        if(!Files.isDirectory(source)) throw new IllegalArgumentException("Source isn't a directory");
        final Path dest;
        if(Files.exists(target)) {
            if(!Files.isDirectory(target)) throw new IllegalArgumentException("Target exists and it's not a directory");
            dest = FileUtil.ensureDirectoryExist(target.resolve(source.getFileName().toString()));
        } else dest = FileUtil.ensureDirectoryExist(target);
        Path p = source.toAbsolutePath().normalize();
        try(Stream<Path> sourceStream = iterateFiles(p)) {
            LOGGER.log(Level.FINER, "Coping directory \"{0}\" to \"{1}\"...", new Object[] {source, target});
            sourceStream.forEach(path -> {
                Path relative = p.relativize(path);
                try(InputStream in = Files.newInputStream(path);
                    OutputStream out = Files.newOutputStream(ensureFileExist(dest.resolve(relative.toString())), TRUNCATE_EXISTING)) {
                    in.transferTo(out);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error coping file \"{0}\"", new Object[] {path, e});
                }
            });
        }
    }

    public static void copyFile(Path source, Path target) {
        if(Files.notExists(source)) {
            LOGGER.log(Level.FINER, "Source \"{0}\" does not exist, skipping this operation...", source);
            return;
        }
        if(Files.isDirectory(source)) throw new IllegalArgumentException("Source isn't a file");
        if(Files.exists(target) && Files.isDirectory(target)) target = target.resolve(source.getFileName().toString());
        LOGGER.log(Level.FINER, "Coping file {0} to {1} ...", new Object[] {source, target});
        try {
            Files.createDirectories(target.getParent());
            Files.copy(source, target, REPLACE_EXISTING);
        } catch(FileAlreadyExistsException ignored) {
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error coping file", e);
        }
    }

    public static void deleteIfExists(Path path) {
        if(Files.notExists(Objects.requireNonNull(path, "path cannot be null"))) {
            LOGGER.log(Level.FINER, "\"{0}\" does not exist, skipping this operation...", path);
            return;
        }
        try {
            LOGGER.log(Level.FINER, "Deleting \"{0}\"...", path);
            if(Files.isDirectory(path)) {
                try(DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
                    StreamSupport.stream(ds.spliterator(), true)
                            .forEach(FileUtil::deleteIfExists);
                }
            }
            Files.delete(path);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to delete \"{0}\"", new Object[] {path, e});
        }
    }

    public static Path requireExist(Path p) {
        if(Files.notExists(Objects.requireNonNull(p))) throw new IllegalArgumentException("Path \"" + p + "\"does not exist");
        return p;
    }

    public static Path ensureFileExist(Path p) {
        if(Files.notExists(p)) {
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

    public static Stream<Path> iterateFiles(Path path) {
        try {
            DirectoryStream<Path> ds = Files.newDirectoryStream(Objects.requireNonNull(path, "path"));
            return StreamSupport.stream(ds.spliterator(), true)
                    .mapMulti((Path p, Consumer<Path> cons) -> {
                        if(Files.isDirectory(p)) iterateFiles(p).forEach(cons);
                        else cons.accept(p);
                    }).onClose(LambdaUtil.unwrap(ds::close));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error iterating files", e);
            throw Utils.wrapInRuntime(e);
        }
    }

    public static boolean verify(Path path, String hash, long size) {
        if(Files.notExists(path)) return false;
        if(Files.isDirectory(path)) throw new IllegalArgumentException("Verify a directory is not supported");
        try(FileChannel fc = FileChannel.open(path, READ)) {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            ByteBuffer buf = ByteBuffer.allocateDirect(65536);
            while(fc.read(buf) != -1) {
                buf.flip();
                md.update(buf);
                buf.clear();
            }
            StringBuilder out = new StringBuilder();
            for(byte b : md.digest()) {
                String hex = Integer.toHexString(Byte.toUnsignedInt(b));
                if(hex.length() < 2) out.append('0');
                out.append(hex);
            }
            return (size < 0 || fc.size() == size) && (hash == null || hash.isBlank() || hash.contentEquals(out));
        } catch(IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading files", e);
            throw Utils.wrapInRuntime(e);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warning("Hmm... You need a SHA-1 digest implementation");
            return false;
        }
    }
}