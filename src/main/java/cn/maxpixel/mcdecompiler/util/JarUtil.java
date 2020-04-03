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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class JarUtil {
	private static final Logger LOGGER = LogManager.getLogger();
	public static void decompressJar(String jarPath, File target) {
		LOGGER.info("decompressing");
		try(JarFile jarFile = new JarFile(jarPath)) {
			jarFile.stream().forEach(entry -> {
				if(entry.isDirectory()) new File(target, entry.getName()).mkdirs();
				else {
					try {
						File f = new File(target, entry.getName());
						if(!f.exists()) {
							f.getParentFile().mkdirs();
							try(ReadableByteChannel readableByteChannel = Channels.newChannel(jarFile.getInputStream(entry));
								FileChannel fileChannel = FileChannel.open(f.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
								fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
							}
						}
					} catch (IOException ex) {
						LOGGER.error("A exception occurred while decompressing jar file", ex);
					}
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void compressJar(String mainClass, File jar, File from) {
		LOGGER.info("compressing");
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);
		try(FileOutputStream jarOut = new FileOutputStream(jar);
		    JarOutputStream outputStream = new JarOutputStream(jarOut, manifest)) {
			for(File child : Objects.requireNonNull(from.listFiles())) {
				if(child.isDirectory()) {
					Files.walkFileTree(child.toPath(), new FileVisitor<Path>() {
						String relativePath = null;
						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
							if(relativePath == null)
								relativePath = dir.getFileName().toString();
							else
								relativePath += "/" + dir.getFileName();
							outputStream.putNextEntry(new ZipEntry(relativePath + "/"));
							outputStream.closeEntry();
							return FileVisitResult.CONTINUE;
						}
						@Override
						public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
							LOGGER.error("Error while zipping file: " + file, exc);
							return FileVisitResult.CONTINUE;
						}
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							outputStream.putNextEntry(new ZipEntry(relativePath == null ? file.getFileName().toString() : relativePath + "/" + file.getFileName()));
							try(FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
								if(channel.size() <= 256L * 1024L * 1024L) { // 256MB
									ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
									channel.read(buffer);
									outputStream.write(buffer.array());
								} else {
									ByteBuffer buffer = ByteBuffer.allocate(256 * 1024 * 1024); //allocate 256MB buffer
									int len;
									while((len = channel.read(buffer)) > 0) {
										outputStream.write(buffer.array(), 0, len);
										buffer.clear();
									}
								}
							}
							outputStream.closeEntry();
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
				}
				if(child.isFile()) {
					outputStream.putNextEntry(new ZipEntry(child.getName()));
					try(FileChannel channel = FileChannel.open(child.toPath(), StandardOpenOption.READ)) {
						if(channel.size() <= 256L * 1024L * 1024L) { // 256MB
							ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
							channel.read(buffer);
							outputStream.write(buffer.array());
						} else {
							ByteBuffer buffer = ByteBuffer.allocate(256 * 1024 * 1024); //allocate 256MB buffer
							int len;
							while((len = channel.read(buffer)) > 0) {
								outputStream.write(buffer.array(), 0, len);
								buffer.clear();
							}
						}
					}
					outputStream.closeEntry();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}