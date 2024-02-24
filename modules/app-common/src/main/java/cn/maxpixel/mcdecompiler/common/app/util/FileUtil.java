/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2024 MaxPixelStudios(XiaoPangxie732)
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

package cn.maxpixel.mcdecompiler.common.app.util;

import cn.maxpixel.mcdecompiler.common.util.LambdaUtil;
import cn.maxpixel.mcdecompiler.common.util.Utils;
import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.READ;

@ApiStatus.Internal
public final class FileUtil {
    private static final Logger LOGGER = LogManager.getLogger();

    private FileUtil() {
        throw new AssertionError("No instances");
    }

//    public static void copyDirectory(@NotNull Path source, @NotNull Path target) {
//        if(Files.notExists(source)) {
//            LOGGER.log(Level.FINER, "Source \"{0}\" does not exist, skipping this operation...", source);
//            return;
//        }
//        if(!Files.isDirectory(source)) throw new IllegalArgumentException("Source isn't a directory");
//        Path p = source.toAbsolutePath().normalize();
//        try(Stream<Path> sourceStream = iterateFiles(p)) {
//            final Path dest;
//            if(Files.exists(target)) {
//                if(!Files.isDirectory(target)) throw new IllegalArgumentException("Target exists and it's not a directory");
//                dest = Files.createDirectories(target.resolve(source.getFileName().toString()));
//            } else dest = Files.createDirectories(target);
//            LOGGER.log(Level.FINER, "Coping directory \"{0}\" to \"{1}\"...", new Object[] {source, target});
//            sourceStream.forEach(path -> {
//                Path relative = p.relativize(path);
//                try(InputStream in = Files.newInputStream(path);
//                    OutputStream out = Files.newOutputStream(ensureFileExist(dest.resolve(relative.toString())), TRUNCATE_EXISTING)) {
//                    in.transferTo(out);
//                } catch (IOException e) {
//                    LOGGER.log(Level.WARNING, "Error coping file \"{0}\"", new Object[] {path, e});
//                }
//            });
//        } catch (IOException e) {
//            LOGGER.log(Level.WARNING, "Error coping directory", e);
//        }
//    }

    public static void copyFile(@NotNull Path source, @NotNull Path target) {
        if (Files.notExists(source)) {
            LOGGER.trace("Source \"{}\" does not exist, skipping this operation...", source);
            return;
        }
        if (Files.isDirectory(source)) throw new IllegalArgumentException("Source isn't a file");
        if (Files.exists(target) && Files.isDirectory(target)) target = target.resolve(source.getFileName().toString());
        LOGGER.debug("Coping file {} to {} ...", source, target);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(source, target, REPLACE_EXISTING);
        } catch(FileAlreadyExistsException ignored) {
        } catch (IOException e) {
            LOGGER.error("Error coping file", e);
        }
    }

    public static void deleteIfExists(@NotNull Path path) {
        if (Files.notExists(path)) return;
        try {
            LOGGER.debug("Deleting \"{}\"...", path);
            if (Files.isDirectory(path)) {
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
                    StreamSupport.stream(ds.spliterator(), true)
                            .forEach(FileUtil::deleteIfExists);
                }
            }
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOGGER.error("Failed to delete \"{}\"", path, e);
        }
    }

    public static Path requireExist(@NotNull Path p) {
        if (Files.notExists(p)) throw new IllegalArgumentException("Path \"" + p + "\"does not exist");
        return p;
    }

    public static Path ensureFileExist(@NotNull Path p) {
        if (Files.notExists(p)) {
            try {
                Path parent = p.getParent();
                if (parent != null) Files.createDirectories(parent);
                Files.createFile(p);
            } catch (IOException e) {
                throw Utils.wrapInRuntime(e);
            }
        }
        return p;
    }

    public static Stream<Path> iterateFiles(@NotNull Path path) {
        try {
            DirectoryStream<Path> ds = Files.newDirectoryStream(path);
            return StreamSupport.stream(ds.spliterator(), true)
                    .mapMulti((Path p, Consumer<Path> cons) -> {
                        if (Files.isDirectory(p)) iterateFiles(p).forEach(cons);
                        else cons.accept(p);
                    }).onClose(LambdaUtil.unwrap(ds::close));
        } catch (IOException e) {
            LOGGER.fatal("Error iterating files", e);
            throw Utils.wrapInRuntime(e);
        }
    }

    /**
     * Verify the file with given SHA-1 hash
     * @param path File to verify. Directory is not supported
     * @param hash Expected SHA-1 hash. Use null or blank string to skip the hash check
     * @throws IllegalArgumentException If {@code path} is a directory
     * @return true if the file is valid. Otherwise false
     */
    public static boolean verify(@NotNull Path path, @NotNull String hash) {
        return verify(path, hash, -1);
    }

    /**
     * Verify the file with given SHA-1 hash and size
     * @param path File to verify. Directory is not supported
     * @param hash Expected SHA-1 hash. Use null or blank string to skip the hash check
     * @param size Expected size in bytes. Use negative size to skip the size check
     * @throws IllegalArgumentException If {@code path} is null or a directory
     * @throws IllegalArgumentException If {@code hash} is blank
     * @throws NullPointerException If {@code hash} is null
     * @return true if the file is valid. Otherwise false
     */
    public static boolean verify(@NotNull Path path, @NotNull String hash, long size) {
        if (Files.notExists(path)) return false;
        if (Files.isDirectory(path)) throw new IllegalArgumentException("Verify a directory is not supported");
        if (Objects.requireNonNull(hash, "Why do you want to verify a file with null hash?").isBlank()) {
            throw new IllegalArgumentException("Why do you want to verify a file with no hash?");
        }
        try (FileChannel fc = FileChannel.open(path, READ)) {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            ByteBuffer buf = ByteBuffer.allocateDirect(65536);
            while (fc.read(buf) != -1) {
                md.update(buf.flip());
                buf.clear();
            }
            return (size < 0 || fc.size() == size) && hash.contentEquals(Utils.createHashString(md));
        } catch (IOException e) {
            LOGGER.fatal("Error reading files", e);
            throw Utils.wrapInRuntime(e);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("Hmm... You need a SHA-1 digest implementation");
            return false;
        }
    }
}