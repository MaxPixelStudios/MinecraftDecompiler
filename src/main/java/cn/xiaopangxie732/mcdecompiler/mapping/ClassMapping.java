package cn.xiaopangxie732.mcdecompiler.mapping;

import java.util.ArrayList;
import java.util.HashMap;

public class ClassMapping extends Mapping {

	private HashMap<String, MethodMapping> methods;
	private HashMap<String, FieldMapping> fields;

	public ClassMapping(String obfuscatedName, String originalName) {
		super(obfuscatedName, originalName);
		this.methods = new HashMap<>();
		this.fields = new HashMap<>();
	}

	public ClassMapping addFields(FieldMapping... fields) {
		for (FieldMapping field : fields) {
			this.fields.put(field.getObfuscatedName(), field);
		}
		return this;
	}

	public ClassMapping addMethods(MethodMapping... methods) {
		for (MethodMapping method : methods) {
			this.methods.put(method.getObfuscatedName(), method);
		}
		return this;
	}

	public ArrayList<MethodMapping> getMethods() {
		return new ArrayList<>(methods.values());
	}

	public ArrayList<FieldMapping> getFields() {
		return new ArrayList<>(fields.values());
	}

	public MethodMapping getMethod(String obfuscatedName) {
		return methods.get(obfuscatedName);
	}

	public FieldMapping getField(String obfuscatedName) {
		return fields.get(obfuscatedName);
	}
}