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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class ForgeFlowerDecompiler extends AbstractLibRecommendedDecompiler implements IExternalJarDecompiler {
	private Path decompilerJarPath;
	ForgeFlowerDecompiler() {}
	@Override
	public SourceType getSourceType() {
		return SourceType.DIRECTORY;
	}
	@Override
	public void extractDecompilerTo(Path decompilerJarPath) throws IOException {
		if(Files.notExists(decompilerJarPath))
			Files.copy(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("forgeflower-1.5.478.16.jar")), decompilerJarPath);
		this.decompilerJarPath = decompilerJarPath;
	}
	@Override
	public void decompile(Path source, Path target) throws IOException {
		checkArgs(source, target);
		ObjectArrayList<String> args = new ObjectArrayList<>(new String[] {"java", "-jar", decompilerJarPath.toString(), "-din=1",
				"-rbr=1", "-dgs=1", "-asc=1", "-rsy=1", "-iec=1", "-jvn=1", "-log=TRACE"});
		List<String> libs = listLibs();
		for(int i = 0; i < libs.size(); i++) args.add("-e=" + libs.get(i));
		args.add(source.toString());
		args.add(target.toString());
		Process process = Runtime.getRuntime().exec(args.toArray(new String[0]));
		ProcessUtil.waitForProcess(process);
	}
}