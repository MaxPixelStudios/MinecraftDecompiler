package cn.xiaopangxie732.mcdecompiler.mapping;

public class Mapping {
	private String obfuscatedName;
	private String originalName;
	protected Mapping(String obfuscatedName, String originalName) {
		this.obfuscatedName = obfuscatedName;
		this.originalName = originalName;
	}
	protected Mapping() {}
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