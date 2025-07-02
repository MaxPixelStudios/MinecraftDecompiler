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

import cn.maxpixel.mcdecompiler.utils.Utils;
import cn.maxpixel.rewh.logging.LogManager;
import cn.maxpixel.rewh.logging.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Async;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class VersionManifest {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final CompletableFuture<String> LATEST_RELEASE;
    public static final CompletableFuture<String> LATEST_SNAPSHOT;
    private static final CompletableFuture<Object2ObjectOpenHashMap<String, URI>> VERSIONS;
    private static final Object2ObjectOpenHashMap<String, CompletableFuture<JsonObject>> CACHE = new Object2ObjectOpenHashMap<>();

    static {
        CompletableFuture<JsonObject> versionManifest = DownloadingUtil.HTTP_CLIENT.sendAsync(
                HttpRequest.newBuilder(URI.create("https://piston-meta.mojang.com/mc/game/version_manifest.json")).build(),
                HttpResponse.BodyHandlers.ofInputStream()
        ).thenApplyAsync(VersionManifest::parse).whenComplete((o, t) -> {
            if (t != null) LOGGER.error("Error fetching Minecraft version manifest", t);
        });
        CompletableFuture<JsonObject> latest = versionManifest.thenApply(obj -> obj.getAsJsonObject("latest"));
        LATEST_RELEASE = latest.thenApply(obj -> obj.get("release").getAsString());
        LATEST_SNAPSHOT = latest.thenApply(obj -> obj.get("snapshot").getAsString());
        VERSIONS = versionManifest.thenApplyAsync(o -> {
            JsonArray versions = o.getAsJsonArray("versions");
            return StreamSupport.stream(Spliterators.spliterator(versions.iterator(), versions.size(),
                    Spliterator.DISTINCT + Spliterator.NONNULL + Spliterator.IMMUTABLE), true
            ).map(JsonElement::getAsJsonObject).collect(Collectors.toMap(
                    obj -> obj.get("id").getAsString(), obj -> URI.create(obj.get("url").getAsString()),
                    Utils::onKeyDuplicate, Object2ObjectOpenHashMap::new
            ));
        });
    }

    @Blocking
    public static JsonObject getSync(String versionId) {
        return get(versionId).join();
    }

    @NonBlocking
    public static CompletableFuture<JsonObject> get(@Async.Schedule @NotNull String versionId) {
        return switch (Objects.requireNonNull(versionId, "versionId cannot be null!")) {
            case "latest_release" -> LATEST_RELEASE.thenCompose(VersionManifest::get);
            case "latest_snapshot" -> LATEST_SNAPSHOT.thenCompose(VersionManifest::get);
            default -> CACHE.computeIfAbsent(versionId, id -> VERSIONS.thenCompose(versions -> {
                if (!versions.containsKey(id)) throw new IllegalArgumentException("Game ID \"" + id + "\" does not exists!");
                return DownloadingUtil.HTTP_CLIENT.sendAsync(HttpRequest.newBuilder(versions.get(id)).build(),
                        HttpResponse.BodyHandlers.ofInputStream());
            }).thenApplyAsync(VersionManifest::parse).whenComplete((o, t) -> {
                if (t != null) LOGGER.error("Error fetching Minecraft version JSON", t);
            }));
        };
    }

    private static JsonObject parse(HttpResponse<InputStream> response) {
        try (InputStreamReader isr = new InputStreamReader(response.body(), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(isr).getAsJsonObject();
        } catch (IOException e) {
            throw new CompletionException(e);
        }
    }
}