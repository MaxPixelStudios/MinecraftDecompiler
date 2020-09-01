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

package cn.maxpixel.mcdecompiler.decompiler;

import cn.maxpixel.mcdecompiler.DeobfuscatorCommandLine;
import cn.maxpixel.mcdecompiler.util.VersionManifest;
import cn.xiaopangxie732.easynetwork.http.HttpConnection;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.StreamSupport;

public abstract class AbstractLibRecommendedDecompiler implements ILibRecommendedDecompiler {
	private final List<String> libs = new ObjectArrayList<>();
	@Override
	public void downloadLib(Path libDir, String version) {
		StreamSupport.stream(VersionManifest.getVersion(version).getAsJsonArray("libraries").spliterator(), true)
				.map(JsonElement::getAsJsonObject).filter(obj->obj.has("artifact")).map(obj->obj.get("artifact").getAsJsonObject())
				.map(obj->obj.get("url").getAsString()).forEach(url -> {
					String fileName = url.substring(url.lastIndexOf('/') + 1);
					Path file = libDir.resolve(fileName);
					if(Files.exists(file)) return;
					try {
						Files.copy(HttpConnection.newGetConnection(url, DeobfuscatorCommandLine.PROXY).getIn(), file);
					} catch (IOException e) {
						e.printStackTrace();
					}
					libs.add(file.toAbsolutePath().normalize().toString());
		});
	}
	protected List<String> listLibs() {
		return libs;
	}
}