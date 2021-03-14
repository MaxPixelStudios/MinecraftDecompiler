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

package cn.maxpixel.mcdecompiler.decompiler;

import cn.maxpixel.mcdecompiler.util.NetworkUtil;
import cn.maxpixel.mcdecompiler.util.VersionManifest;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.StreamSupport;

public abstract class AbstractLibRecommendedDecompiler implements ILibRecommendedDecompiler {
    private static final Logger LOGGER = LogManager.getLogger("Lib downloader");
    private final List<String> libs = new ObjectArrayList<>();
    @Override
    public void downloadLib(Path libDir, String version) throws IOException {
        if(version == null || version.isEmpty()) {
            LOGGER.info("Minecraft version is not provided, skipping downloading libs");
            return;
        }
        LOGGER.info("downloading libs of version " + version);
        StreamSupport.stream(VersionManifest.getVersion(version).getAsJsonArray("libraries").spliterator(), true)
                .map(ele->ele.getAsJsonObject().get("downloads").getAsJsonObject()).filter(obj->obj.has("artifact"))
                .map(obj->obj.get("artifact").getAsJsonObject().get("url").getAsString()).forEach(url -> {
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            Path file = libDir.resolve(fileName);
            libs.add(file.toAbsolutePath().normalize().toString());
            if(Files.exists(file)) return;
            try {
                LOGGER.debug("downloading " + url);
                Files.copy(NetworkUtil.newBuilder(url).connect().asStream(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Get all Minecraft libraries
     * @return All Minecraft libs. If version isn't provided, return a empty list
     */
    protected List<String> listLibs() {
        return libs;
    }
}