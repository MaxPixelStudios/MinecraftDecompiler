/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile jars.
 * Copyright (C) 2019-2024 MaxPixelStudios(XiaoPangxie732)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mcdecompiler.common.app.util;

import cn.maxpixel.mcdecompiler.common.Constants;
import cn.maxpixel.mcdecompiler.common.app.Directories;
import cn.maxpixel.mcdecompiler.common.app.SideType;
import cn.maxpixel.mcdecompiler.common.util.LambdaUtil;
import cn.maxpixel.mcdecompiler.common.util.Utils;
import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.StreamSupport;

import static java.nio.file.StandardOpenOption.*;

public class DownloadingUtil {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final Proxy INTERNAL_PROXY = Constants.IS_DEV ?
            new Proxy(Proxy.Type.HTTP, new InetSocketAddress(1080)) : // Just for internal testing.
            Proxy.NO_PROXY;
    private static final OpenOption[] OPEN_OPTIONS = {CREATE, WRITE, TRUNCATE_EXISTING};

    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .proxy(new ProxySelector() {
                private static final Logger LOGGER = LogManager.getLogger("Proxy");
                private static final List<Proxy> PROXY_LIST = List.of(INTERNAL_PROXY);
                @Override
                public List<Proxy> select(URI uri) {
                    return PROXY_LIST;
                }

                @Override
                public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                    LOGGER.warn("Error connecting to {}", uri, ioe);
                }
            })
            .connectTimeout(Duration.ofSeconds(10L))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Blocking
    public static Path downloadJarSync(@NotNull String version, @NotNull SideType type) {
        return downloadJar(version, type).join();
    }

    @NonBlocking
    public static CompletableFuture<Path> downloadJar(@NotNull String version, @NotNull SideType type) {
        return VersionManifest.get(version).thenComposeAsync(object -> {
            String id = object.get("id").getAsString();
            Path p = Directories.DOWNLOAD_DIR.resolve(id).resolve(type + ".jar");
            JsonObject download = object.getAsJsonObject("downloads").getAsJsonObject(type.toString());
            if (!FileUtil.verify(p, download.get("sha1").getAsString(), download.get("size").getAsLong())) {
                LOGGER.info("Downloading {} {} jar...", id, type);
                return HTTP_CLIENT.sendAsync(
                        HttpRequest.newBuilder(URI.create(download.get("url").getAsString())).build(),
                        HttpResponse.BodyHandlers.ofFile(FileUtil.makeParentDirs(p), OPEN_OPTIONS)
                ).thenApply(HttpResponse::body);
            } else return CompletableFuture.completedFuture(p);
        }).whenComplete((p, ex) -> {
            if (ex != null) LOGGER.error("Error downloading minecraft jar", ex);
        });
    }

    @Blocking
    public static BufferedReader downloadMappingSync(@NotNull String version, @NotNull SideType type) {
        return downloadMapping(version, type).join();
    }

    @NonBlocking
    public static CompletableFuture<BufferedReader> downloadMapping(@NotNull String version, @NotNull SideType type) {
        return VersionManifest.get(version).thenComposeAsync(object -> {
            String id = object.get("id").getAsString();
            JsonObject mappings = object.getAsJsonObject("downloads")
                    .getAsJsonObject(type + "_mappings");
            if (mappings == null) throw new IllegalArgumentException("Version \"" + id +
                    "\" doesn't have official mappings. Please use 1.14.4 or above");
            Path p = Directories.DOWNLOAD_DIR.resolve(id).resolve(type + "_mappings.txt");
            if (!FileUtil.verify(p, mappings.get("sha1").getAsString(), mappings.get("size").getAsLong())) {
                LOGGER.info("Downloading {} {} mapping...", id, type);
                return HTTP_CLIENT.sendAsync(
                        HttpRequest.newBuilder(URI.create(mappings.get("url").getAsString())).build(),
                        HttpResponse.BodyHandlers.ofFile(FileUtil.makeParentDirs(p), OPEN_OPTIONS)
                ).thenApply(HttpResponse::body);
            } else return CompletableFuture.completedFuture(p);
        }).thenApply(LambdaUtil.unwrap(Files::newBufferedReader, LambdaUtil::rethrowAsCompletion));
    }

    /**
     * Download the remote resource if the local file is invalid
     * @param localPath The local file
     * @param remoteResource The URI of the resource
     * @param remoteHash The hash file of the resource
     * @return The input stream of the local file
     */
    @Blocking
    public static InputStream getRemoteResource(@NotNull Path localPath, @NotNull URI remoteResource, @NotNull URI remoteHash) throws IOException {
        try {
            if (!FileUtil.verify(Objects.requireNonNull(localPath), HTTP_CLIENT.send(HttpRequest.newBuilder(remoteHash)
                    .build(), HttpResponse.BodyHandlers.ofString()).body())) {
                LOGGER.debug("Downloading the resource");
                HTTP_CLIENT.send(HttpRequest.newBuilder(remoteResource).build(),
                        HttpResponse.BodyHandlers.ofFile(FileUtil.makeParentDirs(localPath), OPEN_OPTIONS));
            }
        } catch (InterruptedException e) {
            LOGGER.fatal("Download process interrupted", e);
            throw Utils.wrapInRuntime(e);
        }
        return Files.newInputStream(localPath, READ);
    }

    /**
     * Download the libraries of Minecraft with the given version to the given directory
     * @param version version of Minecraft
     * @param libDir Directory to download the libraries to
     * @return All the lib paths
     */
    @Blocking
    public static @NotNull ObjectSet<Path> downloadLibraries(@Nullable String version, @NotNull Path libDir) {
        if (version == null || version.isBlank()) {
            LOGGER.trace("Invalid version, skipping downloading libs");
            return ObjectOpenHashSet.of();
        }
        LOGGER.info("Downloading libs of version {}", version);
        return StreamSupport.stream(VersionManifest.getSync(version).getAsJsonArray("libraries").spliterator(), true)
                .map(ele -> ele.getAsJsonObject().getAsJsonObject("downloads"))
                .map(obj -> obj.getAsJsonObject("artifact"))
                .filter(Objects::nonNull)
                .map(artifact -> {
                    String url = artifact.get("url").getAsString();
                    Path file = libDir.resolve(url.substring(url.lastIndexOf('/') + 1)); // libDir.resolve(lib file name)
                    if (!FileUtil.verify(file, artifact.get("sha1").getAsString(), artifact.get("size").getAsLong())) {
                        LOGGER.debug("Downloading {}", url);
                        try {
                            HTTP_CLIENT.send(HttpRequest.newBuilder(URI.create(url)).build(),
                                    HttpResponse.BodyHandlers.ofFile(file, OPEN_OPTIONS));
                        } catch (IOException e) {
                            LOGGER.fatal("Error downloading files", e);
                            throw Utils.wrapInRuntime(e);
                        } catch (InterruptedException e) {
                            LOGGER.fatal("Download process interrupted", e);
                            throw Utils.wrapInRuntime(e);
                        }
                    }
                    return file;
                }).collect(ObjectOpenHashSet.toSet());
    }
}