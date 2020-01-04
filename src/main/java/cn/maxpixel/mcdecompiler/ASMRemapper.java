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
import org.objectweb.asm.commons.Remapper;

import java.util.Objects;

public class ASMRemapper extends Remapper {
	private ClassMapping mapping;
	public ASMRemapper(ClassMapping mapping) {
		this.mapping = mapping;
	}
	@Override
	public String mapMethodName(String owner, String name, String descriptor) {
		return super.mapMethodName(owner, name, descriptor);
	}

	@Override
	public String mapFieldName(String owner, String name, String descriptor) {
		if(owner.equals(mapping.getObfuscatedName()) && Objects.nonNull(mapping.getField(name))) {

		}
		return super.mapFieldName(owner, name, descriptor);
	}

	@Override
	public String mapPackageName(String name) {
		if(Objects.nonNull(name)) {
			return name.equals(NamingUtil.getPackageName(mapping.getObfuscatedName())) ?
					mapping.getObfuscatedName() : name;
		}
		return null;
	}

	@Override
	public String map(String internalName) {
		return mapping.getObfuscatedName().equals(internalName) ?
				mapping.getOriginalName() : internalName;
	}
}