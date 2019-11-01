package cn.xiaopangxie732.mcdecompiler;

import java.util.Objects;

public class Deobfuscator {
	private String version;
	private Info.MappingType type;
	public Deobfuscator(String version, Info.MappingType type) {
		this.version = Objects.requireNonNull(version);
		this.type = Objects.requireNonNull(type);
	}
}