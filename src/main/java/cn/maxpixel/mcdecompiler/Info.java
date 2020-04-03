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

import java.io.File;

public interface Info {
	String getProguardMappingDownloadPath(String version, SideType type);
	File getMappingPath();
	String getDecompileDirectory(String version, SideType type);
	String getMcJarPath(String version, SideType type);
	String getDeobfuscateJarPath(String version, SideType type);
	default String getTempOriginalClassesPath(String version, SideType type) {
		return getTempPath() + "/" + version + "/" + type + "/originalClasses";
	}
	default String getTempRemappedClassesPath(String version, SideType type) {
		return getTempPath() + "/" + version + "/" + type + "/remappedClasses";
	}
	default String getTempDecompileClassesPath(String version, SideType type) {
		return getTempPath() + "/" + version + "/" + type + "/decompileClasses";
	}
	default String getTempDecompilerPath(DecompilerType type) {
		return getTempPath() + "/" + type + ".jar";
	}
	String getTempPath();
	String FILE_SEPARATOR = System.getProperty("file.separator");
	enum SideType {
		CLIENT,
		SERVER;
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	enum MappingType {
		PROGUARD,
		SRG,
		TSRG,
		CSRG
	}
	enum DecompilerType {
		FERNFLOWER,
		CFR;
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
}