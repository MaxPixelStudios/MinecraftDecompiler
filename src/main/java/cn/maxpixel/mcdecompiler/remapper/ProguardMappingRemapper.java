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

package cn.maxpixel.mcdecompiler.remapper;

import cn.maxpixel.mcdecompiler.asm.SuperClassMapping;
import cn.maxpixel.mcdecompiler.mapping.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.FieldMapping;
import cn.maxpixel.mcdecompiler.mapping.MethodMapping;
import cn.maxpixel.mcdecompiler.util.NamingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.commons.Remapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ProguardMappingRemapper extends Remapper {
	private final Map<String, ClassMapping> mappingByObfus;
	private final Map<String, ClassMapping> mappingByOri;
	private final SuperClassMapping superClassMapping;
	private static final Logger LOGGER = LogManager.getLogger("Remapper");
	public ProguardMappingRemapper(Map<String, ClassMapping> mappingByObfus, Map<String, ClassMapping> mappingByOri, SuperClassMapping superClassMapping) {
		this.mappingByObfus = mappingByObfus;
		this.mappingByOri = mappingByOri;
		this.superClassMapping = superClassMapping;
	}
	@Override
	public String mapInnerClassName(String name, String ownerName, String innerName) {
		ClassMapping classMapping = mappingByObfus.get(NamingUtil.asJavaName(name));
		if(classMapping != null) {
			String innerClassName = NamingUtil.asNativeName(classMapping.getOriginalName());
			return innerClassName.substring(innerClassName.lastIndexOf('$') + 1);
		} else return innerName;
	}
	@Override
	public String map(String internalName) {
		ClassMapping classMapping = mappingByObfus.get(NamingUtil.asJavaName(internalName));
		if(classMapping != null) return NamingUtil.asNativeName(classMapping.getOriginalName());
		else return internalName;
	}
	@Override
	public String mapMethodName(String owner, String name, String descriptor) {
		if(!(name.contains("<init>") || name.contains("<clinit>"))) {
			ClassMapping classMapping = mappingByObfus.get(NamingUtil.asJavaName(owner));
			if(classMapping != null) {
				AtomicReference<MethodMapping> methodMapping = new AtomicReference<>();
				classMapping.getMethods().forEach(mapping -> {
					if(methodMapping.get() == null) return;
					if(mapping.getObfuscatedName().equals(name)) {
						compareMethodDescriptorAndSet(descriptor, methodMapping, mapping);
					}
				});
				if(methodMapping.get() == null) methodMapping.set(processSuperMethod(owner, name, descriptor));
				if(methodMapping.get() != null) return methodMapping.get().getOriginalName();
			}
		}
		return name;
	}

	private String genDescriptor(String arg) {
		if(arg.contains("[]")) {
			ClassMapping argClass = mappingByOri.get(arg.replace("[]", ""));
			if(argClass != null) {
				StringBuilder arrays = new StringBuilder(2);
				{
					int dimension = NamingUtil.getDimension(arg), i = 0;
					do {
						arrays.append('[').append(']');
						i++;
					} while (i < dimension);
				}
				return NamingUtil.asDescriptor(argClass.getObfuscatedName() + arrays);
			} else return NamingUtil.asDescriptor(arg);
		} else {
			ClassMapping argClass = mappingByOri.get(arg);
			if(argClass != null) return NamingUtil.asDescriptor(argClass.getObfuscatedName());
			else return NamingUtil.asDescriptor(arg);
		}
	}

	private void compareMethodDescriptorAndSet(String descriptor, AtomicReference<MethodMapping> target, MethodMapping compare) {
		StringBuilder builder = new StringBuilder().append('(');
		if(compare.getArgTypes() != null) {
			for(String arg : compare.getArgTypes()) {
				builder.append(genDescriptor(arg));
			}
		}
		builder.append(')')
				.append(genDescriptor(compare.getReturnVal()));
		if(descriptor.contentEquals(builder)) target.set(compare);
	}

	private MethodMapping processSuperMethod(String owner, String name, String descriptor) {
		List<String> superNames = superClassMapping.getMap().get(NamingUtil.asJavaName(owner));
		if(superNames != null) {
			AtomicReference<MethodMapping> methodMapping = new AtomicReference<>();
			superNames.forEach(superClass -> {
				if(methodMapping.get() != null) return;
				ClassMapping superClassMapping = mappingByObfus.get(superClass);
				if(superClassMapping != null) {
					superClassMapping.getMethods().forEach(methodMapping1 -> {
						if(methodMapping1.getObfuscatedName().equals(name)) {
							compareMethodDescriptorAndSet(descriptor, methodMapping, methodMapping1);
						}
					});
				}
			});
			if(methodMapping.get() == null) {
				superNames.forEach(superClass -> {
					if(methodMapping.get() != null) return;
					ClassMapping superClassMapping = mappingByObfus.get(superClass);
					if(superClassMapping != null) {
						methodMapping.set(processSuperMethod(superClassMapping.getObfuscatedName(), name, descriptor));
					}
				});
			}
			if(methodMapping.get() != null) return methodMapping.get();
		}
		return null;
	}
	@Override
	public String mapFieldName(String owner, String name, String descriptor) {
		ClassMapping classMapping = mappingByObfus.get(NamingUtil.asJavaName(owner));
		if(classMapping != null) {
			FieldMapping fieldMapping = classMapping.getField(name);
			if(fieldMapping == null) fieldMapping = processSuperField(owner, name);
			if(fieldMapping != null) return fieldMapping.getOriginalName();
		}
		return name;
	}
	private FieldMapping processSuperField(String owner, String name) {
		if(superClassMapping.getMap().get(NamingUtil.asJavaName(owner)) != null) {
			AtomicReference<FieldMapping> fieldMapping = new AtomicReference<>();
			superClassMapping.getMap().get(NamingUtil.asJavaName(owner)).forEach(superClass -> {
				ClassMapping supermapping = mappingByObfus.get(superClass);
				if(supermapping != null && fieldMapping.get() == null) {
					fieldMapping.set(supermapping.getField(name));
				}
			});
			if(fieldMapping.get() == null) {
				superClassMapping.getMap().get(NamingUtil.asJavaName(owner)).forEach(superClass -> {
					if(fieldMapping.get() == null) {
						ClassMapping supermapping = mappingByObfus.get(superClass);
						if(supermapping != null) {
							fieldMapping.set(processSuperField(supermapping.getObfuscatedName(), name));
						}
					}
				});
			}
			if(fieldMapping.get() != null) return fieldMapping.get();
		}
		return null;
	}
}