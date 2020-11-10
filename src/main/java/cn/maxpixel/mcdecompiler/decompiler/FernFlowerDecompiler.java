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

package cn.maxpixel.mcdecompiler.decompiler;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

// Do not extents AbstractLibRecommendedDecompiler because this decompiler cannot read some of the libraries successfully
// TODO: Make FernFlowerDecompiler read all libraries successfully
public class FernFlowerDecompiler// extends AbstractLibRecommendedDecompiler
		implements IDecompiler {
	FernFlowerDecompiler() {}
	@Override
	public SourceType getSourceType() {
		return SourceType.DIRECTORY;
	}
	@Override
	public void decompile(Path source, Path target) throws IOException {
		checkArgs(source, target);
		Map<String, Object> options = new Object2ObjectOpenHashMap<>();
		options.put("log", "TRACE");
		options.put("dgs", "1");
		options.put("hdc", "0");
		options.put("asc", "1");
		options.put("udv", "0");
		options.put("rsy", "1");
		ConsoleDecompiler decompiler = new AccessibleConsoleDecompiler(target.toFile(), options, new PrintStreamLogger(System.out));
		decompiler.addSource(source.toFile());
//		List<String> libs = listLibs();
//		for(int index = 0; index < libs.size(); index++) decompiler.addLibrary(new File(libs.get(index)));
		decompiler.decompileContext();
	}
	private static class AccessibleConsoleDecompiler extends ConsoleDecompiler {
		public AccessibleConsoleDecompiler(File destination, Map<String, Object> options, IFernflowerLogger logger) {
			super(destination, options, logger);
		}
	}
}