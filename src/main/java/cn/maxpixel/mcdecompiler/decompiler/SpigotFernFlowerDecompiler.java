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

import cn.maxpixel.mcdecompiler.util.ProcessUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class SpigotFernFlowerDecompiler implements IExternalJarDecompiler {
	private Path decompilerJarPath;
	SpigotFernFlowerDecompiler() {}
	@Override
	public SourceType getSourceType() {
		return SourceType.DIRECTORY;
	}
	@Override
	public void decompile(Path source, Path target) throws IOException {
		checkArgs(target);
		if(!Files.isDirectory(source)) throw new IllegalArgumentException("source must be directory!");
		Process process = Runtime.getRuntime().exec(new String[] {"java", "-jar", decompilerJarPath.toString(), "-dgs=1", "-hdc=0", "-asc=1", "-udv=0",
				"-rsy=1", "-aoa=1", source.toString(), target.toString()});
		ProcessUtil.waitForProcess(process);
	}
	@Override
	public void extractDecompilerTo(Path decompilerJarPath) throws IOException {
		if(Files.notExists(decompilerJarPath))
			Files.copy(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("fernflower.jar")), decompilerJarPath);
		this.decompilerJarPath = decompilerJarPath;
	}
}