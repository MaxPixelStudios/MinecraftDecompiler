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

package cn.maxpixel.mcdecompiler.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static cn.maxpixel.mcdecompiler.MinecraftDecompiler.HTTP_CLIENT;

public class VersionManifest {
    private static final Logger LOGGER = Logging.getLogger();
    public static final JsonObject VERSION_MANIFEST;
    private static final Map<String, String> versions;
    private static final Object2ObjectOpenHashMap<String, JsonObject> versionJsonCache = new Object2ObjectOpenHashMap<>();

    public static JsonObject get(String versionId) {
        if(!versions.containsKey(Objects.requireNonNull(versionId, "versionId cannot be null!")))
            throw new RuntimeException("Game ID \"" + versionId + "\" does not exists!");
        return versionJsonCache.computeIfAbsent(versionId, _id -> {
            try(InputStreamReader isr = new InputStreamReader(
                    HTTP_CLIENT.send(HttpRequest.newBuilder(URI.create(versions.get(_id))).build(),
                            HttpResponse.BodyHandlers.ofInputStream()).body())) {
                return JsonParser.parseReader(isr).getAsJsonObject();
            } catch (IOException | InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Error fetching Minecraft version JSON", e);
                throw Utils.wrapInRuntime(e);
            }
        });
    }

    static {
        try(InputStreamReader isr = new InputStreamReader(
                HTTP_CLIENT.send(HttpRequest.newBuilder(URI.create("https://launchermeta.mojang.com/mc/game/version_manifest.json")).build(),
                        HttpResponse.BodyHandlers.ofInputStream()).body())) {
            VERSION_MANIFEST = JsonParser.parseReader(isr).getAsJsonObject();
            versions = StreamSupport.stream(VERSION_MANIFEST.getAsJsonArray("versions").spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .collect(Collectors.toMap(obj->obj.get("id").getAsString(), obj->obj.get("url").getAsString()));
        } catch (IOException | InterruptedException e) {
            throw Utils.wrapInRuntime(e);
        }
    }
}