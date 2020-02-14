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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ClassMapping extends Mapping {
	private List<MethodMapping> methods;
	private Map<String, FieldMapping> fields;
	public ClassMapping() {
		this.methods = new ObjectArrayList<>();
		this.fields = new Object2ObjectOpenHashMap<>();
	}
	public ClassMapping(String obfuscatedName, String originalName) {
		super(obfuscatedName, originalName);
		this.methods = new ObjectArrayList<>();
		this.fields = new Object2ObjectOpenHashMap<>();
	}

	public ClassMapping addFields(FieldMapping... fields) {
		for (FieldMapping field : fields) {
			this.fields.put(field.getObfuscatedName(), field);
		}
		return this;
	}
	public ClassMapping addMethods(MethodMapping... methods) {
		this.methods.addAll(Arrays.asList(methods));
		return this;
	}
	public ClassMapping addField(FieldMapping field) {
		this.fields.put(field.getObfuscatedName(), field);
		return this;
	}
	public ClassMapping addMethod(MethodMapping method) {
		this.methods.add(method);
		return this;
	}
	public List<MethodMapping> getMethods() {
		return methods;
	}
	public List<FieldMapping> getFields() {
		return new ArrayList<>(fields.values());
	}
	public Map<String, FieldMapping> getFieldMap() {
		return fields;
	}
	public FieldMapping getField(String obfuscatedName) {
		return fields.get(obfuscatedName);
	}
}