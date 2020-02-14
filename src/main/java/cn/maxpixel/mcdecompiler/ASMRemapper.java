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

import cn.maxpixel.mcdecompiler.mapping.ClassMapping;
import cn.maxpixel.mcdecompiler.mapping.FieldMapping;
import cn.maxpixel.mcdecompiler.mapping.MethodMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.commons.Remapper;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ASMRemapper extends Remapper {
	private Map<String, ClassMapping> mappingByObfus;
	private Map<String, ClassMapping> mappingByOri;
	private SuperClassMapping superClassMapping;
	private static final Logger LOGGER = LogManager.getLogger("Remapper");
	public ASMRemapper(Map<String, ClassMapping> mappingByObfus, Map<String, ClassMapping> mappingByOri, SuperClassMapping superClassMapping) {
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
		}
		else return innerName;
	}
	@Override
	public String mapMethodName(String owner, String name, String descriptor) {
		if(!(name.equals("<init>") || name.equals("<clinit>"))) {
			ClassMapping classMapping = mappingByObfus.get(NamingUtil.asJavaName(owner));
			if(classMapping != null) {
				AtomicReference<MethodMapping> mtdmapping = new AtomicReference<>();
				classMapping.getMethods().forEach(methodMapping -> {
					if(methodMapping.getObfuscatedName().equals(name)) {
						StringBuilder builder = new StringBuilder().append('(');
						if(methodMapping.getArgTypes() != null) {
							for(String arg : methodMapping.getArgTypes()) {
								ClassMapping argClass = mappingByOri.get(arg);
								if(argClass != null) builder.append(NamingUtil.asFQCN(argClass.getObfuscatedName()));
								else builder.append(NamingUtil.asFQCN(arg));
							}
						}
						builder.append(')');
						String returnVal = methodMapping.getReturnVal();
						ClassMapping revClass = mappingByOri.get(returnVal);
						if(revClass != null) builder.append(NamingUtil.asFQCN(revClass.getObfuscatedName()));
						else builder.append(NamingUtil.asFQCN(returnVal));
						if(descriptor.contentEquals(builder)) mtdmapping.set(methodMapping);
					}
				});
				if(mtdmapping.get() == null && superClassMapping.getMap().get(NamingUtil.asJavaName(owner)) != null) {
					superClassMapping.getMap().get(NamingUtil.asJavaName(owner)).forEach(superClass -> {
						mappingByObfus.get(superClass).getMethods().forEach(methodMapping -> {
							if(methodMapping.getObfuscatedName().equals(name)) {
								StringBuilder builder = new StringBuilder().append('(');
								if(methodMapping.getArgTypes() != null) {
									for(String arg : methodMapping.getArgTypes()) {
										ClassMapping argClass = mappingByOri.get(arg);
										if(argClass != null) builder.append(NamingUtil.asFQCN(argClass.getObfuscatedName()));
										else builder.append(NamingUtil.asFQCN(arg));
									}
								}
								builder.append(')');
								String returnVal = methodMapping.getReturnVal();
								ClassMapping revClass = mappingByOri.get(returnVal);
								if(revClass != null) builder.append(NamingUtil.asFQCN(revClass.getObfuscatedName()));
								else builder.append(NamingUtil.asFQCN(returnVal));
								if(descriptor.contentEquals(builder)) mtdmapping.set(methodMapping);
							}
						});
					});
				}
				if(mtdmapping.get() != null) return mtdmapping.get().getOriginalName();
			}
		}
		return name;
	}
	@Override
	public String mapFieldName(String owner, String name, String descriptor) {
		ClassMapping classMapping = mappingByObfus.get(NamingUtil.asJavaName(owner));
		if(classMapping != null) {
			FieldMapping fieldMapping = classMapping.getField(name);
			if(fieldMapping != null) return fieldMapping.getOriginalName();
		}
		return name;
	}
	@Override
	public String map(String internalName) {
		ClassMapping classMapping = mappingByObfus.get(NamingUtil.asJavaName(internalName));
		if(classMapping != null) return NamingUtil.asNativeName(classMapping.getOriginalName());
		else return internalName;
	}
}