package cn.xiaopangxie732.mcdecompiler;

import cn.xiaopangxie732.easynetwork.coder.ByteDecoder;
import cn.xiaopangxie732.easynetwork.http.HttpConnection;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

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
	}
	public Deobfuscator downloadMapping() {
		checkVersion(version);
		File f = new File("mappings/version/mappings.txt");
		f.mkdirs();
		try(FileOutputStream fout = new FileOutputStream(f)) {
			fout.write(HttpConnection.newGetConnection(
					version_json.get("downloads").getAsJsonObject().get(type + "_mappings").getAsJsonObject().get("url").getAsString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	public Deobfuscator deobfuscate() {

		return this;
	}
	private void checkVersion(String version) {
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