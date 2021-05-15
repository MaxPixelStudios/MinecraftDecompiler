/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2021  MaxPixelStudios
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

package cn.maxpixel.mcdecompiler.deobfuscator;

import cn.maxpixel.mcdecompiler.Info;
import cn.maxpixel.mcdecompiler.Properties;
import cn.maxpixel.mcdecompiler.reader.ProguardMappingReader;
import cn.maxpixel.mcdecompiler.util.FileUtil;
import cn.maxpixel.mcdecompiler.util.NetworkUtil;
import cn.maxpixel.mcdecompiler.util.VersionManifest;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class ProguardDeobfuscator extends AbstractDeobfuscator {
    private JsonObject version_json;
    private String version;
    private Info.SideType type;

    public ProguardDeobfuscator(String mappingPath) {
        super(mappingPath);
    }

    public ProguardDeobfuscator(String version, Info.SideType type) {
        this.version = Objects.requireNonNull(version);
        this.type = Objects.requireNonNull(type);
        downloadMapping(version, type);
        downloadJar(version, type);
    }

    private void downloadMapping(String version, Info.SideType type) {
        version_json = VersionManifest.getVersion(version);
        if(!version_json.get("downloads").getAsJsonObject().has(type + "_mappings"))
            throw new RuntimeException("Version \"" + version + "\" doesn't have Proguard mappings. Please use 1.14.4 or above");
        Path p = Properties.getDownloadedProguardMappingPath(version, type);
        if(Files.notExists(p)) {
            FileUtil.ensureDirectoryExist(p.getParent());
            try(FileChannel channel = FileChannel.open(p, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
                ReadableByteChannel from = NetworkUtil.newBuilder(version_json.get("downloads").getAsJsonObject().get(type + "_mappings")
                        .getAsJsonObject().get("url").getAsString()).connect().asChannel()) {
                LOGGER.info("Downloading mapping...");
                channel.transferFrom(from, 0, Long.MAX_VALUE);
            } catch (IOException e) {
                LOGGER.error("Error when downloading Proguard mapping file", e);
            }
        }
    }

    private void downloadJar(String version, Info.SideType type) {
        Path p = Properties.getDownloadedMcJarPath(version, type);
        if(Files.notExists(p)) {
            FileUtil.ensureDirectoryExist(p.getParent());
            try(FileChannel channel = FileChannel.open(p, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
                ReadableByteChannel from = NetworkUtil.newBuilder(version_json.get("downloads").getAsJsonObject().get(type.toString())
                        .getAsJsonObject().get("url").getAsString()).connect().asChannel()) {
                LOGGER.info("Downloading jar...");
                channel.transferFrom(from, 0, Long.MAX_VALUE);
            } catch(IOException e) {
                LOGGER.error("Error when downloading Minecraft jar", e);
            }
        }
    }
    @Override
    public ProguardDeobfuscator deobfuscate(Path source, Path target, boolean includeOthers, boolean reverse) {
        try {
            ProguardMappingReader mappingReader = new ProguardMappingReader(mappingPath == null ? Properties.getDownloadedProguardMappingPath(
                    Objects.requireNonNull(version), Objects.requireNonNull(type)).toString() : mappingPath);
            sharedDeobfuscate(source, target, mappingReader, includeOthers, reverse);
        } catch (Exception e) {
            LOGGER.error("Error when deobfuscating", e);
        }
        return this;
    }
}