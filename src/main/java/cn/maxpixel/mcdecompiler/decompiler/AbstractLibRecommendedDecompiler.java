/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2019-2022  MaxPixelStudios
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

import cn.maxpixel.mcdecompiler.util.FileUtil;
import cn.maxpixel.mcdecompiler.util.Logging;
import cn.maxpixel.mcdecompiler.util.Utils;
import cn.maxpixel.mcdecompiler.util.VersionManifest;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import static cn.maxpixel.mcdecompiler.MinecraftDecompiler.HTTP_CLIENT;
import static java.nio.file.StandardOpenOption.*;

public abstract class AbstractLibRecommendedDecompiler implements ILibRecommendedDecompiler {
    private static final Logger LOGGER = Logging.getLogger("Lib Downloader");
    private final ObjectArrayList<String> libs = new ObjectArrayList<>();
    private final ObjectList<String> libsUnmodifiable = ObjectLists.unmodifiable(libs);

    @Override
    public void downloadLib(Path libDir, String version) throws IOException {
        if(version == null || version.isBlank()) {
            LOGGER.info("Minecraft version is not provided, skipping downloading libs");
            return;
        }
        LOGGER.log(Level.INFO, "Downloading libs of version {0}", version);
        StreamSupport.stream(VersionManifest.get(version).getAsJsonArray("libraries").spliterator(), true)
                .map(ele -> ele.getAsJsonObject().get("downloads").getAsJsonObject().get("artifact").getAsJsonObject())
                .forEach(artifact -> {
                    String url = artifact.get("url").getAsString();
                    Path file = libDir.resolve(url.substring(url.lastIndexOf('/') + 1)); // libDir.resolve(lib file name)
                    libs.add(file.toAbsolutePath().normalize().toString());
                    if(!FileUtil.verify(file, artifact.get("sha1").getAsString(), artifact.get("size").getAsLong())) {
                        LOGGER.log(Level.FINER, "Downloading {0}", url);
                        FileUtil.deleteIfExists(file);
                        try {
                            HTTP_CLIENT.send(HttpRequest.newBuilder(URI.create(url)).build(),
                                    HttpResponse.BodyHandlers.ofFile(file, CREATE, WRITE, TRUNCATE_EXISTING))
                                    .body();
                        } catch(IOException e) {
                            LOGGER.log(Level.SEVERE, "Error downloading files", e);
                            throw Utils.wrapInRuntime(e);
                        } catch(InterruptedException e) {
                            LOGGER.log(Level.SEVERE, "Download process interrupted", e);
                            throw Utils.wrapInRuntime(e);
                        }
                    }
                });
    }

    @Override
    public void receiveLibs(ObjectList<Path> libs) {
        this.libs.addAll(Objects.requireNonNull(libs).stream().map(p -> p.toAbsolutePath().normalize().toString())
                .collect(ObjectArrayList.toList()));
    }

    /**
     * Get all Minecraft libraries.
     * @return All Minecraft libs. If the version isn't provided, will return an empty list.
     */
    protected final ObjectList<String> listLibs() {
        return libsUnmodifiable;
    }
}