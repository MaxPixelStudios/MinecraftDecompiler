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

package cn.maxpixel.mcdecompiler.util;

import cn.maxpixel.mcdecompiler.DeobfuscatorCommandLine;
import cn.xiaopangxie732.easynetwork.http.HttpConnection;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class VersionManifest {
	public static final JsonObject VERSION_MANIFEST;
	private static final Map<String, String> versions;
	private static final Map<String, JsonObject> versionJsonCache = new Object2ObjectOpenHashMap<>();

	public static JsonObject getVersion(String id) {
		if(!versions.containsKey(Objects.requireNonNull(id, "id cannot be null!"))) throw new RuntimeException("Game ID does not exists!");
		return versionJsonCache.computeIfAbsent(id,
				id1->JsonParser.parseReader(HttpConnection.newGetConnection(versions.get(id1), DeobfuscatorCommandLine.PROXY).getReader()).getAsJsonObject());
	}

	static {
		VERSION_MANIFEST = JsonParser.parseReader(HttpConnection.newGetConnection("https://launchermeta.mojang.com/mc/game/version_manifest.json",
													DeobfuscatorCommandLine.PROXY).getReader()).getAsJsonObject();
		versions = StreamSupport.stream(VERSION_MANIFEST.getAsJsonArray("versions").spliterator(), true)
				.map(JsonElement::getAsJsonObject).collect(Collectors.toMap(obj->obj.get("id").getAsString(), obj->obj.get("url").getAsString()));
	}
}