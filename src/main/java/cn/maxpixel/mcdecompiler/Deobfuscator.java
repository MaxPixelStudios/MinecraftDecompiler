/*
 * MinecraftDecompiler. A tool/library to deobfuscate and decompile Minecraft.
 * Copyright (C) 2020  XiaoPangxie732
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

package cn.maxpixel.mcdecompiler;

import cn.maxpixel.mcdecompiler.reader.NotchMappingReader;
import cn.xiaopangxie732.easynetwork.coder.ByteDecoder;
import cn.xiaopangxie732.easynetwork.http.HttpConnection;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Deobfuscator {
	private String version;
	private Info.MappingType type;
	private static JsonArray versions;
	private JsonObject version_json;
	static {
		versions = JsonParser.parseString(ByteDecoder.decodeToString(HttpConnection
				.newGetConnection("https://launchermeta.mojang.com/mc/game/version_manifest.json")))
				.getAsJsonObject().get("versions").getAsJsonArray();
	}
	public Deobfuscator(String version, Info.MappingType type) {
		this.version = Objects.requireNonNull(version);
		this.type = Objects.requireNonNull(type);
		downloadMapping();
		downloadJar();
	}
	private Deobfuscator downloadMapping() {
		checkVersion();
		File f = new File(Info.getMappingPath(version, type));
		f.getParentFile().mkdirs();
		if(!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try(FileOutputStream fout = new FileOutputStream(f)) {
				fout.write(HttpConnection.newGetConnection(
						version_json.get("downloads").getAsJsonObject().get(type + "_mappings").getAsJsonObject().get("url").getAsString()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this;
	}
	private Deobfuscator downloadJar() {
		File f = new File(Info.getMcJarPath(version, type));
		f.getParentFile().mkdirs();
		if(!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try(FileOutputStream fout = new FileOutputStream(f)) {
				fout.write(HttpConnection.newGetConnection(
						version_json.get("downloads").getAsJsonObject().get(type.toString()).getAsJsonObject().get("url").getAsString()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this;
	}
	public Deobfuscator deobfuscate() {
		try {
			System.out.println("deobfuscating...");
			File f = new File(Info.getDeobfuscateJarPath(version));
			f.getParentFile().mkdirs();
			f.createNewFile();
			File temp = new File(Info.TEMP_PATH);
			temp.mkdirs();
			decompress(new JarFile(Info.getMcJarPath(version, type)), new File(temp, "originalClasses"));
			new NotchMappingReader(Info.getMappingPath(version, type)).getMappings().forEach(classMapping -> {
				try {
					ClassReader reader = new ClassReader("temp/originalClasses/" + classMapping.getObfuscatedName()
							.replace(".", "/"));
					ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
					reader.accept(new ClassRemapper(writer, new ASMRemapper(classMapping)), ClassReader.SKIP_DEBUG);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}
	private void decompress(JarFile jar, File target) {
		System.out.println("Decompressing");
		Enumeration<? extends JarEntry> e = jar.entries();
		while(e.hasMoreElements()) {
			JarEntry entry = e.nextElement();
			if(entry.isDirectory()) {
				File dir = new File(target, entry.getName());
				dir.mkdirs();
			} else {
				try {
					File f = new File(target, entry.getName());
					f.getParentFile().mkdirs();
					f.createNewFile();
					InputStream is = jar.getInputStream(entry);
					FileOutputStream out = new FileOutputStream(f);
					int i;
					while((i = is.read()) != -1) {
						out.write(i);
					}
					is.close();
					out.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		try {
			jar.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	private void checkVersion() {
		System.out.println("checking version...");
		for (JsonElement element : versions) {
			JsonObject object = element.getAsJsonObject();
			if(object.get("id").getAsString().equalsIgnoreCase(version)) {
				version_json = JsonParser.parseString(ByteDecoder.decodeToString(HttpConnection
						.newGetConnection(object.get("url").getAsString()))).getAsJsonObject();
				if (version_json.get("downloads").getAsJsonObject().has(type + "_mappings")) break;
				else throw new RuntimeException("This version doesn't have mappings");
			}
		}
	}
}