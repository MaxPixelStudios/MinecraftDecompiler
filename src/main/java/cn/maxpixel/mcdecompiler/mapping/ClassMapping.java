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

package cn.maxpixel.mcdecompiler.mapping;

import java.util.ArrayList;
import java.util.HashMap;

public class ClassMapping extends Mapping {
	private HashMap<String, MethodMapping> methods;
	private HashMap<String, FieldMapping> fields;
	public ClassMapping() {
		super();
		this.methods = new HashMap<>();
		this.fields = new HashMap<>();
	}
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
	public ClassMapping addField(FieldMapping field) {
		this.fields.put(field.getObfuscatedName(), field);
		return this;
	}
	public ClassMapping addMethod(MethodMapping method) {
		this.methods.put(method.getObfuscatedName(), method);
		return this;
	}
	public ArrayList<MethodMapping> getMethods() {
		return new ArrayList<>(methods.values());
	}
	public ArrayList<FieldMapping> getFields() {
		return new ArrayList<>(fields.values());
	}
	public HashMap<String, MethodMapping> getMethodMap() {
		return methods;
	}
	public HashMap<String, FieldMapping> getFieldMap() {
		return fields;
	}
	public MethodMapping getMethod(String obfuscatedName) {
		return methods.get(obfuscatedName);
	}
	public FieldMapping getField(String obfuscatedName) {
		return fields.get(obfuscatedName);
	}
}