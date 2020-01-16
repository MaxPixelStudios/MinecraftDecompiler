/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2020  XiaoPangxie732
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

public class DeobfuscatorCommandLine {
	public static final String USAGE = "java -jar MinecraftDecompiler.jar <version(1.14 or above)> <c or s>";
	public static void main(String[] args) {
		Deobfuscator deobfuscator = new Deobfuscator("1.14.4", Info.MappingType.SERVER);

	}
}