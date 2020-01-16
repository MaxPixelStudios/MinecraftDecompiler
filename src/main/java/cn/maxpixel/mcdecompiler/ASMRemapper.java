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
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.HashMap;

public class ASMRemapper extends Remapper {
	private HashMap<String, ClassMapping> mapping;
	public ASMRemapper(HashMap<String, ClassMapping> mapping) {
		this.mapping = mapping;
	}

	@Override
	public String mapDesc(String descriptor) {
		return super.mapDesc(descriptor);
	}

	@Override
	public String mapType(String internalName) {
		return super.mapType(internalName);
	}

	@Override
	public String[] mapTypes(String[] internalNames) {
		return super.mapTypes(internalNames);
	}

	@Override
	public String mapMethodDesc(String methodDescriptor) {
		return super.mapMethodDesc(methodDescriptor);
	}

	@Override
	public Object mapValue(Object value) {
		return super.mapValue(value);
	}

	@Override
	public String mapSignature(String signature, boolean typeSignature) {
		return super.mapSignature(signature, typeSignature);
	}

	@Override
	public SignatureVisitor createSignatureRemapper(SignatureVisitor signatureVisitor) {
		return super.createSignatureRemapper(signatureVisitor);
	}

	@Override
	public String mapInnerClassName(String name, String ownerName, String innerName) {
		return super.mapInnerClassName(name, ownerName, innerName);
	}

	@Override
	public String mapMethodName(String owner, String name, String descriptor) {
		return super.mapMethodName(owner, name, descriptor);
	}

	@Override
	public String mapInvokeDynamicMethodName(String name, String descriptor) {
		return super.mapInvokeDynamicMethodName(name, descriptor);
	}

	@Override
	public String mapFieldName(String owner, String name, String descriptor) {
		return super.mapFieldName(owner, name, descriptor);
	}

	@Override
	public String mapPackageName(String name) {
		return super.mapPackageName(name);
	}

	@Override
	public String mapModuleName(String name) {
		return super.mapModuleName(name);
	}

	@Override
	public String map(String internalName) {
		return super.map(internalName);
	}
}