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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.Arrays;
import java.util.HashMap;

public class ASMRemapper extends Remapper {
	private HashMap<String, ClassMapping> mapping;
	private static final Logger LOGGER = LogManager.getLogger("");
	public ASMRemapper(HashMap<String, ClassMapping> mapping) {
		this.mapping = mapping;
	}

	@Override
	public String mapDesc(String descriptor) {
		LOGGER.debug("Method: mapDesc(String), Args: descriptor=\"{}\"", descriptor);
		return super.mapDesc(descriptor);
	}

	@Override
	public String mapType(String internalName) {
		LOGGER.debug("Method: mapType(String), Args: internalName=\"{}\"", internalName);
		return super.mapType(internalName);
	}

	@Override
	public String[] mapTypes(String[] internalNames) {
		LOGGER.debug("Method: mapTypes(String[]), Args: internalNames=\"{}\"", Arrays.toString(internalNames));
		return super.mapTypes(internalNames);
	}

	@Override
	public String mapMethodDesc(String methodDescriptor) {
		LOGGER.debug("Method: mapMethodDesc(String), Args: methodDescriptor=\"{}\"", methodDescriptor);
		return super.mapMethodDesc(methodDescriptor);
	}

	@Override
	public Object mapValue(Object value) {
		LOGGER.debug("Method: mapValue(Object), Args: value=\"{}\"", value);
		return super.mapValue(value);
	}

	@Override
	public String mapSignature(String signature, boolean typeSignature) {
		LOGGER.debug("Method: mapSignature(String, boolean), Args: signature=\"{}\", typeSignature=\"{}\"", signature, typeSignature);
		return super.mapSignature(signature, typeSignature);
	}

	@Override
	public SignatureVisitor createSignatureRemapper(SignatureVisitor signatureVisitor) {
		LOGGER.debug("Method: createSignatureRemapper(SignatureVisitor), Args: signatureVisitor=\"{}\"", signatureVisitor);
		return super.createSignatureRemapper(signatureVisitor);
	}

	@Override
	public String mapInnerClassName(String name, String ownerName, String innerName) {
		LOGGER.debug("Method: mapInnerClassName(String, String, String), Args: name=\"{}\", ownerName=\"{}\", innerName=\"{}\"", name, ownerName, innerName);
		return super.mapInnerClassName(name, ownerName, innerName);
	}

	@Override
	public String mapMethodName(String owner, String name, String descriptor) {
		LOGGER.debug("Method: mapMethodName(String, String, String), Args: owner=\"{}\", name=\"{}\", descriptor=\"{}\"", owner, name, descriptor);
		return super.mapMethodName(owner, name, descriptor);
	}

	@Override
	public String mapInvokeDynamicMethodName(String name, String descriptor) {
		LOGGER.debug("Method: mapTypes(String, String), Args: name=\"{}\", descriptor=\"{}\"", name, descriptor);
		return super.mapInvokeDynamicMethodName(name, descriptor);
	}

	@Override
	public String mapFieldName(String owner, String name, String descriptor) {
		LOGGER.debug("Method: mapFieldName(String, String, String), Args: owner=\"{}\", name=\"{}\", descriptor=\"{}\"", owner, name, descriptor);
		return super.mapFieldName(owner, name, descriptor);
	}

	@Override
	public String mapPackageName(String name) {
		LOGGER.debug("Method: mapPackageName(String), Args: name=\"{}\"", name);
		return super.mapPackageName(name);
	}

	@Override
	public String mapModuleName(String name) {
		LOGGER.debug("Method: mapModuleName(String), Args: name=\"{}\"", name);
		return super.mapModuleName(name);
	}

	@Override
	public String map(String internalName) {
		LOGGER.debug("Method: map(String), Args: internalNames=\"{}\"", internalName);
		return super.map(internalName);
	}
}