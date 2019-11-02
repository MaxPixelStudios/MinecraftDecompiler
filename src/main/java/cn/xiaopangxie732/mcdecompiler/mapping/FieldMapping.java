package cn.xiaopangxie732.mcdecompiler.mapping;

public class FieldMapping extends Mapping {
	private String type;
	public FieldMapping(String obfuscatedName, String originalName, String type) {
		super(obfuscatedName, originalName);
		this.type = type;
	}
	public FieldMapping() {}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}