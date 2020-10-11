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

import cn.maxpixel.mcdecompiler.Info;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.EnumMap;

public class Decompilers {
	private static final EnumMap<Info.DecompilerType, IDecompiler> decompilers = new EnumMap<>(Info.DecompilerType.class);
	private static final Object2ObjectOpenHashMap<String, ICustomizedDecompiler> customizedDecompilers = new Object2ObjectOpenHashMap<>();
	static {
		init();
	}
	private static void init() {
		decompilers.put(Info.DecompilerType.FERNFLOWER, new SpigotFernFlowerDecompiler());
		decompilers.put(Info.DecompilerType.CFR, new CFRDecompiler());
		decompilers.put(Info.DecompilerType.OFFICIAL_FERNFLOWER, new FernFlowerDecompiler());
		decompilers.put(Info.DecompilerType.FORGEFLOWER, new ForgeFlowerDecompiler());
		decompilers.put(Info.DecompilerType.USER_DEFINED, new UserDefinedDecompiler());
	}
	public static IDecompiler get(Info.DecompilerType type) {
		return decompilers.getOrDefault(type, decompilers.get(Info.DecompilerType.FERNFLOWER));
	}
	public static ICustomizedDecompiler getCustomized(String name) {
		return customizedDecompilers.get(name);
	}
}