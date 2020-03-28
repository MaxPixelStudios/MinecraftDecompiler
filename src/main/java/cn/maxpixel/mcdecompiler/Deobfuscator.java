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

package cn.maxpixel.mcdecompiler;

import cn.maxpixel.mcdecompiler.deobfuscator.AbstractDeobfuscator;
import cn.maxpixel.mcdecompiler.deobfuscator.ProguardDeobfuscator;
import cn.maxpixel.mcdecompiler.util.FileUtil;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Deobfuscator {
	AbstractDeobfuscator deobfuscator;
	public Deobfuscator(String version, Info.SideType type, Info.MappingType mapping) {
		Objects.requireNonNull(version, "version cannot be null!");
		Objects.requireNonNull(type, "type cannot be null!");
		switch(mapping) {
			case PROGUARD:
				deobfuscator = new ProguardDeobfuscator(version, type);
				break;
			default:
				throw new IllegalArgumentException("MappingType " + mapping + " is not supported now");
		}
	}
	public void deobfuscate() {
		try {
			Path tempPath = Paths.get(InfoProviders.get().getTempPath());
			Files.createDirectories(tempPath);
			Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtil.deleteDirectory(tempPath)));
		} catch (FileAlreadyExistsException ignored) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		deobfuscator.deobfuscate();
	}
}