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
import cn.maxpixel.mcdecompiler.util.Utils;
import cn.maxpixel.mcdecompiler.util.VersionManifest;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.StreamSupport;

import static java.nio.file.StandardOpenOption.*;

public abstract class AbstractLibRecommendedDecompiler implements ILibRecommendedDecompiler {
    private static final Logger LOGGER = LogManager.getLogger("Lib downloader");
    private final ObjectArrayList<String> libs = new ObjectArrayList<>();
    private final ObjectList<String> libsUnmodifiable = ObjectLists.unmodifiable(libs);

    @Override
    public void downloadLib(Path libDir, String version) throws IOException {
        if(version == null || version.isEmpty()) {
            LOGGER.info("Minecraft version is not provided, skipping downloading libs");
            return;
        }
        LOGGER.info("Downloading libs of version {}", version);
        StreamSupport.stream(VersionManifest.getVersion(version).getAsJsonArray("libraries").spliterator(), true)
                .map(ele->ele.getAsJsonObject().get("downloads").getAsJsonObject().get("artifact").getAsJsonObject())
                .forEach(artifact -> {
                    String url = artifact.get("url").getAsString();
                    Path file = libDir.resolve(url.substring(url.lastIndexOf('/') + 1)); // libDir.resolve(lib file name)
                    libs.add(file.toAbsolutePath().normalize().toString());
                    try(FileChannel channel = FileChannel.open(file, CREATE, READ, WRITE)) {
                        if(channel.size() > 0L && channel.size() == artifact.get("size").getAsInt()) {
                            MessageDigest md = MessageDigest.getInstance("SHA-1");
                            ByteBuffer bb = ByteBuffer.allocate(65536);
                            while(channel.read(bb) != -1) {
                                bb.flip();
                                md.update(bb);
                                bb.clear();
                            }
                            StringBuilder out = new StringBuilder();
                            for(byte b : md.digest()) {
                                String hex = Integer.toHexString(b & 0xff);
                                if (hex.length() < 2) out.append('0');
                                out.append(hex);
                            }
                            if(artifact.get("sha1").getAsString().contentEquals(out)) return;
                        }
                        LOGGER.debug("Downloading {}", url);
                        channel.position(0L);
                        channel.transferFrom(NetworkUtil.newBuilder(url).connect().asChannel(), 0, Long.MAX_VALUE);
                    } catch (IOException e) {
                        LOGGER.fatal("IO error occurred, throwing an exception...");
                        throw Utils.wrapInRuntime(LOGGER.throwing(e));
                    } catch (NoSuchAlgorithmException e) {
                        LOGGER.fatal("Hmm... You need a SHA-1 digest implementation.");
                        throw Utils.wrapInRuntime(LOGGER.throwing(e));
                    }
                });
    }

    /**
     * Get all Minecraft libraries.
     * @return All Minecraft libs. If version isn't provided, return a empty list.
     */
    protected final ObjectList<String> listLibs() {
        return libsUnmodifiable;
    }
}