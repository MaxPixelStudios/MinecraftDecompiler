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

import cn.maxpixel.mcdecompiler.util.NamingUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Map;

public class SuperClassMapping extends ClassVisitor {
	private Map<String, List<String>> superClassMap = new Object2ObjectOpenHashMap<>();
	public SuperClassMapping() {
		super(Opcodes.ASM7);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		List<String> list = new ObjectArrayList<>(2);
		if(!superName.contains("java/lang/Object")) {
			list.add(NamingUtil.asJavaName(superName));
		}
		if(interfaces != null) for(String interface_ : interfaces) list.add(NamingUtil.asJavaName(interface_));
		if(!list.isEmpty()) superClassMap.put(NamingUtil.asJavaName(name), list);
	}

	public Map<String, List<String>> getMap() {
		return superClassMap;
	}
}