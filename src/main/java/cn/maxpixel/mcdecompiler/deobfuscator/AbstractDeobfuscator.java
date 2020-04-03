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

package cn.maxpixel.mcdecompiler.deobfuscator;

import cn.maxpixel.mcdecompiler.DeobfuscatorCommandLine;
import cn.maxpixel.mcdecompiler.Info;
import cn.maxpixel.mcdecompiler.InfoProviders;
import cn.xiaopangxie732.easynetwork.coder.ByteDecoder;
import cn.xiaopangxie732.easynetwork.http.HttpConnection;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public abstract class AbstractDeobfuscator {
	protected String version;
	protected Info.SideType type;
	protected static final Logger LOGGER = LogManager.getLogger();
	protected static JsonArray versions;
	protected JsonObject version_json;
	static {
		versions = JsonParser.parseString(ByteDecoder.decodeToString(HttpConnection
				.newGetConnection("https://launchermeta.mojang.com/mc/game/version_manifest.json", DeobfuscatorCommandLine.PROXY).getAllBytes()))
				.getAsJsonObject().get("versions").getAsJsonArray();
	}
	protected AbstractDeobfuscator(String version, Info.SideType type) {
		this.version = Objects.requireNonNull(version);
		this.type = Objects.requireNonNull(type);
	}
	public abstract AbstractDeobfuscator deobfuscate();
	protected void downloadJar() {
		File f = new File(InfoProviders.get().getMcJarPath(version, type));
		if(!f.exists()) {
			f.getParentFile().mkdirs();
			LOGGER.info("downloading jar...");
			try(FileChannel channel = FileChannel.open(f.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
				channel.transferFrom(HttpConnection.newGetConnection(
						version_json.get("downloads").getAsJsonObject().get(type.toString()).getAsJsonObject().get("url").getAsString(),
						DeobfuscatorCommandLine.PROXY).getInChannel(), 0, Long.MAX_VALUE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	protected void checkVersion() {
		LOGGER.info("checking version...");
		for (JsonElement element : versions) {
			JsonObject object = element.getAsJsonObject();
			if(object.get("id").getAsString().equalsIgnoreCase(version)) {
				version_json = JsonParser.parseString(ByteDecoder.decodeToString(HttpConnection
						.newGetConnection(object.get("url").getAsString(), DeobfuscatorCommandLine.PROXY).getAllBytes())).getAsJsonObject();
			}
		}
		if(version_json == null) throw new RuntimeException("INVALID VERSION DETECTED: " + version);
	}
}