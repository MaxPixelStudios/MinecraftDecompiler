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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class JarUtil {
	private static final Logger LOGGER = LogManager.getLogger();
	public static void decompressJar(String jarPath, File target) {
		LOGGER.info("decompressing");
		try(JarFile jarFile = new JarFile(jarPath)) {
			jarFile.stream().forEach(entry -> {
				if(entry.isDirectory()) new File(target, entry.getName());
				else {
					try {
						File f = new File(target, entry.getName());
						if(!f.exists()) {
							boolean success = f.getParentFile().mkdirs();
							success &= f.createNewFile();
							if(!success) throw new IOException("File create failed");
							try(InputStream is = jarFile.getInputStream(entry);
							    FileOutputStream out = new FileOutputStream(f)) {
								for (int i = is.read(); i != -1; i = is.read()) out.write(i);
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
		File file = new File(from, "META-INF/MANIFEST.MF");
		if(!file.exists()) {
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try(OutputStream manifestOut = new FileOutputStream(file)) {
			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
			manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);
			manifest.write(manifestOut);
			Process pro = Runtime.getRuntime().exec(new String[] {"jar", "cfm0", jar.getAbsolutePath(), "META-INF\\MANIFEST.MF", "."}, null, from);
			ProcessUtil.waitForProcess(pro);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}