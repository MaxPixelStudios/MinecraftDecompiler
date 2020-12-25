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

import java.nio.file.Path;

public interface Info {
    String getDownloadPath();
    default String getProguardMappingDownloadPath(String version, SideType type) {
        return getDownloadPath() + "/" + version + "/" + type + "_mappings.txt";
    }
    default String getLibDownloadPath() {
        return getDownloadPath() + "/libs/";
    }
    Path getMappingPath();
    default String getDecompileDirectory(String version, SideType type) {
        return getOutputPath() + "/" + version + "_" + type + "_decompiled/";
    }
    default String getMcJarPath(String version, SideType type) {
        return getDownloadPath() + "/" + version + "/" + type + ".jar";
    }
    default String getDeobfuscateJarPath(String version, SideType type) {
        return getOutputPath() + "/" + version + "_" + type + "_deobfuscated.jar";
    }
    String getOutputPath();
    default Path getTempOriginalClassesPath() {
        return getTempPath().resolve("originalClasses");
    }
    default Path getTempRemappedClassesPath() {
        return getTempPath().resolve("remappedClasses");
    }
    default Path getTempDecompileClassesPath() {
        return getTempPath().resolve("decompileClasses");
    }
    default Path getTempDecompilerPath(DecompilerType type) {
        return getTempPath().resolve(type + ".jar");
    }
    Path getTempPath();
    String FILE_SEPARATOR = System.getProperty("file.separator");
    String PATH_SEPARATOR = System.getProperty("path.separator");
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
        CSRG,
        TINY
    }
    enum DecompilerType {
        FERNFLOWER,
        OFFICIAL_FERNFLOWER,
        FORGEFLOWER,
        CFR,
        USER_DEFINED;
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}