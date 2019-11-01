package cn.xiaopangxie732.mcdecompiler.mapping;

public class Mapping {
	protected String obfuscatedName;
	protected String originalName;
	protected Mapping(String obfuscatedName, String originalName) {
		this.obfuscatedName = obfuscatedName;
		this.originalName = originalName;
	}
	public String getObfuscatedName() {
		return obfuscatedName;
	}
	public void setObfuscatedName(String obfuscatedName) {
		this.obfuscatedName = obfuscatedName;
	}
	public String getOriginalName() {
		return originalName;
	}
	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}
}