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
import java.io.OutputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class JarUtil {
	private static final Logger LOGGER = LogManager.getLogger();
	public static void decompressJar(String jarPath, File target) {
		LOGGER.info("decompressing");
		try {
			Process pro = Runtime.getRuntime().exec(new String[] {"jar", "xf", new File(jarPath).getAbsolutePath()}, null, target.getAbsoluteFile());
			ProcessUtil.waitForProcess(pro);
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