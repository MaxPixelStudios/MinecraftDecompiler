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
	public static void copyDirectory(Path source, Path target, CopyOption... copyOptions) {
		try {
			LOGGER.debug("Coping directory {} to {} ...", source, target);
			try {
				Files.copy(source, target, copyOptions);
			} catch(FileAlreadyExistsException ignored) {}
			Files.walkFileTree(source, new FileVisitor<Path>() {
				String relativePath = null;
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					if(relativePath == null)
						relativePath = dir.getFileName().toString();
					else
						relativePath += "/" + dir.getFileName();
					try {
						Files.copy(dir, target.resolve(relativePath), copyOptions);
					} catch(FileAlreadyExistsException ignored) {}
					return FileVisitResult.CONTINUE;
				}
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					LOGGER.error("Error while coping file: " + file, exc);
					return FileVisitResult.CONTINUE;
				}
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					try {
						Files.copy(file, relativePath == null ?
								target.resolve(file.getFileName()) :
								target.resolve(relativePath).resolve(file.getFileName()), copyOptions);
					} catch(FileAlreadyExistsException ignored) {}
					return FileVisitResult.CONTINUE;
				}
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					int index = relativePath.lastIndexOf('/');
					if(index == -1) relativePath = null;
					else relativePath = relativePath.substring(0, index);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			LOGGER.error("Error while coping directory", e);
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
			LOGGER.debug("deleting directory {} ...", directory);
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					LOGGER.error("Error while deleting file: " + file, exc);
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
			LOGGER.error("Error while deleting directory", e);
		}
	}
}